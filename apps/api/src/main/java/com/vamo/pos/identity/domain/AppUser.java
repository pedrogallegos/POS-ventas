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
 * Cuenta utilizada para acceder al punto de venta.
 *
 * Cada usuario pertenece a un tenant y tiene una sucursal predeterminada.
 */
@Entity
@Table(name = "app_users")
public class AppUser {

    // Llave primaria generada por Hibernate.
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    // Negocio propietario de la cuenta.
    @Column(name = "tenant_id", nullable = false, updatable = false)
    private UUID tenantId;

    // Sucursal seleccionada inicialmente para el usuario.
    @Column(name = "default_branch_id", nullable = false)
    private UUID defaultBranchId;

    // Nombre original utilizado para iniciar sesión.
    @Column(name = "username", nullable = false, length = 80, updatable = false)
    private String username;

    // Nombre de usuario en minúsculas para búsquedas y unicidad.
    @Column(name = "normalized_username", nullable = false, length = 80, updatable = false)
    private String normalizedUsername;

    // Nombre mostrado en la interfaz y los registros de auditoría.
    @Column(name = "display_name", nullable = false, length = 120)
    private String displayName;

    // Hash de la contraseña; nunca contiene la contraseña en texto plano.
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    // Estado persistente almacenado como ACTIVE o DISABLED.
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private UserStatus status;

    // Cantidad de intentos fallidos consecutivos.
    @Column(name = "failed_login_attempts", nullable = false)
    private int failedLoginAttempts;

    // Momento hasta el cual existe un bloqueo temporal.
    @Column(name = "locked_until")
    private OffsetDateTime lockedUntil;

    // Fecha del último acceso exitoso.
    @Column(name = "last_login_at")
    private OffsetDateTime lastLoginAt;

    // Fecha inicial o del cambio más reciente de contraseña.
    @CreationTimestamp
    @Column(name = "password_changed_at", nullable = false)
    private OffsetDateTime passwordChangedAt;

    // Usuario creador; puede ser null durante el alta inicial del tenant.
    @Column(name = "created_by", updatable = false)
    private UUID createdBy;

    // Fecha de creación administrada por Hibernate.
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    // Fecha de última modificación administrada por Hibernate.
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    /**
     * Constructor requerido exclusivamente por JPA.
     */
    protected AppUser() {
    }

    /**
     * Crea una cuenta activa dentro de un tenant.
     */
    public AppUser(
            UUID tenantId,
            UUID defaultBranchId,
            String username,
            String displayName,
            String passwordHash,
            UUID createdBy) {
        // El usuario siempre debe pertenecer a un negocio.
        this.tenantId = Objects.requireNonNull(tenantId, "El tenantId es obligatorio.");

        // La sucursal debe existir dentro del mismo tenant.
        this.defaultBranchId = Objects.requireNonNull(defaultBranchId, "La sucursal predeterminada es obligatoria.");

        // Eliminamos espacios exteriores y verificamos su longitud.
        this.username = requireTrimmedText(username, "El nombre de usuario", 3, 80);

        // Esta versión permite buscar sin diferenciar mayúsculas.
        this.normalizedUsername = this.username.toLowerCase(Locale.ROOT);

        this.displayName = requireTrimmedText(displayName, "El nombre visible", 2, 120);

        this.passwordHash = requirePasswordHash(passwordHash);

        // Toda cuenta nueva comienza activa y sin intentos fallidos.
        this.status = UserStatus.ACTIVE;
        this.failedLoginAttempts = 0;
        this.createdBy = createdBy;
    }

    /**
     * Elimina espacios exteriores y valida un texto obligatorio.
     */
    private static String requireTrimmedText(
            String value,
            String fieldName,
            int minimumLength,
            int maximumLength) {
        String normalizedValue = Objects.requireNonNull(
                value,
                fieldName + " es obligatorio.").strip();

        if (normalizedValue.length() < minimumLength || normalizedValue.length() > maximumLength) {
            throw new IllegalArgumentException(
                    fieldName + " debe contener entre " + minimumLength + " y " + maximumLength + " caracteres.");
        }

        return normalizedValue;
    }

    /**
     * Verifica que recibamos un hash y no una contraseña vacía.
     */
    private static String requirePasswordHash(String value) {
        String validatedHash = Objects.requireNonNull(value, "El hash de contraseña es obligatorio.");

        if (validatedHash.isBlank() || validatedHash.length() < 20 || validatedHash.length() > 255) {
            throw new IllegalArgumentException("El hash debe contener entre 20 y 255 caracteres.");
        }
        return validatedHash;
    }

    /**
     * Desactiva la cuenta sin eliminar su historial.
     */
    public void disable() {
        status = UserStatus.DISABLED;
    }

    /**
     * Reactiva una cuenta previamente desactivada.
     */
    public void activate() {
        status = UserStatus.ACTIVE;
    }

    /**
     * Cambia la sucursal predeterminada del usuario.
     */
    public void changeDefaultBranch(UUID branchId) {
        defaultBranchId = Objects.requireNonNull(branchId, "La sucursal predeterminada es obligatoria.");
    }

    /**
     * Cambia el nombre mostrado sin alterar el nombre de acceso.
     */
    public void changeDisplayName(String newDisplayName) {
        displayName = requireTrimmedText(newDisplayName, "El nombre visible", 2, 120);
    }

    public UUID getId() {
        return id;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public UUID getDefaultBranchId() {
        return defaultBranchId;
    }

    public String getUsername() {
        return username;
    }

    public String getNormalizedUsername() {
        return normalizedUsername;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public UserStatus getStatus() {
        return status;
    }

    public int getFailedLoginAttempts() {
        return failedLoginAttempts;
    }

    public OffsetDateTime getLockedUntil() {
        return lockedUntil;
    }

    public OffsetDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    public OffsetDateTime getPasswordChangedAt() {
        return passwordChangedAt;
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