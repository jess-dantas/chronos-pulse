-- V23: NFe por XML/SEFAZ — armazenamento do documento e identificação do emitente
ALTER TABLE tb_entrada_nfe ADD COLUMN cnpj_emitente VARCHAR(14);
ALTER TABLE tb_entrada_nfe ADD COLUMN razao_emitente VARCHAR(160);
ALTER TABLE tb_entrada_nfe ADD COLUMN xml_nfe TEXT;

CREATE INDEX idx_entrada_nfe_emitente ON tb_entrada_nfe(tenant_id, cnpj_emitente);