package com.paulo.helpdesk_api_java.dto.ticket;

import com.paulo.helpdesk_api_java.entities.enums.TicketStatus;
import jakarta.validation.constraints.NotNull;

public record TicketStatusUpdateDTO(

        @NotNull(message = "O status é obrigatório")
        TicketStatus status

) {
}