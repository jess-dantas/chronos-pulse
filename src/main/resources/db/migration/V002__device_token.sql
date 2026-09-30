-- Vinculo de dispositivo para o "Modo Ponto" (batida offline/sem sessão ativa).
-- O valor cru do token nunca é persistido: apenas o hash SHA-256 (64 chars).
-- O vinculo expira em 7 dias (chronos.jwt.device-expiration-ms) e pode ser
-- revogado a qualquer momento pelo titular (revogado_em preenchido).
CREATE TABLE device_token (
    id UUID PRIMARY KEY,
    usuario_id UUID NOT NULL REFERENCES cpc_usuario(id) ON DELETE CASCADE,
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    device_name VARCHAR(120),
    criado_em TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expira_em TIMESTAMP WITH TIME ZONE NOT NULL,
    revogado_em TIMESTAMP WITH TIME ZONE,
    ultimo_uso_em TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_device_token_usuario ON device_token(usuario_id);
