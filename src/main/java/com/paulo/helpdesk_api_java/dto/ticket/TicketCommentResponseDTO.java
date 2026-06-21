package com.paulo.helpdesk_api_java.dto.ticket;

import com.paulo.helpdesk_api_java.entities.TicketComment;

import java.time.Instant;

public record TicketCommentResponseDTO(
        Long id,
        Long authorId,
        String authorName,
        String content,
        Instant createdAt
) {
    public TicketCommentResponseDTO(TicketComment comment) {
        this(
                comment.getId(),
                comment.getAuthor().getId(),
                comment.getAuthor().getName(),
                comment.getContent(),
                comment.getCreatedAt());
    }
}
