package com.vamo.pos.identity.application;

import java.util.Objects;
import java.util.UUID;

/**
 * Datos necesarios para solicitar la creación de un usuario.
 * rawPassword existe únicamente durante esta operación y nunca debe almacenarse
 * directamente.
 */
public record CreateUserCommand(
                UUID tenantId,
                UUID defaultBranchId,
                String username,
                String displayName,
                String rawPassword,
                UUID createdBy) {
        /**
         * Valida los datos antes de que lleguen al servicio.
         */
        public CreateUserCommand {
                tenantId = Objects.requireNonNull(tenantId, "El tenantId es obligatorio.");

                defaultBranchId = Objects.requireNonNull(defaultBranchId,
                                "La sucursal predeterminada es obligatoria.");

                username = requireText(
                                username,
                                "El nombre de usuario",
                                3,
                                80);

                displayName = requireText(
                                displayName,
                                "El nombre visible",
                                2,
                                120);

                rawPassword = requirePassword(rawPassword);
        }

        /**
         * Elimina espacios exteriores y valida textos obligatorios.
         */

        private static String requireText(
                        String value,
                        String fieldName,
                        int minimumLength,
                        int maximumLength) {
                String normalizedValue = Objects.requireNonNull(value, fieldName + " es obligatorio.").strip();

                if (normalizedValue.length() < minimumLength || normalizedValue.length() > maximumLength) {
                        throw new IllegalArgumentException(fieldName + " debe contener entre " +
                                        minimumLength + " y " + maximumLength + " caracteres.");
                }
                return normalizedValue;
        }

        /**
         * Conserva los caracteres originales de la contraseña.
         * No usamos strip porque los espacios pueden formar parte de ella.
         */
        private static String requirePassword(String value) {
                String password = Objects.requireNonNull(value, "La contraseña es obligatoria.");

                if (password.isBlank() || password.length() < 8 || password.length() > 72) {
                        throw new IllegalArgumentException("La contraseña debe contener entre 8 y 72 caracteres.");
                }

                return password;
        }
}
