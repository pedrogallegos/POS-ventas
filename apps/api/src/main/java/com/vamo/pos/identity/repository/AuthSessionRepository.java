package com.vamo.pos.identity.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.repository.Repository;

import com.vamo.pos.identity.domain.AuthSession;

/**
 * Acceso a sesiones mediante consultas limitadas por negocio.
 * Exponemos únicamente las operaciones que necesitamos.
 */
public interface AuthSessionRepository extends Repository<AuthSession, UUID> {

    /**
     * Guarda una sesión nueva o los cambios de una existente.
     * Debemos usar la entidad devuelta por save.
     */
    AuthSession save(AuthSession session);

    /**
     * Busca una sesión por su identificador y el negocio al que pertenece.
     * Devuelve Optional.empty() cuando no hay coincidencias.
     */
    Optional<AuthSession> findByTenantIdAndId(UUID tenantId, UUID id);

    /**
     * Busca por el hash del token dentro del negocio indicado.
     * Recibe el hash calculado, nunca el token original.
     * Encontrar la sesión no implica que siga vigente:
     * el servicio deberá comprobar isActiveAt y el estado del usuario.
     */
    Optional<AuthSession> findByTenantIdAndTokenHash(UUID tenantId, String tokenHash);
}