package com.paulo.helpdesk_api_java.services;

import com.paulo.helpdesk_api_java.dto.ticket.TicketAssignDTO;
import com.paulo.helpdesk_api_java.dto.ticket.TicketCreateDTO;
import com.paulo.helpdesk_api_java.dto.ticket.TicketStatusUpdateDTO;
import com.paulo.helpdesk_api_java.entities.Ticket;
import com.paulo.helpdesk_api_java.entities.User;
import com.paulo.helpdesk_api_java.entities.enums.TicketPriority;
import com.paulo.helpdesk_api_java.entities.enums.TicketStatus;
import com.paulo.helpdesk_api_java.entities.enums.UserRoles;
import com.paulo.helpdesk_api_java.repositories.TicketCommentRepository;
import com.paulo.helpdesk_api_java.repositories.TicketHistoryRepository;
import com.paulo.helpdesk_api_java.repositories.TicketRepository;
import com.paulo.helpdesk_api_java.repositories.UserRepository;
import com.paulo.helpdesk_api_java.services.exceptions.BusinessRuleException;
import com.paulo.helpdesk_api_java.services.exceptions.ResourceConflictException;
import com.paulo.helpdesk_api_java.services.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TicketServiceTest {

    private TicketRepository ticketRepository;
    private UserRepository userRepository;
    private TicketCommentRepository commentRepository;
    private TicketHistoryRepository historyRepository;
    private TicketService service;

    @BeforeEach
    void setUp() {
        ticketRepository = mock(TicketRepository.class);
        userRepository = mock(UserRepository.class);
        commentRepository = mock(TicketCommentRepository.class);
        historyRepository = mock(TicketHistoryRepository.class);
        service = new TicketService(
                ticketRepository, userRepository, commentRepository, historyRepository);
    }

    @Test
    void shouldRejectTicketCreationWhenClientDoesNotHaveUserRole() {
        User admin = user(1L, UserRoles.ROLE_ADMIN);
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));

        TicketCreateDTO dto = new TicketCreateDTO(
                "Falha no sistema",
                "Descrição detalhada da falha",
                TicketPriority.HIGH);

        BusinessRuleException exception =
                assertThrows(BusinessRuleException.class, () -> service.insert(dto, admin));

        assertEquals("TICKET_CLIENT_INVALID_ROLE", exception.getCode());
    }

    @Test
    void shouldReturnSpecificNotFoundErrorWhenTicketDoesNotExist() {
        when(ticketRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(ResourceNotFoundException.class, () -> service.findById(99L, user(1L, UserRoles.ROLE_USER)));

        assertEquals("Ticket não encontrado(a) com identificador: 99", exception.getMessage());
    }

    @Test
    void shouldRejectAssigningAnAlreadyAssignedTicket() {
        User currentAttendant = user(2L, UserRoles.ROLE_ADMIN);
        User newAttendant = user(3L, UserRoles.ROLE_ADMIN);
        Ticket ticket = new Ticket();
        ticket.setStatus(TicketStatus.IN_PROGRESS);
        ticket.setAttendant(currentAttendant);

        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));
        when(userRepository.findById(3L)).thenReturn(Optional.of(newAttendant));

        ResourceConflictException exception = assertThrows(
                ResourceConflictException.class,
                () -> service.assignAttendant(10L, new TicketAssignDTO(3L), currentAttendant));

        assertEquals("TICKET_ALREADY_ASSIGNED", exception.getCode());
    }

    @Test
    void regularUserMustOnlyListOwnTickets() {
        User client = user(4L, UserRoles.ROLE_USER);
        Ticket ticket = new Ticket();
        ticket.setClient(client);
        PageRequest pageable = PageRequest.of(0, 20);
        when(ticketRepository.findAll(
                org.mockito.ArgumentMatchers.<Specification<Ticket>>any(),
                org.mockito.ArgumentMatchers.eq(pageable)))
                .thenReturn(new PageImpl<>(java.util.List.of(ticket), pageable, 1));

        assertEquals(1, service.findAll(
                client, null, null, 999L, null, null, pageable).content().size());
    }

    @Test
    void regularUserCannotReadAnotherClientsTicket() {
        User owner = user(4L, UserRoles.ROLE_USER);
        User anotherUser = user(5L, UserRoles.ROLE_USER);
        Ticket ticket = new Ticket();
        ticket.setClient(owner);
        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));

        assertTrue(assertThrows(
                org.springframework.security.access.AccessDeniedException.class,
                () -> service.findById(10L, anotherUser)).getMessage().contains("não possui acesso"));
    }

    @Test
    void adminCanMoveAssignedTicketFromOpenToInProgress() {
        User admin = user(2L, UserRoles.ROLE_ADMIN);
        Ticket ticket = new Ticket();
        ticket.setStatus(TicketStatus.OPEN);
        ticket.setAttendant(admin);
        ticket.setClient(user(4L, UserRoles.ROLE_USER));
        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(ticket)).thenReturn(ticket);

        service.updateStatus(
                10L, new TicketStatusUpdateDTO(TicketStatus.IN_PROGRESS), admin);

        assertEquals(TicketStatus.IN_PROGRESS, ticket.getStatus());
        verify(historyRepository).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void invalidStatusTransitionMustBeRejected() {
        User admin = user(2L, UserRoles.ROLE_ADMIN);
        Ticket ticket = new Ticket();
        ticket.setStatus(TicketStatus.OPEN);
        ticket.setClient(user(4L, UserRoles.ROLE_USER));
        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));

        BusinessRuleException exception = assertThrows(
                BusinessRuleException.class,
                () -> service.updateStatus(
                        10L, new TicketStatusUpdateDTO(TicketStatus.CLOSED), admin));

        assertEquals("TICKET_STATUS_TRANSITION_NOT_ALLOWED", exception.getCode());
    }

    @Test
    void closedTicketCanBeReopened() {
        User owner = user(4L, UserRoles.ROLE_USER);
        Ticket ticket = new Ticket();
        ticket.setStatus(TicketStatus.CLOSED);
        ticket.setClient(owner);
        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(ticket)).thenReturn(ticket);

        service.reopen(10L, owner);

        assertEquals(TicketStatus.OPEN, ticket.getStatus());
        verify(historyRepository).save(org.mockito.ArgumentMatchers.any());
    }

    private User user(Long id, UserRoles role) {
        return new User(id, "Usuário", "user%d@example.com".formatted(id), "secret", role);
    }
}
