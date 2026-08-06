CREATE TABLE tenants (
    id UUID PRIMARY KEY,
    slug VARCHAR(63) NOT NULL,
    legal_name VARCHAR(160) NOT NULL,
    display_name VARCHAR(120) NOT NULL,
    time_zone VARCHAR(64) NOT NULL DEFAULT 'America/Mexico_City',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_tenants_slug UNIQUE (slug),
    CONSTRAINT ck_tenants_status CHECK (status IN ('ACTIVE', 'SUSPENDED', 'INACTIVE'))
);

CREATE TABLE branches (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    code VARCHAR(32) NOT NULL,
    name VARCHAR(120) NOT NULL,
    time_zone VARCHAR(64) NOT NULL DEFAULT 'America/Mexico_City',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_branches_tenant
        FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    CONSTRAINT uq_branches_tenant_code UNIQUE (tenant_id, code),
    CONSTRAINT ck_branches_status CHECK (status IN ('ACTIVE', 'INACTIVE'))
);

CREATE INDEX ix_branches_tenant_id ON branches (tenant_id);

