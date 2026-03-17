package com.felipelima.clientmanager.controller;

import com.felipelima.clientmanager.dto.request.ClientRequest;
import com.felipelima.clientmanager.dto.response.ClientResponse;
import com.felipelima.clientmanager.service.ClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * REST Controller for Client CRUD operations.
 * 
 * Authorization is handled by SecurityConfig:
 * - GET endpoints: any authenticated user (ADMIN or USER)
 * - POST/PUT/DELETE: ADMIN only
 * 
 */
@RestController
@RequestMapping("/clients")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;

    /**
     * POST /clients
     * Creates a new client. ADMIN only.
     * Returns 201 Created with the created client data.
     */
    @PostMapping
    public ResponseEntity<ClientResponse> create(@Valid @RequestBody ClientRequest request) {
        ClientResponse response = clientService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /clients
     * Lists all clients. Any authenticated user.
     */
    @GetMapping
    public ResponseEntity<List<ClientResponse>> findAll() {
        return ResponseEntity.ok(clientService.findAll());
    }

    /**
     * GET /clients/{id}
     * Finds a client by ID. Any authenticated user.
     * Returns 404 if not found.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ClientResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(clientService.findById(id));
    }

    /**
     * PUT /clients/{id}
     * Updates an existing client. ADMIN only.
     * Returns 404 if not found.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ClientResponse> update(@PathVariable Long id,
                                                  @Valid @RequestBody ClientRequest request) {
        return ResponseEntity.ok(clientService.update(id, request));
    }

    /**
     * DELETE /clients/{id}
     * Deletes a client. ADMIN only.
     * Returns 204 No Content on success, 404 if not found.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        clientService.delete(id);
        return ResponseEntity.noContent().build();
    }
}