package com.paulo.helpdesk_api_java.dto.ticket;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TicketCommentCreateDTO(
        @NotBlank(message = "O comentário é obrigatório")
        @Size(max = 2000, message = "O comentário deve possuir no máximo 2000 caracteres")
        String content
) {
}
