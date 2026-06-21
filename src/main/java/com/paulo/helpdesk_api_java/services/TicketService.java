package com.paulo.helpdesk_api_java.services;

import com.paulo.helpdesk_api_java.dto.ticket.TicketAssignDTO;
import com.paulo.helpdesk_api_java.dto.ticket.TicketCreateDTO;
import com.paulo.helpdesk_api_java.dto.ticket.TicketResponseDTO;
import com.paulo.helpdesk_api_java.entities.Ticket;
import com.paulo.helpdesk_api_java.entities.User;
import com.paulo.helpdesk_api_java.entities.enums.TicketStatus;
import com.paulo.helpdesk_api_java.entities.enums.UserRoles;
import com.paulo.helpdesk_api_java.repositories.TicketRepository;
import com.paulo.helpdesk_api_java.repositories.UserRepository;
import com.paulo.helpdesk_api_java.services.exceptions.BusinessRuleException;
import com.paulo.helpdesk_api_java.services.exceptions.ResourceConflictException;
import com.paulo.helpdesk_api_java.services.exceptions.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;

    public TicketService(TicketRepository ticketRepository, UserRepository userRepository) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
    }

    public TicketResponseDTO insert(TicketCreateDTO dto) {
        User client = userRepository.findById(dto.clientId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", dto.clientId()));

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

        return new TicketResponseDTO(savedTicket);
    }

    public List<TicketResponseDTO> findAll() {
        return ticketRepository.findAll()
                .stream()
                .map(TicketResponseDTO::new)
                .toList();
    }

    public TicketResponseDTO findById(Long id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", id));

        return new TicketResponseDTO(ticket);
    }

    public TicketResponseDTO assignAttendant(Long ticketId, TicketAssignDTO dto) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", ticketId));

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

        ticket.setAttendant(attendant);
        ticket.setStatus(TicketStatus.IN_PROGRESS);

        Ticket savedTicket = ticketRepository.save(ticket);

        return new TicketResponseDTO(savedTicket);
    }
}
