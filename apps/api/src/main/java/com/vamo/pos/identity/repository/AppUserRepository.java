package com.vamo.pos.identity.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.repository.Repository;

import com.vamo.pos.identity.domain.AppUser;
import com.vamo.pos.identity.domain.UserStatus;

/**
 * Acceso persistente a usuarios, siempre limitado por tenant.
 * No exponemos búsquedas globales ni operaciones de eliminación.
 */
public interface AppUserRepository extends Repository<AppUser, UUID> {
    /**
     * Inserta un usuario nuevo o guarda sus cambios permitidos.
     */
    AppUser save(AppUser user);

    /**
     * Busca un usuario por identificador dentro de un tenant.
     */
    Optional<AppUser> findByTenantIdAndId(UUID tenantId, UUID id);

    /**
     * Busca una cuenta mediante el nombre de usuario normalizado.
     */
    Optional<AppUser> findByTenantIdAndNormalizedUsername(UUID tenantId, String normalizedUsername);

    /**
     * Comprueba si el nombre normalizado ya está ocupado en el tenant.
     */
    boolean existsByTenantIdAndNormalizedUsername(UUID tenantId, String normalizedUsername);

    /**
     * Lista los usuarios de un tenant filtrados por estado.
     */
    List<AppUser> findAllByTenantIdAndStatusOrderByDisplayNameAsc(UUID tenantId, UserStatus status);
}
