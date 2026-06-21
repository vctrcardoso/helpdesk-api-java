package com.paulo.helpdesk_api_java.dto.ticket;

import com.paulo.helpdesk_api_java.entities.TicketHistory;
import com.paulo.helpdesk_api_java.entities.enums.TicketHistoryType;
import com.paulo.helpdesk_api_java.entities.enums.TicketStatus;

import java.time.Instant;

public record TicketHistoryResponseDTO(
        Long id,
        TicketHistoryType type,
        Long actorId,
        String actorName,
        TicketStatus previousStatus,
        TicketStatus newStatus,
        String description,
        Instant createdAt
) {
    public TicketHistoryResponseDTO(TicketHistory history) {
        this(
                history.getId(),
                history.getType(),
                history.getActor().getId(),
                history.getActor().getName(),
                history.getPreviousStatus(),
                history.getNewStatus(),
                history.getDescription(),
                history.getCreatedAt());
    }
}
