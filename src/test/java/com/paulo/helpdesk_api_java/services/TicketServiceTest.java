package com.paulo.helpdesk_api_java.services;

import com.paulo.helpdesk_api_java.dto.ticket.TicketAssignDTO;
import com.paulo.helpdesk_api_java.dto.ticket.TicketCreateDTO;
import com.paulo.helpdesk_api_java.entities.Ticket;
import com.paulo.helpdesk_api_java.entities.User;
import com.paulo.helpdesk_api_java.entities.enums.TicketPriority;
import com.paulo.helpdesk_api_java.entities.enums.TicketStatus;
import com.paulo.helpdesk_api_java.entities.enums.UserRoles;
import com.paulo.helpdesk_api_java.repositories.TicketRepository;
import com.paulo.helpdesk_api_java.repositories.UserRepository;
import com.paulo.helpdesk_api_java.services.exceptions.BusinessRuleException;
import com.paulo.helpdesk_api_java.services.exceptions.ResourceConflictException;
import com.paulo.helpdesk_api_java.services.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TicketServiceTest {

    private TicketRepository ticketRepository;
    private UserRepository userRepository;
    private TicketService service;

    @BeforeEach
    void setUp() {
        ticketRepository = mock(TicketRepository.class);
        userRepository = mock(UserRepository.class);
        service = new TicketService(ticketRepository, userRepository);
    }

    @Test
    void shouldRejectTicketCreationWhenClientDoesNotHaveUserRole() {
        User admin = user(1L, UserRoles.ROLE_ADMIN);
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));

        TicketCreateDTO dto = new TicketCreateDTO(
                "Falha no sistema",
                "Descrição detalhada da falha",
                TicketPriority.HIGH,
                1L);

        BusinessRuleException exception =
                assertThrows(BusinessRuleException.class, () -> service.insert(dto));

        assertEquals("TICKET_CLIENT_INVALID_ROLE", exception.getCode());
    }

    @Test
    void shouldReturnSpecificNotFoundErrorWhenTicketDoesNotExist() {
        when(ticketRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(ResourceNotFoundException.class, () -> service.findById(99L));

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
                () -> service.assignAttendant(10L, new TicketAssignDTO(3L)));

        assertEquals("TICKET_ALREADY_ASSIGNED", exception.getCode());
    }

    private User user(Long id, UserRoles role) {
        return new User(id, "Usuário", "user%d@example.com".formatted(id), "secret", role);
    }
}
