package com.paulo.helpdesk_api_java.dto.ticket;

import jakarta.validation.constraints.NotNull;

public record TicketAssignDTO(

        @NotNull(message = "O atendente é obrigatório")
        Long attendantId

) {
}