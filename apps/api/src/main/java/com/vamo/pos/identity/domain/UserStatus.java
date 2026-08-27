package com.vamo.pos.identity.domain;

/**
 * Estados persistentes permitidos para una cuenta de usuario.
 * Los nombres deben coincidir exactamente con los valores aceptados
 * por ck_app_users_status en PostgreSQL
 */
public enum UserStatus {
    ACTIVE,
    DISABLED
}