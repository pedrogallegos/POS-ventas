package com.vamo.pos.identity.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.repository.Repository;

import com.vamo.pos.identity.domain.Role;
import com.vamo.pos.identity.domain.RoleStatus;

/**
 * Acceso persistente a roles, siempre limitado por tenant.
 * No exponemos findById, findAll ni delete para reducir el riesgo de
 * consultar o eliminar información sin considerar el tenant.
 */
public interface RoleRepository extends Repository<Role, UUID> {
    /**
     * Inserta un rol nuevo o guarda sus cambios permitidos.
     */
    Role save(Role role);

    /**
     * Busca un rol por su identificador dentro de un tenant.
     */
    Optional<Role> findByTenantIdAndId(UUID tenantId, UUID id);

    /**
     * Busca un rol mediante su código dentro de un tenant.
     */
    Optional<Role> findByTenantIdAndCode(UUID tenantId, String code);

    /**
     * Comprueba si un código ya está ocupado dentro del tenant.
     */
    boolean existsByTenantIdAndCode(UUID tenantId, String code);

    /**
     * Lista los roles de un tenant filtrados por estado.
     */
    List<Role> findAllByTenantIdAndStatusOrderByNameAsc(UUID tenantId, RoleStatus status);
}
