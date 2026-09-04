package com.vamo.pos.identity.api.user;

import java.net.URI;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.vamo.pos.identity.application.CreateUserCommand;
import com.vamo.pos.identity.application.UserApplicationService;

import jakarta.validation.Valid;

/**
 * Expone los casos de uso relacionados con usuarios mediante HTTP.
 */
@RestController
@RequestMapping("/api/v1/tenants/{tenantId}/users")
public class UserController {
    private final UserApplicationService userApplicationService;

    public UserController(UserApplicationService userApplicationService) {
        this.userApplicationService = userApplicationService;
    }

    /**
     * Registra un usuario dentro del tenant indicado por la URL.
     */
    @PostMapping
    public ResponseEntity<CreateUserResponse> createUser(
        @PathVariable("tenantId") UUID tenantId,
        @Valid @RequestBody CreateUserRequest request
    ) {
        UUID userId = userApplicationService.createUser(
            new CreateUserCommand(
                tenantId,
                request.defaultBranchId(),
                request.username(),
                request.displayName(),
                request.password(),
                null
            )
        );

        URI location = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/{userId}")
            .buildAndExpand(userId)
            .toUri();

        return ResponseEntity
            .created(location)
            .body(new CreateUserResponse(userId));
    }
}
