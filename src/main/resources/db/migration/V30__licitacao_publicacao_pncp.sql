-- ==========================================================
-- V30: Publicação de avisos de licitação no PNCP
--      (Portal Nacional de Contratações Públicas — art. 54 da
--      Lei 14.133/2021). Rastreia o envio por licitação.
-- ==========================================================

ALTER TABLE tb_licitacao
    ADD COLUMN pncp_status VARCHAR(20) NOT NULL DEFAULT 'NAO_PUBLICADO',
    ADD COLUMN pncp_protocolo VARCHAR(100),
    ADD COLUMN pncp_publicado_em TIMESTAMPTZ,
    ADD COLUMN pncp_erro TEXT;

ALTER TABLE tb_licitacao
    ADD CONSTRAINT chk_licitacao_pncp_status
        CHECK (pncp_status IN ('NAO_PUBLICADO', 'PUBLICADO', 'FALHA'));