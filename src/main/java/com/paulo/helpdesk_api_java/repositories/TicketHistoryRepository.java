package com.paulo.helpdesk_api_java.repositories;

import com.paulo.helpdesk_api_java.entities.TicketHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketHistoryRepository extends JpaRepository<TicketHistory, Long> {

    List<TicketHistory> findAllByTicketIdOrderByCreatedAtAsc(Long ticketId);
}
