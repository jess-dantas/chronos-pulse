CREATE TABLE registro_ponto (
                                id UUID PRIMARY KEY,
                                colaborador_id UUID NOT NULL,
                                data_hora_dispositivo TIMESTAMP WITH TIME ZONE NOT NULL,
                                data_hora_servidor TIMESTAMP WITH TIME ZONE NOT NULL,
                                tipo_registro VARCHAR(20) NOT NULL,
                                latitude NUMERIC(10, 8),
                                longitude NUMERIC(11, 8),
                                precisao_gps NUMERIC(6, 2),
                                foto_url VARCHAR(500),
                                hash_integridade VARCHAR(64) NOT NULL,
                                sincronizado_offline BOOLEAN DEFAULT FALSE,
                                nsr BIGSERIAL UNIQUE
);

CREATE INDEX idx_registro_ponto_colaborador ON registro_ponto(colaborador_id);
CREATE INDEX idx_registro_ponto_data_servidor ON registro_ponto(data_hora_servidor);