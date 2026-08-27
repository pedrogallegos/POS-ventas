package com.vamo.pos.identity.domain;

import java.time.OffsetDateTime;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Rol perteneciente a un tenant.
 * Un rol agrupa permisos y posteriormente puede asignarse a varios usuarios.
 */
@Entity
@Table(name = "roles")
public class Role {
    // Hibernate genera un UUID antes de insertar el rol.
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    // Identifica al negocio propietario del rol.
    @Column(name = "tenant_id", nullable = false, updatable = false)
    private UUID tenantId;

    // Identificador estable del rol, por ejemplo OWNER o CASHIER.
    @Column(name = "code", nullable = false, length = 50, updatable = false)
    private String code;

    // Nombre legible mostrado por el usuario
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    // Explicación opcional del propósito del rol.
    @Column(name = "description", length = 300)
    private String description;

    // Indica si el sistema creó el rol y protege su identidad
    @Column(name = "is_system", nullable = false, updatable = false)
    private boolean system;

    // Enum almacenado como texto: ACTIVE O INACTIVE.
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private RoleStatus status;

    // Usuario que creó el rol; puede ser null durante el alta inicial.
    @Column(name = "created_by", updatable = false)
    private UUID createdBy;

    // Hibernate establece esta fecha al insertar.
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    // Hibernate actualiza esta fecha cuando modifica el rol.
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    /**
     * Constructor requerido exclusivamente por JPA.
     */
    protected Role() {
    }

    /**
     * Crea un rol activo dentro de un tenant.
     */
    public Role(
        UUID tenantId,
        String code,
        String name,
        String description,
        boolean system,
        UUID createdBy
    ) {
        this.tenantId = Objects.requireNonNull(tenantId, "El tenantId es obligatorio.");
        this.code = Objects.requireNonNull(code, "El código del rol es obligatorio.").strip().toUpperCase(Locale.ROOT);
        this.name = Objects.requireNonNull(name, "El nombre del rol es obligatorio.").strip();
        this.description = normalizeOptionalText(description);
        this.system = system;
        this.status = RoleStatus.ACTIVE;
        this.createdBy = createdBy;
    }

    /**
     * Convierte texto vacío en null y elimina espacios exteriores.
     */
    private static String normalizeOptionalText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.strip();
    }

    /**
     * Desactiva el rol sin eliminar su historial.
     */
    public void deactivate() {
        status = RoleStatus.INACTIVE;
    }

    /**
     * Reactiva un rol previamente desactivado.
     */
    public void activate() {
        status = RoleStatus.ACTIVE;
    }

    public UUID getId() {
        return id;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public boolean isSystem() {
        return system;
    }

    public RoleStatus getStatus() {
        return status;
    }

    public UUID getCreatedBy() {
        return createdBy;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
