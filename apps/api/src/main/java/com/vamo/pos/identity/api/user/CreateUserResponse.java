package com.vamo.pos.identity.api.user;

import java.util.UUID;

/**
 * Respuesta segura después de registrar un usuario.
 * No contiene contraseña ni passwordHash.
 */
public record CreateUserResponse(
    UUID id
) {
}
