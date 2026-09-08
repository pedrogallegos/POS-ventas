package com.vamo.pos.identity.application;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Base64;

import org.junit.jupiter.api.Test;

class SessionTokenServiceTest {

    // Probamos directamente la clase: no necesitamos Spring ni Docker.
    private final SessionTokenService service = new SessionTokenService();

    @Test
    void generatesTokenWithExpectedFormat() {
        String token = service.generateToken();

        assertAll(
                // El token debe tener exactamente 43 caracteres URL-safe.
                () -> assertTrue(token.matches("[A-Za-z0-9_-]{43}")),

                // Al decodificarlo, debemos recuperar los 32 bytes originales.
                () -> assertEquals(
                        32, Base64.getUrlDecoder().decode(token).length),

                // El token generado debe ser aceptado por nuestro método de hash.
                () -> assertTrue(
                        service.hashToken(token).matches("[0-9a-f]{64}"))
        );
    }

    @Test
    void hashesTokenUsingKnownSha256Value() {
        // Dato ficticio y fijo, utilizado exclusivamente en esta prueba.
        String token = "A".repeat(43);

        // Resultado calculado independientemente del servicio.
        // Comparar solo la longitud no detectaría un algoritmo equivocado.
        String expectedHash =
                "0f007385b6f9d4b7eeb2748605afe1a984a0a3bfa3f014d09e2a784ce9e5cd1a";

        assertEquals(expectedHash, service.hashToken(token));
    }

    @Test
    void rejectsInvalidTokens() {
        assertAll(
                // Un token ausente no puede procesarse.
                () -> assertThrows(
                        NullPointerException.class,
                        () -> service.hashToken(null)),

                // Rechazamos longitudes inferiores y superiores a la esperada.
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> service.hashToken("A".repeat(42))),
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> service.hashToken("A".repeat(44))),

                // Aunque mida 43, no aceptamos espacios ni caracteres ajenos.
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> service.hashToken("A".repeat(42) + " ")),
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> service.hashToken("A".repeat(42) + "+"))
        );
    }
}
