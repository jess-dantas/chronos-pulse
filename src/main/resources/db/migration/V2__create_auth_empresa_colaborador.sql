-- Tenant (órgão público / empresa cliente da plataforma)
CREATE TABLE tenant (
    id UUID PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    cnpj VARCHAR(14) UNIQUE NOT NULL,
    ativo BOOLEAN DEFAULT TRUE,
    criado_em TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Usuário da plataforma (colaborador, RH, admin, suporte)
CREATE TABLE cpc_usuario (
    id UUID PRIMARY KEY,
    cpc_id UUID UNIQUE NOT NULL,
    cpf VARCHAR(11) UNIQUE NOT NULL,
    nome VARCHAR(255) NOT NULL,
    email_corporativo VARCHAR(255),
    email_pessoal VARCHAR(255),
    apelido VARCHAR(100),
    celular VARCHAR(20),
    senha_hash VARCHAR(255) NOT NULL,
    role VARCHAR(30) NOT NULL,
    tenant_id UUID REFERENCES tenant(id),
    ativo BOOLEAN DEFAULT TRUE,
    criado_em TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    CONSTRAINT chk_role CHECK (role IN (
        'ADMIN_PLATAFORMA', 'SUPORTE_N1', 'SUPORTE_N2',
        'ADMIN_EMPRESA', 'COLABORADOR'
    ))
);

-- Configuração de jornada da empresa
CREATE TABLE configuracao_jornada (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenant(id),
    nome VARCHAR(100) NOT NULL,
    carga_horaria_diaria_minutos INTEGER NOT NULL,
    exige_intervalo BOOLEAN NOT NULL,
    intervalo_minimo_minutos INTEGER,
    tolerancia_entrada_minutos INTEGER DEFAULT 10,
    tolerancia_saida_minutos INTEGER DEFAULT 10,
    interjornada_minima_minutos INTEGER DEFAULT 660
);

-- Colaborador (dados corporativos gerenciados pela empresa)
CREATE TABLE colaborador (
    id UUID PRIMARY KEY,
    cpc_usuario_id UUID NOT NULL REFERENCES cpc_usuario(id),
    tenant_id UUID NOT NULL REFERENCES tenant(id),
    matricula VARCHAR(50),
    cargo VARCHAR(100),
    departamento VARCHAR(100),
    data_nascimento DATE,
    data_admissao DATE NOT NULL,
    configuracao_jornada_id UUID REFERENCES configuracao_jornada(id),
    ativo BOOLEAN DEFAULT TRUE
);

-- Adiciona tenant_id na tabela de registro de ponto
ALTER TABLE registro_ponto ADD COLUMN tenant_id UUID REFERENCES tenant(id);

CREATE INDEX idx_cpc_usuario_cpf ON cpc_usuario(cpf);
CREATE INDEX idx_cpc_usuario_tenant ON cpc_usuario(tenant_id);
CREATE INDEX idx_colaborador_tenant ON colaborador(tenant_id);
CREATE INDEX idx_registro_ponto_tenant ON registro_ponto(tenant_id);
