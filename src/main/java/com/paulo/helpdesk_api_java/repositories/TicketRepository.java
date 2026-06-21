package com.paulo.helpdesk_api_java.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.paulo.helpdesk_api_java.entities.Ticket;

import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    List<Ticket> findAllByClientId(Long clientId);
}
