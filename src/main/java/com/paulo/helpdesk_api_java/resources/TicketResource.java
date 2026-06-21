package com.paulo.helpdesk_api_java.resources;

import com.paulo.helpdesk_api_java.config.ApiPaths;
import com.paulo.helpdesk_api_java.dto.PageResponseDTO;
import com.paulo.helpdesk_api_java.dto.ticket.TicketAssignDTO;
import com.paulo.helpdesk_api_java.dto.ticket.TicketCommentCreateDTO;
import com.paulo.helpdesk_api_java.dto.ticket.TicketCommentResponseDTO;
import com.paulo.helpdesk_api_java.dto.ticket.TicketCreateDTO;
import com.paulo.helpdesk_api_java.dto.ticket.TicketHistoryResponseDTO;
import com.paulo.helpdesk_api_java.dto.ticket.TicketResponseDTO;
import com.paulo.helpdesk_api_java.dto.ticket.TicketStatusUpdateDTO;
import com.paulo.helpdesk_api_java.entities.User;
import com.paulo.helpdesk_api_java.entities.enums.TicketPriority;
import com.paulo.helpdesk_api_java.entities.enums.TicketStatus;
import com.paulo.helpdesk_api_java.services.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
    @Operation(summary = "Listar e filtrar tickets")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PageResponseDTO<TicketResponseDTO>> findAll(
            @RequestParam(required = false) TicketStatus status,
            @RequestParam(required = false) TicketPriority priority,
            @RequestParam(required = false) Long clientId,
            @RequestParam(required = false) Long attendantId,
            @RequestParam(required = false) String search,
            @ParameterObject
            @PageableDefault(size = 20, sort = "createdAt")
            Pageable pageable,
            @AuthenticationPrincipal User authenticatedUser) {
        PageResponseDTO<TicketResponseDTO> response = service.findAll(
                authenticatedUser, status, priority, clientId, attendantId, search, pageable);
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
            @RequestBody @Valid TicketAssignDTO dto,
            @AuthenticationPrincipal User authenticatedUser
    ) {
        TicketResponseDTO response = service.assignAttendant(ticketId, dto, authenticatedUser);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{ticketId}/status")
    @Operation(summary = "Atualizar status do ticket")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TicketResponseDTO> updateStatus(
            @PathVariable Long ticketId,
            @RequestBody @Valid TicketStatusUpdateDTO dto,
            @AuthenticationPrincipal User authenticatedUser) {
        return ResponseEntity.ok(service.updateStatus(ticketId, dto, authenticatedUser));
    }

    @PostMapping("/{ticketId}/reopen")
    @Operation(summary = "Reabrir ticket")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TicketResponseDTO> reopen(
            @PathVariable Long ticketId,
            @AuthenticationPrincipal User authenticatedUser) {
        return ResponseEntity.ok(service.reopen(ticketId, authenticatedUser));
    }

    @PostMapping("/{ticketId}/comments")
    @Operation(summary = "Adicionar comentário")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TicketCommentResponseDTO> addComment(
            @PathVariable Long ticketId,
            @RequestBody @Valid TicketCommentCreateDTO dto,
            @AuthenticationPrincipal User authenticatedUser) {
        return ResponseEntity.ok(service.addComment(ticketId, dto, authenticatedUser));
    }

    @GetMapping("/{ticketId}/comments")
    @Operation(summary = "Listar comentários")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<TicketCommentResponseDTO>> findComments(
            @PathVariable Long ticketId,
            @AuthenticationPrincipal User authenticatedUser) {
        return ResponseEntity.ok(service.findComments(ticketId, authenticatedUser));
    }

    @GetMapping("/{ticketId}/history")
    @Operation(summary = "Consultar histórico do ticket")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<TicketHistoryResponseDTO>> findHistory(
            @PathVariable Long ticketId,
            @AuthenticationPrincipal User authenticatedUser) {
        return ResponseEntity.ok(service.findHistory(ticketId, authenticatedUser));
    }
}
