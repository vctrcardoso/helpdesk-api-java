package com.paulo.helpdesk_api_java.dto.ticket;

import com.paulo.helpdesk_api_java.entities.Ticket;
import com.paulo.helpdesk_api_java.entities.enums.TicketPriority;
import com.paulo.helpdesk_api_java.entities.enums.TicketStatus;

import java.time.Instant;

public record TicketResponseDTO(
        Long id,
        String title,
        String description,
        TicketStatus status,
        TicketPriority priority,
        Instant createdAt,

        Long clientId,
        String clientName,

        Long attendantId,
        String attendantName
) {

    public TicketResponseDTO(Ticket ticket) {
        this(
                ticket.getId(),
                ticket.getTitle(),
                ticket.getDescription(),
                ticket.getStatus(),
                ticket.getPriority(),
                ticket.getCreatedAt(),

                ticket.getClient() != null ? ticket.getClient().getId() : null,
                ticket.getClient() != null ? ticket.getClient().getName() : null,

                ticket.getAttendant() != null ? ticket.getAttendant().getId() : null,
                ticket.getAttendant() != null ? ticket.getAttendant().getName() : null
        );
    }
}
