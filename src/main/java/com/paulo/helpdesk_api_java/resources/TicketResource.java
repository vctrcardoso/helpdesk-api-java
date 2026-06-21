package com.paulo.helpdesk_api_java.resources;

import com.paulo.helpdesk_api_java.config.ApiPaths;
import com.paulo.helpdesk_api_java.dto.ticket.TicketAssignDTO;
import com.paulo.helpdesk_api_java.dto.ticket.TicketCreateDTO;
import com.paulo.helpdesk_api_java.dto.ticket.TicketResponseDTO;
import com.paulo.helpdesk_api_java.entities.User;
import com.paulo.helpdesk_api_java.services.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(ApiPaths.TICKETS)
@Tag(name = "Tickets", description = "Abertura, consulta e atribuição de tickets")
public class TicketResource {

    private final TicketService service;

    public TicketResource(TicketService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Abrir ticket")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<TicketResponseDTO> insert(
            @RequestBody @Valid TicketCreateDTO dto,
            @AuthenticationPrincipal User authenticatedUser) {
        TicketResponseDTO response = service.insert(dto, authenticatedUser);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Listar tickets")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<TicketResponseDTO>> findAll(
            @AuthenticationPrincipal User authenticatedUser) {
        List<TicketResponseDTO> response = service.findAll(authenticatedUser);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar ticket por ID")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TicketResponseDTO> findById(
            @PathVariable Long id,
            @AuthenticationPrincipal User authenticatedUser) {
        TicketResponseDTO response = service.findById(id, authenticatedUser);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{ticketId}/assign")
    @Operation(summary = "Atribuir atendente ao ticket")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TicketResponseDTO> assignAttendant(
            @PathVariable Long ticketId,
            @RequestBody @Valid TicketAssignDTO dto
    ) {
        TicketResponseDTO response = service.assignAttendant(ticketId, dto);
        return ResponseEntity.ok(response);
    }
}
