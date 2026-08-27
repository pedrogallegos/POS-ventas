package com.vamo.pos.identity.domain;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;

import org.junit.jupiter.api.Test;

/**
 * Pruebas unitarias de las reglas internas de Role.
 *
 * No requieren Spring, Hibernate ni una conexión a PostgreSQL.
 */
class RoleTest {

    @Test
    void createsActiveRoleWithNormalizedValues() {
        // Arrange: preparamos los identificadores requeridos.
        UUID tenantId = UUID.randomUUID();
        UUID createdBy = UUID.randomUUID();

        // Act: construimos un rol con espacios y código en minúsculas.
        Role role = new Role(
            tenantId,
            " cashier ",
            " Cajero ",
            " Atiende las ventas ",
            false,
            createdBy
        );

        // Assert: verificamos todas las reglas de construcción.
        assertAll(
            () -> assertEquals(tenantId, role.getTenantId()),
            () -> assertEquals("CASHIER", role.getCode()),
            () -> assertEquals("Cajero", role.getName()),
            () -> assertEquals(
                "Atiende las ventas",
                role.getDescription()
            ),
            () -> assertFalse(role.isSystem()),
            () -> assertEquals(RoleStatus.ACTIVE, role.getStatus()),
            () -> assertEquals(createdBy, role.getCreatedBy())
        );
    }

    @Test
    void normalizesBlankDescriptionToNull() {
        Role role = createRole("   ");

        assertNull(role.getDescription());
    }

    @Test
    void deactivatesAndReactivatesRole() {
        Role role = createRole("Rol para cobrar ventas");

        role.deactivate();
        assertEquals(RoleStatus.INACTIVE, role.getStatus());

        role.activate();
        assertEquals(RoleStatus.ACTIVE, role.getStatus());
    }

    @Test
    void rejectsMissingTenant() {
        assertThrows(
            NullPointerException.class,
            () -> new Role(
                null,
                "CASHIER",
                "Cajero",
                null,
                false,
                null
            )
        );
    }

    /**
     * Crea un rol válido para evitar repetir datos en varias pruebas.
     */
    private Role createRole(String description) {
        return new Role(
            UUID.randomUUID(),
            "CASHIER",
            "Cajero",
            description,
            false,
            null
        );
    }
}