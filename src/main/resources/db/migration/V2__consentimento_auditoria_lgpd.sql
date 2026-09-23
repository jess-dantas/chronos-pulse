-- Auditoria reforçada do Termo de Ciência (LGPD + validade trabalhista):
-- registra tenant, user-agent e hash SHA-256 do texto exato do termo aceito.
ALTER TABLE tb_consentimento_privacidade
    ADD COLUMN tenant_id  UUID,
    ADD COLUMN user_agent VARCHAR(512),
    ADD COLUMN hash_termo VARCHAR(64);
