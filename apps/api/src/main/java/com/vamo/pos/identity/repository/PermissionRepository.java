package com.vamo.pos.identity.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.Repository;

import com.vamo.pos.identity.domain.Permission;

/**
 * Acceso de lectura al catálogo global de permisos.
 * No expone operaciones para crear, modificar o eliminar permisos porque
 * estos se administran mediante migraciones de Flyway.
 */
public interface PermissionRepository extends Repository<Permission, String> {
    /**
     * Busca un permiso mediante su código, que funciona como llave primaria.
     */
    Optional<Permission> findByCode(String code);

    /**
     * Obtiene todo el catálogo ordenado por módulo y código.
     */
    List<Permission> findAllByOrderByModuleAscCodeAsc();

    /**
     * Obtiene los permisos pertenecientes a un módulo.
     */
    List<Permission> findAllByModuleOrderByCodeAsc(String module);
}
