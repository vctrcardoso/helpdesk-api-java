package com.paulo.helpdesk_api_java.repositories;

import com.paulo.helpdesk_api_java.entities.TicketComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketCommentRepository extends JpaRepository<TicketComment, Long> {

    List<TicketComment> findAllByTicketIdOrderByCreatedAtAsc(Long ticketId);
}
