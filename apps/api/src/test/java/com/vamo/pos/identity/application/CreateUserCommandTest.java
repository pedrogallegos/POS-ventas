package com.vamo.pos.identity.application;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;

import org.junit.jupiter.api.Test;

/**
 * Pruebas de las reglas de entrada para crear usuarios.
 */
class CreateUserCommandTest {

    @Test
    void trimsUserDataButPreservesPassword() {
        String rawPassword = " Secret123! ";

        CreateUserCommand command = new CreateUserCommand(
                UUID.randomUUID(),
                UUID.randomUUID(),
                " Admin ",
                " Pedro Gallegos ",
                rawPassword,
                null
        );

        assertAll(
                () -> assertEquals("Admin", command.username()),
                () -> assertEquals(
                        "Pedro Gallegos",
                        command.displayName()
                ),
                () -> assertEquals(rawPassword, command.rawPassword())
        );
    }

    @Test
    void rejectsPasswordShorterThanEightCharacters() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new CreateUserCommand(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        "admin",
                        "Administrador",
                        "1234567",
                        null
                )
        );
    }

    @Test
    void rejectsPasswordLongerThanSeventyTwoCharacters() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new CreateUserCommand(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        "admin",
                        "Administrador",
                        "a".repeat(73),
                        null
                )
        );
    }
}