package com.vamo.pos.identity.domain;

import java.time.OffsetDateTime;

import java.util.UUID;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "auth_sessions")

public class AuthSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "tenant_id", nullable = false, updatable = false)
    private UUID tenantId;

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Column(name = "token_hash", nullable = false, length = 64, updatable = false)
    private String tokenHash;

    @Column(name = "expires_at", nullable = false, updatable = false)
    private OffsetDateTime expiresAt;

    @Column(name = "revoked_at")
    private OffsetDateTime revokedAt;

    @Column(name = "revoked_by")
    private UUID revokedBy;

    @Column(name = "last_used_at")
    private OffsetDateTime lastUsedAt;

    @Column(name = "ip_address", length = 45, updatable = false)
    private String ipAddress;

    @Column(name = "user_agent", length = 512, updatable = false)
    private String userAgent;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    protected AuthSession() {
    }

    /**
     * Crea una sesión con vencimiento fijo.
     * Las fechas las proporciona el servicio, no el cliente HTTP.
     */
    public AuthSession(
        UUID tenantId,
        UUID userId,
        String tokenHash,
        OffsetDateTime createdAt,
        OffsetDateTime expiresAt,
        String ipAddress,
        String userAgent
    ) {
        this.tenantId = Objects.requireNonNull(tenantId, "El tenantId es obligatorio");
        this.userId = Objects.requireNonNull(userId, "El userId es obligatorio");
        this.tokenHash = Objects.requireNonNull(tokenHash, "El hash del token es obligatorio");

        if (!this.tokenHash.matches("[0-9a-f]{64}")) {
            throw new IllegalArgumentException("El hash del token debe contener 64 caracteres hexadecimales en minúsculas.");
        }
        this.createdAt = Objects.requireNonNull(createdAt, "La fecha de creación es obligatoria.");
        this.expiresAt = Objects.requireNonNull(expiresAt, "La fecha de vencimiento es obligatoria.");

        if (!this.expiresAt.isAfter(this.createdAt)) {
            throw new IllegalArgumentException("El vencimiento debe ser posterior a la fecha de creación.");
        }
        this.ipAddress = normalizedOptionalText(ipAddress,"La dirección IP", 45);
        this.userAgent = normalizedOptionalText(userAgent,"El user agent", 512);
    }

    /**
     * Normaliza metadatos opcionales y valida su longitud.
     * No comprueba que el contenido sea una dirección IP válida.
     */
    private static String normalizedOptionalText(
        String value,
        String fieldName,
        int maximumLength
    ) {
        if (value == null) {
            return null;
        }

        String normalizedValue = value.strip();

        if (normalizedValue.isEmpty()) {
            return null;
        }

        if (normalizedValue.length() > maximumLength) {
            throw new IllegalArgumentException(
                    fieldName + " no debe superar " + maximumLength + " caracteres.");
        }
        return normalizedValue;
    }

    /**
     * Comprueba si la sesión puede utilizarse en el instante indicado.
     * El servicio proporcionará la hora para poder probar límites exactos.
     */
    public boolean isActiveAt(OffsetDateTime now){
        Objects.requireNonNull(now, "La fecha de consulta es obligatoria.");
        return revokedAt == null
            && !now.isBefore(createdAt)
            && now.isBefore(expiresAt);
    }

    /**
     * Revoca la sesión conservando el registro para auditoría.
     * revokedBy puede ser null cuando la revocación es automática.
     */
    public void revoke(OffsetDateTime now, UUID revokedBy) {
        Objects.requireNonNull(now, "La fecha de revocación es obligatoria.");

        if (now.isBefore(createdAt)) {
            throw new IllegalArgumentException("La revocación no puede ser anterior a la creación.");
        }

        if (this.revokedAt != null ) {
            return;
        }

        this.revokedAt = now;
        this.revokedBy = revokedBy;
    }

    /**
     * Registra el uso de una sesión vigente sin extender su vencimiento.
     */
    public void recordUsage(OffsetDateTime now) {
        if (!isActiveAt(now)) {
            throw new IllegalStateException("No se puede registrar el uso de una sesión fuera de la vigencia o revocada.");
        }

        if (lastUsedAt == null || now.isAfter(lastUsedAt)) {
            lastUsedAt = now;
        }
    }

    public UUID getId() {
        return id;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public OffsetDateTime getExpiresAt() {
        return expiresAt;
    }

    public OffsetDateTime getRevokedAt() {
        return revokedAt;
    }

    public UUID getRevokedBy() {
        return revokedBy;
    }

    public OffsetDateTime getLastUsedAt() {
        return lastUsedAt;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
