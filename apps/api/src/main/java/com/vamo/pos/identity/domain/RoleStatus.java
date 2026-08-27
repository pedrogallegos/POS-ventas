package com.vamo.pos.identity.domain;

/**
 * Estados persistentes para un rol.
 * Los nombres deben coincidir exactamente con los valores aceptados
 * por ck_roles_status en PostgreSQL.
 */
public enum RoleStatus {
    ACTIVE,
    INACTIVE
}
