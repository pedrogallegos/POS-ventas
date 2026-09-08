package com.vamo.pos.identity.application;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Objects;

import org.springframework.stereotype.Service;

/** Genera tokens de sesión y calcula sus hashes.
 * No guarda datos ni decide si una sesión está vigente.
 */
@Service
public class SessionTokenService {
    private static final int TOKEN_BYTES = 32;
    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Genera el token original que posteriormente recibirá el cliente.
     */
    public String generateToken() {
        byte[] randomBytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(randomBytes);
    }
    /**
     * Calcula el hash del token para almacenarlo o buscar su sesión.
     * El token original nunca debe aparecer en logs.
     */
    public String hashToken(String rawToken) {
        Objects.requireNonNull(rawToken, "El token es obligatorio");
        if (!rawToken.matches("[A-Za-z0-9_-]{43}")) {
            throw new IllegalArgumentException("El token debe contener 43 caracteres Base64 URL-safe.");
        }

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 no está disponible en este entorno", exception);
        }
    }
}
