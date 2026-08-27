package com.vamo.pos.identity.domain;

import java.time.OffsetDateTime;

import org.hibernate.annotations.Immutable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Permiso disponible dentro del sistema.
 * Los permisos son globales: no pertenecen a un tenant particular.
 * Se crean mediante migraciones de Flyway y la aplicación solo los consulta.
 */
@Entity
@Table(name = "permissions")
@Immutable
public class Permission {
    @Id
    @Column(name = "code", nullable = false, length = 80)
    private String code;

    @Column(name = "name", nullable = false, length = 120)
    private String name;

    @Column(name = "description", nullable = false, length = 300)
    private String description;

    @Column(name = "module", nullable = false, length = 40)
    private String module;

    @Column(
        name = "created_at",
        nullable = false,
        insertable = false,
        updatable = false
    )
    private OffsetDateTime createdAt;

    /**
     * Constructor requerido por JPA.
     * Es protected para impedir que otras partes de la aplicación creen
     * permisos arbitrarios que no existan en las migraciones.
     */
    protected Permission() {
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

    public String getModule() {
        return module;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
