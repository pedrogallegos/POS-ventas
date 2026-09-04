package com.vamo.pos.identity.api.user;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * JSON recibido para registrar un usuario.
 *
 * tenantId no aparece aquí porque se obtiene directamente de la URL.
 * createdBy tampoco se acepta desde el cliente para impedir que alguien
 * falsifique quién realizó la operación.
 */
public record CreateUserRequest(

        @NotNull(message = "La sucursal predeterminada es obligatoria.")
        UUID defaultBranchId,

        @NotBlank(message = "El nombre de usuario es obligatorio.")
        @Size(
                min = 3,
                max = 80,
                message = "El nombre de usuario debe contener entre 3 y 80 caracteres."
        )
        String username,

        @NotBlank(message = "El nombre visible es obligatorio.")
        @Size(
                min = 2,
                max = 120,
                message = "El nombre visible debe contener entre 2 y 120 caracteres."
        )
        String displayName,

        @NotBlank(message = "La contraseña es obligatoria.")
        @Size(
                min = 8,
                max = 72,
                message = "La contraseña debe contener entre 8 y 72 caracteres."
        )
        String password
) {
}