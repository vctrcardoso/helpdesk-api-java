package com.paulo.helpdesk_api_java.resources;

import com.paulo.helpdesk_api_java.dto.ticket.TicketAssignDTO;
import com.paulo.helpdesk_api_java.dto.ticket.TicketCreateDTO;
import com.paulo.helpdesk_api_java.dto.ticket.TicketResponseDTO;
import com.paulo.helpdesk_api_java.services.TicketService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tickets")
public class TicketResource {

    private final TicketService service;

    public TicketResource(TicketService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<TicketResponseDTO> insert(@RequestBody @Valid TicketCreateDTO dto) {
        TicketResponseDTO response = service.insert(dto);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<TicketResponseDTO>> findAll() {
        List<TicketResponseDTO> response = service.findAll();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TicketResponseDTO> findById(@PathVariable Long id) {
        TicketResponseDTO response = service.findById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{ticketId}/assign")
    public ResponseEntity<TicketResponseDTO> assignAttendant(
            @PathVariable Long ticketId,
            @RequestBody @Valid TicketAssignDTO dto
    ) {
        TicketResponseDTO response = service.assignAttendant(ticketId, dto);
        return ResponseEntity.ok(response);
    }
}