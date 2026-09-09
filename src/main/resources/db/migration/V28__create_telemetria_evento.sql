-- V28 (R27): Eventos de telemetria/observabilidade.
-- Tabela resiliente (best-effort) e sem dados pessoais (LGPD).
-- app_versao e plataforma identificam a origem (servidor/app). tenant_id e
-- usuario_id sao nulos quando o evento nao tem contexto autenticado.

CREATE TABLE tb_telemetria_evento (
    id          UUID PRIMARY KEY,
    tenant_id   UUID,
    usuario_id  UUID,
    modulo      VARCHAR(30)  NOT NULL,
    tipo        VARCHAR(30)  NOT NULL,
    endpoint    VARCHAR(255),
    status_http INT,
    latency_ms  BIGINT,
    mensagem    VARCHAR(500),
    detalhe     TEXT,
    trace_id    VARCHAR(64),
    app_versao  VARCHAR(30),
    plataforma  VARCHAR(20),
    criado_em   TIMESTAMP WITH TIME ZONE NOT NULL
);

ALTER TABLE tb_telemetria_evento
    ADD CONSTRAINT chk_telemetria_tipo CHECK (
        tipo IN ('LOGIN_SUCESSO', 'LOGIN_FALHA', 'API_REQUEST', 'API_ERRO', 'UI_ERRO', 'CONEXAO_BD')
    );

CREATE INDEX idx_telemetria_tenant_data ON tb_telemetria_evento(tenant_id, criado_em);
CREATE INDEX idx_telemetria_tipo_data   ON tb_telemetria_evento(tipo, criado_em);
CREATE INDEX idx_telemetria_criado_em   ON tb_telemetria_evento(criado_em);