package com.vamo.pos.identity.repository;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.jdbc.core.JdbcTemplate;

import com.vamo.pos.identity.domain.Role;
import com.vamo.pos.identity.domain.RoleStatus;

import com.vamo.pos.identity.domain.AppUser;
import com.vamo.pos.identity.domain.UserStatus;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import com.vamo.pos.identity.domain.Permission;

/**
 * Comprueba PermissionRepository usando PostgreSQL real y desechable.
 * Testcontainers crea la base de datos antes de las pruebas y la elimina
 * cuando terminan. No utiliza ni modifica nuestra base de datos de desarrollo.
 */
@SpringBootTest
@Testcontainers
@Transactional

class IdentityRepositoryIntegrationTest {
    /**
     * Contenedor PostgreSQL compartido por todas las pruebas de esta clase.
     */
    @Container
    @ServiceConnection
    static final PostgreSQLContainer POSTGRESQL = new PostgreSQLContainer("postgres:17-alpine")
            .withDatabaseName("vamo_pos_test")
            .withUsername("vamo_pos_test")
            .withPassword("vamo_pos_test");
    /**
     * Spring inyecta la implementación generada del repositorio.
     */
    @Autowired
    private PermissionRepository permissionRepository;

    /**
     * Permite preparar directamente los tenants requeridos por las llaves
     * foráneas. Todavía no tenemos entidades ni repositorios para Tenant.
     */
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    private UUID firstTenantId;
    private UUID secondTenantId;
    private UUID firstBranchId;
    private UUID secondBranchId;

    /**
     * Prepara dos negocios independientes antes de cada prueba.
     * La transacción elimina estos datos automáticamente al terminar.
     */
    @BeforeEach
    void createTenants() {
        firstTenantId = UUID.randomUUID();
        secondTenantId = UUID.randomUUID();
        firstBranchId = UUID.randomUUID();
        secondBranchId = UUID.randomUUID();

        insertTenant(
                firstTenantId,
                "tenant-one-" + firstTenantId, "Negocio Uno");

        insertTenant(
                secondTenantId,
                "tenant-two-" + secondTenantId,
                "Negocio Dos");

        insertBranch(
                firstBranchId,
                firstTenantId,
                "MAIN",
                "Sucursal principal");
        insertBranch(
                secondBranchId,
                secondTenantId,
                "MAIN",
                "Sucursal principal");
    }

    /**
     * Inserta únicamente la infraestructura mínima requerida por un rol.
     */
    private void insertTenant(
            UUID tenantId,
            String slug,
            String displayName) {
        jdbcTemplate.update(
                """
                        INSERT INTO tenants (
                            id,
                            slug,
                            legal_name,
                            display_name
                        )
                        VALUES (?, ?, ?, ?)
                        """,
                tenantId,
                slug,
                displayName,
                displayName);
    }

    /**
     * Crea una sucursal perteneciente al tenant indicado.
     */
    private void insertBranch(
            UUID branchId,
            UUID tenantId,
            String code,
            String name) {
        jdbcTemplate.update(
                """
                        INSERT INTO branches (
                            id,
                            tenant_id,
                            code,
                            name
                        )
                        VALUES (?, ?, ?, ?)
                        """,
                branchId,
                tenantId,
                code,
                name);
    }

    /**
     * Verifica que Flyway cargue los ocho permisos definidos en V2
     * y que el repositorio pueda consultarlos.
     */
    @Test
    void findPermissionsSeededByFlyway() {
        List<Permission> permissions = permissionRepository.findAllByModuleOrderByCodeAsc("IDENTITY");

        assertAll(
                () -> assertEquals(8, permissions.size()),
                () -> assertEquals("ROLE_MANAGE", permissions.getFirst().getCode()),
                () -> assertTrue(permissionRepository.findByCode("USER_CREATE").isPresent()));
    }

    /**
     * Verifica que el mismo nombre de usuario pueda existir en negocios
     * diferentes sin permitir consultas entre tenants.
     */
    @Test
    void keepsUserQueriesScopedToTenant() {
        AppUser firstTenantUser = appUserRepository.save(
                new AppUser(
                        firstTenantId,
                        firstBranchId,
                        "Admin",
                        "Administrador Uno",
                        "$2a$10$abcdefghijklmnopqrstuv1234567890",
                        null));

        AppUser secondTenantUser = appUserRepository.save(
                new AppUser(
                        secondTenantId,
                        secondBranchId,
                        "Admin",
                        "Administrador Dos",
                        "$2a$10$abcdefghijklmnopqrstuv1234567890",
                        null));

        assertAll(
                () -> assertEquals(
                        "admin",
                        firstTenantUser.getNormalizedUsername()),
                () -> assertTrue(
                        appUserRepository
                                .existsByTenantIdAndNormalizedUsername(firstTenantId, "admin")),
                () -> assertEquals(
                        firstTenantUser.getId(),
                        appUserRepository
                                .findByTenantIdAndNormalizedUsername(firstTenantId, "admin")
                                .orElseThrow()
                                .getId()),
                () -> assertTrue(
                        appUserRepository
                                .findByTenantIdAndId(secondTenantId, firstTenantUser.getId()).isEmpty()),
                () -> assertEquals(
                        1,
                        appUserRepository
                                .findAllByTenantIdAndStatusOrderByDisplayNameAsc(firstTenantId, UserStatus.ACTIVE)
                                .size()),
                () -> assertEquals(
                        secondTenantUser.getId(),
                        appUserRepository
                                .findByTenantIdAndNormalizedUsername(secondTenantId, "admin")
                                .orElseThrow()
                                .getId()));
    }

    /**
     * Demuestra que dos negocios pueden utilizar el mismo código de rol
     * sin acceder a los roles pertenecientes al otro tenant.
     */
    @Test
    void keepsRoleQueriesScopedToTenant() {
        Role firstTenantRole = roleRepository.save(
                new Role(
                        firstTenantId,
                        "cashier",
                        "Cajero",
                        "Atiende las ventas",
                        false,
                        null));

        Role secondTenantRole = roleRepository.save(
                new Role(
                        secondTenantId,
                        "cashier",
                        "Cajero",
                        "Atiende las ventas",
                        false,
                        null));

        assertAll(
                () -> assertTrue(
                        roleRepository.existsByTenantIdAndCode(
                                firstTenantId, "CASHIER")),
                () -> assertEquals(
                        firstTenantRole.getId(),
                        roleRepository
                                .findByTenantIdAndCode(
                                        firstTenantId,
                                        "CASHIER")
                                .orElseThrow()
                                .getId()),
                () -> assertTrue(
                        roleRepository
                                .findByTenantIdAndId(
                                        secondTenantId,
                                        firstTenantRole.getId())
                                .isEmpty()),
                () -> assertEquals(
                        1,
                        roleRepository
                                .findAllByTenantIdAndStatusOrderByNameAsc(
                                        firstTenantId,
                                        RoleStatus.ACTIVE)
                                .size()),
                () -> assertEquals(
                        secondTenantRole.getId(),
                        roleRepository
                                .findByTenantIdAndCode(
                                        secondTenantId,
                                        "CASHIER")
                                .orElseThrow()
                                .getId()));
    }
}
