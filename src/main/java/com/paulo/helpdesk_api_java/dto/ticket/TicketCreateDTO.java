package com.paulo.helpdesk_api_java.dto.ticket;

import com.paulo.helpdesk_api_java.entities.enums.TicketPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TicketCreateDTO(

        @NotBlank(message = "O título é obrigatório")
        @Size(min = 3, max = 120, message = "O título deve ter entre 3 e 120 caracteres")
        String title,

        @NotBlank(message = "A descrição é obrigatória")
        @Size(min = 10, max = 1000, message = "A descrição deve ter entre 10 e 1000 caracteres")
        String description,

        @NotNull(message = "A prioridade é obrigatória")
        TicketPriority priority

) {
}
