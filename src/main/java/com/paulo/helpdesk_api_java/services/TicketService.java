package com.paulo.helpdesk_api_java.services;

import com.paulo.helpdesk_api_java.dto.PageResponseDTO;
import com.paulo.helpdesk_api_java.dto.ticket.TicketAssignDTO;
import com.paulo.helpdesk_api_java.dto.ticket.TicketCommentCreateDTO;
import com.paulo.helpdesk_api_java.dto.ticket.TicketCommentResponseDTO;
import com.paulo.helpdesk_api_java.dto.ticket.TicketCreateDTO;
import com.paulo.helpdesk_api_java.dto.ticket.TicketHistoryResponseDTO;
import com.paulo.helpdesk_api_java.dto.ticket.TicketResponseDTO;
import com.paulo.helpdesk_api_java.dto.ticket.TicketStatusUpdateDTO;
import com.paulo.helpdesk_api_java.entities.Ticket;
import com.paulo.helpdesk_api_java.entities.TicketComment;
import com.paulo.helpdesk_api_java.entities.TicketHistory;
import com.paulo.helpdesk_api_java.entities.User;
import com.paulo.helpdesk_api_java.entities.enums.TicketHistoryType;
import com.paulo.helpdesk_api_java.entities.enums.TicketPriority;
import com.paulo.helpdesk_api_java.entities.enums.TicketStatus;
import com.paulo.helpdesk_api_java.entities.enums.UserRoles;
import com.paulo.helpdesk_api_java.repositories.TicketCommentRepository;
import com.paulo.helpdesk_api_java.repositories.TicketHistoryRepository;
import com.paulo.helpdesk_api_java.repositories.TicketRepository;
import com.paulo.helpdesk_api_java.repositories.UserRepository;
import com.paulo.helpdesk_api_java.repositories.specifications.TicketSpecifications;
import com.paulo.helpdesk_api_java.services.exceptions.BusinessRuleException;
import com.paulo.helpdesk_api_java.services.exceptions.ResourceConflictException;
import com.paulo.helpdesk_api_java.services.exceptions.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class TicketService {

    private static final Map<TicketStatus, Set<TicketStatus>> ALLOWED_TRANSITIONS = Map.of(
            TicketStatus.OPEN, Set.of(TicketStatus.IN_PROGRESS, TicketStatus.CANCELED),
            TicketStatus.IN_PROGRESS, Set.of(TicketStatus.WAITING_CLIENT, TicketStatus.RESOLVED, TicketStatus.CANCELED),
            TicketStatus.WAITING_CLIENT, Set.of(TicketStatus.IN_PROGRESS, TicketStatus.RESOLVED, TicketStatus.CANCELED),
            TicketStatus.RESOLVED, Set.of(TicketStatus.CLOSED));

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "id", "title", "status", "priority", "createdAt", "updatedAt", "closedAt");

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final TicketCommentRepository commentRepository;
    private final TicketHistoryRepository historyRepository;

    public TicketService(
            TicketRepository ticketRepository,
            UserRepository userRepository,
            TicketCommentRepository commentRepository,
            TicketHistoryRepository historyRepository) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.commentRepository = commentRepository;
        this.historyRepository = historyRepository;
    }

    @Transactional
    public TicketResponseDTO insert(TicketCreateDTO dto, User client) {
        if (client.getRole() != UserRoles.ROLE_USER) {
            throw new BusinessRuleException(
                    "TICKET_CLIENT_INVALID_ROLE",
                    "Somente usuários com o perfil ROLE_USER podem abrir tickets.");
        }

        Ticket ticket = new Ticket();
        ticket.setTitle(dto.title());
        ticket.setDescription(dto.description());
        ticket.setPriority(dto.priority());
        ticket.setStatus(TicketStatus.OPEN);
        ticket.setClient(client);

        Ticket savedTicket = ticketRepository.save(ticket);
        recordHistory(savedTicket, client, TicketHistoryType.CREATED, null, TicketStatus.OPEN,
                "Ticket criado.");
        return new TicketResponseDTO(savedTicket);
    }

    @Transactional(readOnly = true)
    public PageResponseDTO<TicketResponseDTO> findAll(
            User authenticatedUser,
            TicketStatus status,
            TicketPriority priority,
            Long clientId,
            Long attendantId,
            String search,
            Pageable pageable) {
        validateSort(pageable);

        Long effectiveClientId = authenticatedUser.getRole() == UserRoles.ROLE_ADMIN
                ? clientId
                : authenticatedUser.getId();

        Page<Ticket> tickets = ticketRepository.findAll(
                TicketSpecifications.withFilters(
                        status, priority, effectiveClientId, attendantId, search),
                pageable);

        return PageResponseDTO.from(tickets, TicketResponseDTO::new);
    }

    @Transactional(readOnly = true)
    public TicketResponseDTO findById(Long id, User authenticatedUser) {
        Ticket ticket = findTicket(id);
        checkAccess(ticket, authenticatedUser);
        return new TicketResponseDTO(ticket);
    }

    @Transactional
    public TicketResponseDTO assignAttendant(Long ticketId, TicketAssignDTO dto, User actor) {
        Ticket ticket = findTicket(ticketId);
        User attendant = userRepository.findById(dto.attendantId())
                .orElseThrow(() -> new ResourceNotFoundException("Atendente", dto.attendantId()));

        if (attendant.getRole() != UserRoles.ROLE_ADMIN) {
            throw new BusinessRuleException(
                    "TICKET_ATTENDANT_INVALID_ROLE",
                    "Somente usuários com o perfil ROLE_ADMIN podem atender tickets.");
        }
        if (ticket.getStatus() == TicketStatus.CLOSED || ticket.getStatus() == TicketStatus.CANCELED) {
            throw new BusinessRuleException(
                    "TICKET_NOT_ASSIGNABLE",
                    "Tickets fechados ou cancelados não podem receber um atendente.");
        }
        if (ticket.getAttendant() != null) {
            throw new ResourceConflictException(
                    "TICKET_ALREADY_ASSIGNED",
                    "O ticket já possui um atendente atribuído.");
        }

        TicketStatus previousStatus = ticket.getStatus();
        ticket.setAttendant(attendant);
        ticket.setStatus(TicketStatus.IN_PROGRESS);
        Ticket savedTicket = ticketRepository.save(ticket);
        recordHistory(savedTicket, actor, TicketHistoryType.ASSIGNED, previousStatus, TicketStatus.IN_PROGRESS,
                "Atendente %s atribuído ao ticket.".formatted(attendant.getName()));
        return new TicketResponseDTO(savedTicket);
    }

    @Transactional
    public TicketResponseDTO updateStatus(
            Long ticketId, TicketStatusUpdateDTO dto, User actor) {
        Ticket ticket = findTicket(ticketId);
        checkAccess(ticket, actor);
        TicketStatus currentStatus = ticket.getStatus();
        TicketStatus newStatus = dto.status();

        if (currentStatus == newStatus) {
            throw new ResourceConflictException(
                    "TICKET_STATUS_UNCHANGED",
                    "O ticket já está com o status informado.");
        }
        if (!ALLOWED_TRANSITIONS.getOrDefault(currentStatus, Set.of()).contains(newStatus)) {
            throw new BusinessRuleException(
                    "TICKET_STATUS_TRANSITION_NOT_ALLOWED",
                    "A transição de %s para %s não é permitida.".formatted(currentStatus, newStatus));
        }
        validateStatusPermission(ticket, actor, newStatus);

        ticket.setStatus(newStatus);
        if (newStatus == TicketStatus.CLOSED || newStatus == TicketStatus.CANCELED) {
            ticket.setClosedAt(Instant.now());
        }

        Ticket savedTicket = ticketRepository.save(ticket);
        recordHistory(savedTicket, actor, TicketHistoryType.STATUS_CHANGED, currentStatus, newStatus,
                "Status alterado de %s para %s.".formatted(currentStatus, newStatus));
        return new TicketResponseDTO(savedTicket);
    }

    @Transactional
    public TicketResponseDTO reopen(Long ticketId, User actor) {
        Ticket ticket = findTicket(ticketId);
        checkAccess(ticket, actor);
        TicketStatus previousStatus = ticket.getStatus();

        if (previousStatus != TicketStatus.RESOLVED
                && previousStatus != TicketStatus.CLOSED
                && previousStatus != TicketStatus.CANCELED) {
            throw new BusinessRuleException(
                    "TICKET_CANNOT_BE_REOPENED",
                    "Somente tickets resolvidos, fechados ou cancelados podem ser reabertos.");
        }

        ticket.setStatus(TicketStatus.OPEN);
        ticket.setClosedAt(null);
        Ticket savedTicket = ticketRepository.save(ticket);
        recordHistory(savedTicket, actor, TicketHistoryType.REOPENED, previousStatus, TicketStatus.OPEN,
                "Ticket reaberto.");
        return new TicketResponseDTO(savedTicket);
    }

    @Transactional
    public TicketCommentResponseDTO addComment(
            Long ticketId, TicketCommentCreateDTO dto, User author) {
        Ticket ticket = findTicket(ticketId);
        checkAccess(ticket, author);

        TicketComment comment = new TicketComment();
        comment.setTicket(ticket);
        comment.setAuthor(author);
        comment.setContent(dto.content().trim());
        TicketComment savedComment = commentRepository.save(comment);
        recordHistory(ticket, author, TicketHistoryType.COMMENT_ADDED, null, null,
                "Novo comentário adicionado.");
        return new TicketCommentResponseDTO(savedComment);
    }

    @Transactional(readOnly = true)
    public List<TicketCommentResponseDTO> findComments(Long ticketId, User authenticatedUser) {
        Ticket ticket = findTicket(ticketId);
        checkAccess(ticket, authenticatedUser);
        return commentRepository.findAllByTicketIdOrderByCreatedAtAsc(ticketId)
                .stream()
                .map(TicketCommentResponseDTO::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TicketHistoryResponseDTO> findHistory(Long ticketId, User authenticatedUser) {
        Ticket ticket = findTicket(ticketId);
        checkAccess(ticket, authenticatedUser);
        return historyRepository.findAllByTicketIdOrderByCreatedAtAsc(ticketId)
                .stream()
                .map(TicketHistoryResponseDTO::new)
                .toList();
    }

    private Ticket findTicket(Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", id));
    }

    private void checkAccess(Ticket ticket, User user) {
        boolean isAdmin = user.getRole() == UserRoles.ROLE_ADMIN;
        boolean isOwner = ticket.getClient() != null
                && ticket.getClient().getId().equals(user.getId());
        if (!isAdmin && !isOwner) {
            throw new AccessDeniedException("O usuário não possui acesso a este ticket.");
        }
    }

    private void validateStatusPermission(Ticket ticket, User actor, TicketStatus newStatus) {
        boolean isAdmin = actor.getRole() == UserRoles.ROLE_ADMIN;

        if (newStatus == TicketStatus.CLOSED) {
            return;
        }
        if (newStatus == TicketStatus.CANCELED && (isAdmin || ticket.getStatus() == TicketStatus.OPEN)) {
            return;
        }
        if (!isAdmin) {
            throw new AccessDeniedException("Somente administradores podem realizar esta transição de status.");
        }
        if (newStatus == TicketStatus.IN_PROGRESS && ticket.getAttendant() == null) {
            throw new BusinessRuleException(
                    "TICKET_REQUIRES_ATTENDANT",
                    "O ticket precisa possuir um atendente antes de entrar em andamento.");
        }
    }

    private void validateSort(Pageable pageable) {
        pageable.getSort().forEach(order -> {
            if (!ALLOWED_SORT_FIELDS.contains(order.getProperty())) {
                throw new BusinessRuleException(
                        "INVALID_SORT_FIELD",
                        "O campo '%s' não pode ser usado para ordenação.".formatted(order.getProperty()));
            }
        });
    }

    private void recordHistory(
            Ticket ticket,
            User actor,
            TicketHistoryType type,
            TicketStatus previousStatus,
            TicketStatus newStatus,
            String description) {
        TicketHistory history = new TicketHistory();
        history.setTicket(ticket);
        history.setActor(actor);
        history.setType(type);
        history.setPreviousStatus(previousStatus);
        history.setNewStatus(newStatus);
        history.setDescription(description);
        historyRepository.save(history);
    }
}
