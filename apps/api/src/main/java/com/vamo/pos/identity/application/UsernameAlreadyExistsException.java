package com.vamo.pos.identity.application;

/**
 * Indica que un tenant ya tiene registrado el nombre de usuario solicitado.
 * Es una excepción específica para poder convertirla posteriormente
 * en una respuesta HTTP 409 Conflict.
 */

public final class UsernameAlreadyExistsException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public UsernameAlreadyExistsException() {
        super("El nombre de usuario ya está registrado en este negocio.");
    }
}
