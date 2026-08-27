ALTER TABLE branches ADD CONSTRAINT uq_branches_tenant_id UNIQUE (
    tenant_id, id
);

CREATE TABLE permissions (
    code VARCHAR(80) PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    description VARCHAR(300) NOT NULL,
    module VARCHAR(40) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE
    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_permissions_code_format
    CHECK (code ~ '^[A-Z][A-Z0-9_]{2,79}$'),
    CONSTRAINT ck_permissions_name_length
    CHECK (CHAR_LENGTH(BTRIM(name)) BETWEEN 2 AND 120),
    CONSTRAINT ck_permissions_module_format
    CHECK (module ~ '^[A-Z][A-Z0-9_]{2,39}$')
);

INSERT INTO permissions (
    code,
    name,
    description,
    module
)
VALUES (
    'TENANT_MANAGE',
    'Administrar negocio',
    'Modificar la configuración general del negocio',
    'IDENTITY'
),
(
    'USER_READ',
    'Consultar usuarios',
    'Consultar usuarios pertenecientes al negocio',
    'IDENTITY'
),
(
    'USER_CREATE',
    'Crear usuarios',
    'Crear usuarios dentro del negocio',
    'IDENTITY'
),
(
    'USER_UPDATE',
    'Actualizar usuarios',
    'Actualizar datos y asignaciones de usuarios',
    'IDENTITY'
),
(
    'USER_DISABLE',
    'Desactivar usuarios',
    'Desactivar o reactivar usuarios.',
    'IDENTITY'
),
(
    'ROLE_READ',
    'Consultar roles',
    'Consultar roles y permisos del negocio.',
    'IDENTITY'
),
(
    'ROLE_MANAGE',
    'Administrar roles',
    'Crear roles y administrar sus permisos.',
    'IDENTITY'
),
(
    'SESSION_REVOKE',
    'Revocar sesiones',
    'Cerrar sesiones activas de usuarios del negocio.',
    'IDENTITY'
);

CREATE TABLE roles (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(300),
    is_system BOOLEAN NOT NULL DEFAULT FALSE,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_by UUID,
    created_at TIMESTAMP WITH TIME ZONE
    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE
    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_roles_tenant
    FOREIGN KEY (tenant_id)
    REFERENCES tenants (id),
    CONSTRAINT uq_roles_tenant_id
    UNIQUE (tenant_id, id),
    CONSTRAINT uq_roles_tenant_code
    UNIQUE (tenant_id, code),
    CONSTRAINT ck_roles_code_format
    CHECK (code ~ '^[A-Z][A-Z0-9_]{2,49}$'),
    CONSTRAINT ck_roles_name_trimmed
    CHECK (name = BTRIM(name)),
    CONSTRAINT ck_roles_name_length
    CHECK (CHAR_LENGTH(name) BETWEEN 2 AND 100),
    CONSTRAINT ck_roles_description_length
    CHECK (
        description IS NULL OR CHAR_LENGTH(BTRIM(description)) BETWEEN 2 AND 300
    ),
    CONSTRAINT ck_roles_status
    CHECK (status IN ('ACTIVE', 'INACTIVE'))
);

CREATE TABLE app_users (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    default_branch_id UUID NOT NULL,
    username VARCHAR(80) NOT NULL,
    normalized_username VARCHAR(80) NOT NULL,
    display_name VARCHAR(120) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    failed_login_attempts INTEGER NOT NULL DEFAULT 0,
    locked_until TIMESTAMP WITH TIME ZONE,
    last_login_at TIMESTAMP WITH TIME ZONE,
    password_changed_at TIMESTAMP WITH TIME ZONE
        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    created_at TIMESTAMP WITH TIME ZONE
        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE
        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_app_users_tenant
        FOREIGN KEY (tenant_id)
        REFERENCES tenants (id),
    CONSTRAINT fk_app_users_default_branch
        FOREIGN KEY (tenant_id, default_branch_id)
        REFERENCES branches (tenant_id, id),
    CONSTRAINT uq_app_users_tenant_id
        UNIQUE (tenant_id, id),
    CONSTRAINT uq_app_users_tenant_username
        UNIQUE (tenant_id, normalized_username),
    CONSTRAINT fk_app_users_created_by
        FOREIGN KEY (tenant_id, created_by)
        REFERENCES app_users (tenant_id, id),
    CONSTRAINT ck_app_users_username_trimmed
        CHECK (username = BTRIM(username)),
    CONSTRAINT ck_app_users_username_length
        CHECK (CHAR_LENGTH(username) BETWEEN 3 AND 80),
    CONSTRAINT ck_app_users_normalized_username
        CHECK (normalized_username = LOWER(username)),
    CONSTRAINT ck_app_users_display_name_trimmed
        CHECK (display_name = BTRIM(display_name)),
    CONSTRAINT ck_app_users_display_name_length
        CHECK (CHAR_LENGTH(display_name) BETWEEN 2 AND 120),
    CONSTRAINT ck_app_users_password_hash
        CHECK (CHAR_LENGTH(password_hash) >= 20),
    CONSTRAINT ck_app_users_status
        CHECK (status IN ('ACTIVE', 'DISABLED')),
    CONSTRAINT ck_app_users_locked_until
        CHECK (locked_until IS NULL OR locked_until >= created_at),
    CONSTRAINT ck_app_users_failed_login_attempts
        CHECK (failed_login_attempts >= 0),
    CONSTRAINT ck_app_users_last_login
        CHECK (last_login_at IS NULL OR last_login_at >= created_at)
);

ALTER TABLE roles
    ADD CONSTRAINT fk_roles_created_by
        FOREIGN KEY (tenant_id, created_by)
        REFERENCES app_users (tenant_id, id);

CREATE TABLE user_roles (
    tenant_id UUID NOT NULL,
    user_id UUID NOT NULL,
    role_id UUID NOT NULL,
    assigned_by UUID,
    assigned_at TIMESTAMP WITH TIME ZONE
        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_user_roles
        PRIMARY KEY (tenant_id, user_id, role_id),
    CONSTRAINT fk_user_roles_user
        FOREIGN KEY (tenant_id, user_id)
        REFERENCES app_users (tenant_id, id),
    CONSTRAINT fk_user_roles_role
        FOREIGN KEY (tenant_id, role_id)
        REFERENCES roles (tenant_id, id),
    CONSTRAINT fk_user_roles_assigned_by
        FOREIGN KEY (tenant_id, assigned_by)
        REFERENCES app_users (tenant_id, id)
);

CREATE TABLE role_permissions (
    tenant_id UUID NOT NULL,
    role_id UUID NOT NULL,
    permission_code VARCHAR(80) NOT NULL,
    granted_by UUID,
    granted_at TIMESTAMP WITH TIME ZONE
        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_role_permissions
        PRIMARY KEY (tenant_id, role_id, permission_code),
    CONSTRAINT fk_role_permissions_role
        FOREIGN KEY (tenant_id, role_id)
        REFERENCES roles (tenant_id, id),
    CONSTRAINT fk_role_permissions_permission
        FOREIGN KEY (permission_code)
        REFERENCES permissions (code),
    CONSTRAINT fk_role_permissions_granted_by
        FOREIGN KEY (tenant_id, granted_by)
        REFERENCES app_users (tenant_id, id)
);

CREATE TABLE auth_sessions (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    user_id UUID NOT NULL,
    token_hash VARCHAR(64) NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    revoked_at TIMESTAMP WITH TIME ZONE,
    revoked_by UUID,
    last_used_at TIMESTAMP WITH TIME ZONE,
    ip_address VARCHAR(45),
    user_agent VARCHAR(512),
    created_at TIMESTAMP WITH TIME ZONE
        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_auth_sessions_user
        FOREIGN KEY (tenant_id, user_id)
        REFERENCES app_users (tenant_id, id),
    CONSTRAINT fk_auth_sessions_revoked_by
        FOREIGN KEY (tenant_id, revoked_by)
        REFERENCES app_users (tenant_id, id),
    CONSTRAINT uq_auth_sessions_token_hash
        UNIQUE (token_hash),
    CONSTRAINT ck_auth_sessions_token_hash_format
        CHECK (token_hash ~ '^[0-9a-f]{64}$'),
    CONSTRAINT ck_auth_sessions_expiration
        CHECK (expires_at > created_at),
    CONSTRAINT ck_auth_sessions_revocation
        CHECK (revoked_at IS NULL OR revoked_at >= created_at),
    CONSTRAINT ck_auth_sessions_revoked_by
        CHECK (revoked_at IS NOT NULL OR revoked_by IS NULL),
    CONSTRAINT ck_auth_sessions_last_used
        CHECK (last_used_at IS NULL OR last_used_at >= created_at)
);

CREATE INDEX ix_roles_tenant_status
    ON roles (tenant_id, status);

CREATE INDEX ix_app_users_tenant_status
    ON app_users (tenant_id, status);

CREATE INDEX ix_app_users_default_branch
    ON app_users (tenant_id, default_branch_id);

CREATE INDEX ix_user_roles_role
    ON user_roles (tenant_id, role_id);

CREATE INDEX ix_role_permissions_permission
    ON role_permissions (permission_code, tenant_id, role_id);

CREATE INDEX ix_auth_sessions_user
    ON auth_sessions (tenant_id, user_id);

CREATE INDEX ix_auth_sessions_active_expiration
    ON auth_sessions (expires_at)
    WHERE revoked_at IS NULL;
