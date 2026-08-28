package com.vamo.pos.identity.domain;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;

import org.junit.jupiter.api.Test;

/**
 * Pruebas unitarias de las reglas internas de AppUser.
 */
class AppUserTest {

    // Simula un hash de longitud válida sin usar una contraseña real.
    private static final String VALID_PASSWORD_HASH = "x".repeat(60);

    @Test
    void createsActiveUserWithNormalizedValues() {
        UUID tenantId = UUID.randomUUID();
        UUID branchId = UUID.randomUUID();
        UUID createdBy = UUID.randomUUID();

        AppUser user = new AppUser(
            tenantId,
            branchId,
            " Admin ",
            " María López ",
            VALID_PASSWORD_HASH,
            createdBy
        );

        assertAll(
            () -> assertEquals(tenantId, user.getTenantId()),
            () -> assertEquals(branchId, user.getDefaultBranchId()),
            () -> assertEquals("Admin", user.getUsername()),
            () -> assertEquals(
                "admin",
                user.getNormalizedUsername()
            ),
            () -> assertEquals("María López", user.getDisplayName()),
            () -> assertEquals(
                VALID_PASSWORD_HASH,
                user.getPasswordHash()
            ),
            () -> assertEquals(UserStatus.ACTIVE, user.getStatus()),
            () -> assertEquals(0, user.getFailedLoginAttempts()),
            () -> assertNull(user.getLockedUntil()),
            () -> assertNull(user.getLastLoginAt()),
            () -> assertEquals(createdBy, user.getCreatedBy())
        );
    }

    @Test
    void disablesAndReactivatesUser() {
        AppUser user = createValidUser();

        user.disable();
        assertEquals(UserStatus.DISABLED, user.getStatus());

        user.activate();
        assertEquals(UserStatus.ACTIVE, user.getStatus());
    }

    @Test
    void changesDefaultBranchAndDisplayName() {
        AppUser user = createValidUser();
        UUID newBranchId = UUID.randomUUID();

        user.changeDefaultBranch(newBranchId);
        user.changeDisplayName(" Nuevo nombre ");

        assertAll(
            () -> assertEquals(
                newBranchId,
                user.getDefaultBranchId()
            ),
            () -> assertEquals(
                "Nuevo nombre",
                user.getDisplayName()
            )
        );
    }

    @Test
    void rejectsShortUsername() {
        assertThrows(
            IllegalArgumentException.class,
            () -> new AppUser(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "ab",
                "Usuario",
                VALID_PASSWORD_HASH,
                null
            )
        );
    }

    @Test
    void rejectsInvalidPasswordHash() {
        assertThrows(
            IllegalArgumentException.class,
            () -> new AppUser(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "usuario",
                "Usuario",
                "hash-corto",
                null
            )
        );
    }

    @Test
    void rejectsMissingTenant() {
        assertThrows(
            NullPointerException.class,
            () -> new AppUser(
                null,
                UUID.randomUUID(),
                "usuario",
                "Usuario",
                VALID_PASSWORD_HASH,
                null
            )
        );
    }

    /**
     * Crea un usuario válido para evitar repetir datos.
     */
    private AppUser createValidUser() {
        return new AppUser(
            UUID.randomUUID(),
            UUID.randomUUID(),
            "usuario",
            "Usuario de prueba",
            VALID_PASSWORD_HASH,
            null
        );
    }
}