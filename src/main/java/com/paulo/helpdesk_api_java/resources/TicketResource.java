package com.paulo.helpdesk_api_java.resources;

import com.paulo.helpdesk_api_java.dto.ticket.TicketAssignDTO;
import com.paulo.helpdesk_api_java.dto.ticket.TicketCreateDTO;
import com.paulo.helpdesk_api_java.dto.ticket.TicketResponseDTO;
import com.paulo.helpdesk_api_java.services.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tickets")
@Tag(name = "Tickets", description = "Abertura, consulta e atribuição de tickets")
public class TicketResource {

    private final TicketService service;

    public TicketResource(TicketService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Abrir ticket")
    public ResponseEntity<TicketResponseDTO> insert(@RequestBody @Valid TicketCreateDTO dto) {
        TicketResponseDTO response = service.insert(dto);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Listar tickets")
    public ResponseEntity<List<TicketResponseDTO>> findAll() {
        List<TicketResponseDTO> response = service.findAll();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar ticket por ID")
    public ResponseEntity<TicketResponseDTO> findById(@PathVariable Long id) {
        TicketResponseDTO response = service.findById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{ticketId}/assign")
    @Operation(summary = "Atribuir atendente ao ticket")
    public ResponseEntity<TicketResponseDTO> assignAttendant(
            @PathVariable Long ticketId,
            @RequestBody @Valid TicketAssignDTO dto
    ) {
        TicketResponseDTO response = service.assignAttendant(ticketId, dto);
        return ResponseEntity.ok(response);
    }
}
