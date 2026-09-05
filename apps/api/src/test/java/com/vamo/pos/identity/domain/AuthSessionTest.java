package com.vamo.pos.identity.domain;


import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class AuthSessionTest {

    // Fecha fija: las pruebas no dependen de la hora en que se ejecuten.
    private static final OffsetDateTime CREATED_AT =
            OffsetDateTime.parse("2026-09-05T12:00:00Z");

    // Valor ficticio que cumple el formato; no es un token real.
    private static final String TOKEN_HASH = "a".repeat(64);

    @Test
    void respectsValidityBoundaries() {
        AuthSession session = createSession();
        OffsetDateTime expiresAt = CREATED_AT.plusHours(1);

        // Comprobamos ambos extremos: creación incluida, vencimiento excluido.
        assertAll(
                () -> assertFalse(
                        session.isActiveAt(CREATED_AT.minusNanos(1))),
                () -> assertTrue(
                        session.isActiveAt(CREATED_AT)),
                () -> assertTrue(
                        session.isActiveAt(expiresAt.minusNanos(1))),
                () -> assertFalse(
                        session.isActiveAt(expiresAt)),
                () -> assertFalse(
                        session.isActiveAt(expiresAt.plusSeconds(1)))
        );
    }

    @Test
    void comparesInstantsAcrossDifferentOffsets() {
        AuthSession session = createSession();

        // Representamos los mismos instantes usando el offset -06:00.
        OffsetDateTime creationWithAnotherOffset =
                CREATED_AT.withOffsetSameInstant(ZoneOffset.ofHours(-6));

        OffsetDateTime expirationWithAnotherOffset =
                CREATED_AT.plusHours(1)
                        .withOffsetSameInstant(ZoneOffset.ofHours(-6));

        // Cambiar la representación horaria no debe cambiar la vigencia.
        assertAll(
                () -> assertTrue(
                        session.isActiveAt(creationWithAnotherOffset)),
                () -> assertFalse(
                        session.isActiveAt(expirationWithAnotherOffset))
        );
    }

    // Cada llamada construye una sesión independiente para evitar
    // que los cambios de una prueba afecten a otra.
    private static AuthSession createSession() {
        return new AuthSession(
                UUID.randomUUID(),
                UUID.randomUUID(),
                TOKEN_HASH,
                CREATED_AT,
                CREATED_AT.plusHours(1),
                null,
                null
        );
    }

    @Test
    void revokesSessionAndPreservesOriginalAudit() {
        AuthSession session = createSession();
        UUID administratorId = UUID.randomUUID();
        OffsetDateTime revokedAt = CREATED_AT.plusMinutes(10);

        // La primera revocación debe registrar fecha y responsable.
        session.revoke(revokedAt, administratorId);

        // Repetir la operación no debe sobrescribir esos datos.
        session.revoke(CREATED_AT.plusMinutes(20), UUID.randomUUID());

        assertAll(
                () -> assertFalse(session.isActiveAt(revokedAt)),
                () -> assertEquals(revokedAt, session.getRevokedAt()),
                () -> assertEquals(administratorId, session.getRevokedBy())
        );
    }
    @Test
    void recordsLatestUsageWithoutExtendingExpiration() {
        AuthSession session = createSession();
        OffsetDateTime latestUsage = CREATED_AT.plusMinutes(20);

        // Registramos dos usos consecutivos.
        session.recordUsage(CREATED_AT.plusMinutes(10));
        session.recordUsage(latestUsage);

        // Una fecha anterior o repetida no debe retroceder el último uso.
        session.recordUsage(CREATED_AT.plusMinutes(15));
        session.recordUsage(latestUsage);

        assertAll(
                () -> assertEquals(latestUsage, session.getLastUsedAt()),

                // Usar la sesión no debe prolongar su duración.
                () -> assertEquals(
                        CREATED_AT.plusHours(1), session.getExpiresAt())
        );
    }
    @Test
    void rejectsUsageAtExpiration() {
        AuthSession session = createSession();
        OffsetDateTime previousUsage = CREATED_AT.plusMinutes(10);
        session.recordUsage(previousUsage);

        // Esperamos una excepción al usar la sesión justo cuando vence.
        assertThrows(
                IllegalStateException.class,
                () -> session.recordUsage(CREATED_AT.plusHours(1))
        );

        // La operación rechazada no debe modificar el último uso válido.
        assertEquals(previousUsage, session.getLastUsedAt());
    }

    @Test
    void rejectsUsageAfterRevocation() {
        AuthSession session = createSession();
        OffsetDateTime previousUsage = CREATED_AT.plusMinutes(5);
        session.recordUsage(previousUsage);

        // null indica que la revocación fue automática.
        session.revoke(CREATED_AT.plusMinutes(10), null);

        // Aunque aún no vence, una sesión revocada ya no puede utilizarse.
        assertThrows(
                IllegalStateException.class,
                () -> session.recordUsage(CREATED_AT.plusMinutes(15))
        );

        assertEquals(previousUsage, session.getLastUsedAt());
    }
}