-- V15: Trilha de auditoria imutável (hash chain) para conformidade pública.
-- Registra quem, quando, o que e o estado antes/depois de cada mutação relevante.

CREATE TABLE tb_auditoria (
    id            UUID PRIMARY KEY,
    tenant_id     UUID,
    usuario_cpc_id UUID,
    usuario_cpf   VARCHAR(20),
    papel         VARCHAR(50),
    acao          VARCHAR(100)  NOT NULL,
    entidade      VARCHAR(100)  NOT NULL,
    entidade_id   VARCHAR(100),
    descricao     VARCHAR(1000),
    payload_antes TEXT,
    payload_depois TEXT,
    ip_origem     VARCHAR(64),
    data_hora     TIMESTAMP WITH TIME ZONE NOT NULL,
    hash_anterior VARCHAR(64),
    hash_registro VARCHAR(64)   NOT NULL UNIQUE
);

CREATE INDEX idx_auditoria_tenant_data ON tb_auditoria(tenant_id, data_hora);
CREATE INDEX idx_auditoria_entidade ON tb_auditoria(entidade, entidade_id);
