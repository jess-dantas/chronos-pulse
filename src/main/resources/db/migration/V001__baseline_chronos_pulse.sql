-- ============================================================
-- V001: Chronos Pulse — baseline única (schema + seeds)
-- Consolida TODO o histórico de migrations (cadeia antiga V1–V46
-- + auditoria do Termo de Ciência LGPD) em um único arquivo.
--
-- REGRA PRÉ-PRODUÇÃO: enquanto o software não estiver em produção
-- com validade jurídica, este é o ÚNICO arquivo de migration —
-- toda mudança de schema é uma refactor deste V001 (nunca crie
-- V002+). O histórico é recriado via workflow "Deploy Production
-- Backend" (workflow_dispatch com a checkbox reset_database).
--
-- Quando a produção real for ativada, este arquivo passa a ser o
-- baseline CONGELADO (imutável — regra de ouro: nunca editar
-- migration já aplicada) e novas mudanças terão migrations próprias
-- (V002+).
--
-- Conteúdo novo em relação à cadeia antiga:
--   * admin_plataforma: colunas de 2FA (TOTP Google Authenticator)
--   * usuario_modulo: associação usuário ↔ módulo da plataforma
--   * SEM seed de admin_plataforma: provisionamento via first-run wizard
--     (POST /admin/auth/bootstrap, somente enquanto a tabela estiver vazia)
--   * removido o usuário CPF 00000000000 (admin via tabela própria)
--   * removido o usuário CPF 99999999999 (Fundador Red Cape — zero-trace)
--   * colaboradores renomeados para "Colaborador 1" / "Colaborador 2"
--   * seeds de demo no tenant "Demonstração" (CNPJ 01001001000101, slug
--     demonstracao) e tenant "LJ Code" (ex-Red Cape, slug lj-code)
--   * auditoria reforçada do Termo de Ciência (LGPD + validade
--     trabalhista): tenant_id, user_agent e hash_termo (SHA-256 do
--     texto exato do termo) em tb_consentimento_privacidade
-- ============================================================

-- ============================================================
-- TABELAS CORE (Autenticação, Tenant, Colaborador, Ponto)
-- ============================================================

CREATE TABLE tenant (
    id UUID PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    cnpj VARCHAR(14) UNIQUE NOT NULL,
    slug VARCHAR(255) UNIQUE NOT NULL,
    razao_social VARCHAR(255),
    nome_fantasia VARCHAR(255),
    produto VARCHAR(100),
    responsavel_nome VARCHAR(255),
    responsavel_cpf VARCHAR(14),
    responsavel_email VARCHAR(255),
    responsavel_celular VARCHAR(20),
    responsavel_telefone VARCHAR(20),
    endereco_logradouro VARCHAR(255),
    endereco_numero VARCHAR(20),
    endereco_complemento VARCHAR(100),
    endereco_bairro VARCHAR(100),
    endereco_cidade VARCHAR(100),
    endereco_uf VARCHAR(2),
    endereco_cep VARCHAR(8),
    ativo BOOLEAN DEFAULT TRUE,
    criado_em TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE TABLE cpc_usuario (
    id UUID PRIMARY KEY,
    cpc_id UUID UNIQUE NOT NULL,
    cpf VARCHAR(11) UNIQUE NOT NULL,
    nome VARCHAR(255) NOT NULL,
    email_corporativo VARCHAR(255),
    email_pessoal VARCHAR(255),
    apelido VARCHAR(100),
    celular VARCHAR(20),
    foto TEXT,
    senha_hash VARCHAR(255) NOT NULL,
    role VARCHAR(30) NOT NULL,
    tenant_id UUID REFERENCES tenant(id),
    acesso_estoque BOOLEAN DEFAULT FALSE,
    acesso_patrimonio BOOLEAN DEFAULT FALSE,
    acesso_frota BOOLEAN DEFAULT FALSE,
    acesso_protocolo BOOLEAN DEFAULT FALSE,
    ativo BOOLEAN DEFAULT TRUE,
    criado_em TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    senha_alterada_em TIMESTAMP WITH TIME ZONE,
    tentativas_login_falhas INTEGER DEFAULT 0,
    bloqueio_login_ate TIMESTAMP WITH TIME ZONE,
    CONSTRAINT chk_role CHECK (role IN (
        'ADMIN_PLATAFORMA', 'SUPORTE_N1', 'SUPORTE_N2',
        'ADMIN_EMPRESA', 'GESTOR_RH', 'COLABORADOR'
    ))
);

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

CREATE TABLE colaborador (
    id UUID PRIMARY KEY,
    cpc_usuario_id UUID NOT NULL REFERENCES cpc_usuario(id),
    tenant_id UUID NOT NULL REFERENCES tenant(id),
    matricula VARCHAR(50),
    cargo VARCHAR(100),
    departamento VARCHAR(100),
    data_nascimento DATE,
    data_admissao DATE NOT NULL,
    data_desligamento DATE,
    configuracao_jornada_id UUID REFERENCES configuracao_jornada(id),
    ativo BOOLEAN DEFAULT TRUE
);

CREATE TABLE registro_ponto (
    id UUID PRIMARY KEY,
    colaborador_id UUID NOT NULL,
    tenant_id UUID NOT NULL REFERENCES tenant(id),
    data_hora_dispositivo TIMESTAMP WITH TIME ZONE NOT NULL,
    data_hora_servidor TIMESTAMP WITH TIME ZONE NOT NULL,
    tipo_registro VARCHAR(20) NOT NULL,
    latitude NUMERIC(10, 8),
    longitude NUMERIC(11, 8),
    precisao_gps NUMERIC(6, 2),
    foto_url VARCHAR(500),
    hash_integridade VARCHAR(64) NOT NULL,
    sincronizado_offline BOOLEAN DEFAULT FALSE,
    nsr BIGSERIAL UNIQUE,
    nsr_logico BIGINT,
    ajuste_manual BOOLEAN DEFAULT FALSE,
    justificativa VARCHAR(255),
    observacao VARCHAR(500),
    ajuste_status VARCHAR(20) DEFAULT 'APROVADO',
    ajuste_motivo_rejeicao VARCHAR(500),
    aprovado_por UUID,
    aprovado_em TIMESTAMP WITH TIME ZONE,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_registro_ponto_colaborador ON registro_ponto(colaborador_id);
CREATE INDEX idx_registro_ponto_data_servidor ON registro_ponto(data_hora_servidor);
CREATE INDEX idx_registro_ponto_tenant ON registro_ponto(tenant_id);
CREATE INDEX idx_registro_ponto_nsr_logico ON registro_ponto(tenant_id, colaborador_id, nsr_logico);
CREATE INDEX idx_registro_ponto_colab_data ON registro_ponto(colaborador_id, tenant_id, data_hora_dispositivo);

CREATE SEQUENCE IF NOT EXISTS seq_registro_ponto_nsr;

CREATE OR REPLACE FUNCTION obter_proximo_nsr_logico(p_tenant_id UUID, p_colaborador_id UUID)
RETURNS BIGINT AS $$
DECLARE v_nsr BIGINT;
BEGIN
    SELECT COALESCE(MAX(nsr_logico), 0) + 1 INTO v_nsr
    FROM registro_ponto
    WHERE tenant_id = p_tenant_id AND colaborador_id = p_colaborador_id;
    RETURN v_nsr;
END;
$$ LANGUAGE plpgsql;

-- ============================================================
-- MÓDULOS DA PLATAFORMA (catálogo e associação por tenant/usuário)
-- ============================================================

CREATE TABLE modulo_plataforma (
    id UUID PRIMARY KEY,
    codigo VARCHAR(40) NOT NULL UNIQUE,
    nome VARCHAR(120) NOT NULL,
    descricao VARCHAR(500),
    ativo BOOLEAN DEFAULT TRUE
);

CREATE TABLE empresa_modulo (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenant(id),
    modulo_id UUID NOT NULL REFERENCES modulo_plataforma(id),
    ativado_em TIMESTAMPTZ DEFAULT NOW(),
    CONSTRAINT uk_empresa_modulo UNIQUE (tenant_id, modulo_id)
);

CREATE INDEX idx_empresa_modulo_tenant ON empresa_modulo(tenant_id);
CREATE INDEX idx_empresa_modulo_modulo ON empresa_modulo(modulo_id);

-- Associação usuário ↔ módulo (gating por usuário; backfill dos módulos do tenant)
CREATE TABLE usuario_modulo (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id UUID NOT NULL REFERENCES cpc_usuario(id) ON DELETE CASCADE,
    tenant_id UUID,
    codigo VARCHAR(40) NOT NULL REFERENCES modulo_plataforma(codigo),
    criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_usuario_modulo UNIQUE (usuario_id, codigo)
);

CREATE INDEX idx_usuario_modulo_usuario ON usuario_modulo(usuario_id);

-- ============================================================
-- MÓDULO: Estoque & Almoxarifado
-- ============================================================

CREATE TABLE tb_material_grupo (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenant(id),
    codigo VARCHAR(20) NOT NULL,
    nome VARCHAR(100) NOT NULL,
    ativo BOOLEAN DEFAULT TRUE,
    CONSTRAINT uk_material_grupo_tenant_codigo UNIQUE (tenant_id, codigo)
);

CREATE INDEX idx_material_grupo_tenant ON tb_material_grupo(tenant_id);

CREATE TABLE tb_almoxarifado (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenant(id),
    nome VARCHAR(100) NOT NULL,
    descricao VARCHAR(255),
    responsavel_cpc_id UUID,
    ativo BOOLEAN DEFAULT TRUE
);

CREATE INDEX idx_almoxarifado_tenant ON tb_almoxarifado(tenant_id);

CREATE TABLE tb_material (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenant(id),
    grupo_id UUID NOT NULL REFERENCES tb_material_grupo(id),
    codigo_catmat VARCHAR(20),
    descricao VARCHAR(255) NOT NULL,
    unidade_medida VARCHAR(20) NOT NULL,
    estoque_minimo NUMERIC(12, 3),
    controla_lote_validade BOOLEAN DEFAULT FALSE,
    codigo_barras VARCHAR(32),
    ativo BOOLEAN DEFAULT TRUE
);

CREATE INDEX idx_material_tenant ON tb_material(tenant_id);
CREATE INDEX idx_material_grupo ON tb_material(grupo_id);
CREATE INDEX idx_material_catmat ON tb_material(codigo_catmat);

CREATE TABLE tb_estoque_saldo (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenant(id),
    almoxarifado_id UUID NOT NULL REFERENCES tb_almoxarifado(id),
    material_id UUID NOT NULL REFERENCES tb_material(id),
    lote VARCHAR(50),
    data_validade DATE,
    quantidade_atual NUMERIC(12, 3) NOT NULL DEFAULT 0,
    custo_medio_unitario NUMERIC(15, 4) NOT NULL DEFAULT 0,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_estoque_saldo_tenant ON tb_estoque_saldo(tenant_id);
CREATE INDEX idx_estoque_saldo_almox_mat ON tb_estoque_saldo(tenant_id, almoxarifado_id, material_id);

CREATE TABLE tb_estoque_movimentacao (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenant(id),
    almoxarifado_id UUID NOT NULL REFERENCES tb_almoxarifado(id),
    material_id UUID NOT NULL REFERENCES tb_material(id),
    tipo_movimento VARCHAR(30) NOT NULL,
    quantidade NUMERIC(12, 3) NOT NULL,
    valor_unitario NUMERIC(15, 4) NOT NULL,
    valor_total NUMERIC(15, 4) NOT NULL,
    lote VARCHAR(50),
    data_validade DATE,
    documento_referencia VARCHAR(100),
    usuario_cpc_id UUID NOT NULL,
    data_hora_registro TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    motivo_baixa VARCHAR(30),
    numero_termo VARCHAR(30),
    observacao VARCHAR(255),
    tipo_termo VARCHAR(20)
);

CREATE INDEX idx_estoque_mov_tenant ON tb_estoque_movimentacao(tenant_id);
CREATE INDEX idx_estoque_mov_almox_mat ON tb_estoque_movimentacao(almoxarifado_id, material_id);
CREATE INDEX idx_estoque_mov_data ON tb_estoque_movimentacao(data_hora_registro);

CREATE TABLE tb_requisicao (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenant(id),
    almoxarifado_id UUID NOT NULL REFERENCES tb_almoxarifado(id),
    solicitante_cpc_id UUID NOT NULL,
    departamento VARCHAR(100),
    justificativa TEXT,
    status VARCHAR(30) NOT NULL,
    data_solicitacao TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    data_atendimento TIMESTAMP WITH TIME ZONE,
    atendente_cpc_id UUID,
    CONSTRAINT chk_requisicao_status CHECK (status IN (
        'PENDENTE', 'APROVADA', 'ATENDIDA', 'REJEITADA', 'CANCELADA'
    ))
);

CREATE INDEX idx_requisicao_tenant ON tb_requisicao(tenant_id);
CREATE INDEX idx_requisicao_status ON tb_requisicao(status);
CREATE INDEX idx_requisicao_solicitante ON tb_requisicao(solicitante_cpc_id);

CREATE TABLE tb_requisicao_item (
    id UUID PRIMARY KEY,
    requisicao_id UUID NOT NULL REFERENCES tb_requisicao(id) ON DELETE CASCADE,
    material_id UUID NOT NULL REFERENCES tb_material(id),
    quantidade_solicitada NUMERIC(12, 3) NOT NULL,
    quantidade_atendida NUMERIC(12, 3) DEFAULT 0
);

CREATE INDEX idx_requisicao_item_req ON tb_requisicao_item(requisicao_id);

-- ============================================================
-- MÓDULO: Compras & Fornecedores
-- ============================================================

CREATE TABLE tb_fornecedor (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenant(id),
    cnpj VARCHAR(14) NOT NULL,
    razao_social VARCHAR(180) NOT NULL,
    nome_fantasia VARCHAR(120),
    email VARCHAR(255),
    telefone VARCHAR(20),
    endereco_logradouro VARCHAR(255),
    endereco_numero VARCHAR(20),
    endereco_complemento VARCHAR(100),
    endereco_bairro VARCHAR(100),
    endereco_cidade VARCHAR(100),
    endereco_uf VARCHAR(2),
    endereco_cep VARCHAR(8),
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    inscricao_estadual VARCHAR(30),
    observacoes TEXT,
    ativo BOOLEAN DEFAULT TRUE
);

CREATE INDEX idx_fornecedor_tenant ON tb_fornecedor(tenant_id);
CREATE INDEX idx_fornecedor_cnpj ON tb_fornecedor(cnpj);

-- ============================================================
-- MÓDULO: Licitações & Contratações
-- ============================================================

CREATE TABLE tb_licitacao (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenant(id),
    numero VARCHAR(20) NOT NULL,
    modalidade VARCHAR(30) NOT NULL,
    tipo_julgamento VARCHAR(30) NOT NULL,
    objeto TEXT NOT NULL,
    data_abertura DATE,
    valor_estimado NUMERIC(15, 2),
    observacoes TEXT,
    status VARCHAR(30) NOT NULL DEFAULT 'EM_ELABORACAO',
    pedido_gerado BOOLEAN NOT NULL DEFAULT FALSE,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    contrato_gerado BOOLEAN NOT NULL DEFAULT FALSE,
    pncp_erro TEXT,
    pncp_protocolo VARCHAR(100),
    pncp_publicado_em TIMESTAMPTZ,
    pncp_status VARCHAR(20) NOT NULL DEFAULT 'NAO_PUBLICADO',
    CONSTRAINT uk_licitacao_numero_tenant UNIQUE (tenant_id, numero),
    CONSTRAINT chk_licitacao_modalidade CHECK (modalidade IN ('PREGAO', 'CONCORRENCIA', 'LEILAO', 'CONCURSO', 'DIALOGO_COMPETITIVO')),
    CONSTRAINT chk_licitacao_julgamento CHECK (tipo_julgamento IN ('MENOR_PRECO', 'MAIOR_DESCONTO', 'MELHOR_TECNICA', 'TECNICA_E_PRECO', 'MAIOR_LANCE')),
    CONSTRAINT chk_licitacao_status CHECK (status IN ('EM_ELABORACAO', 'PUBLICADA', 'ABERTA', 'ADJUDICADA', 'HOMOLOGADA', 'CANCELADA'))
);

CREATE INDEX idx_licitacao_tenant ON tb_licitacao(tenant_id);
CREATE INDEX idx_licitacao_numero ON tb_licitacao(numero);

-- ============================================================
-- CONTRATOS (dependem de tb_licitacao)
-- ============================================================

CREATE TABLE contrato (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenant(id),
    numero VARCHAR(50) NOT NULL,
    objeto TEXT NOT NULL,
    data_inicio DATE NOT NULL,
    data_fim DATE NOT NULL,
    valor_mensal NUMERIC(12,2) NOT NULL DEFAULT 0,
    valor_total NUMERIC(12,2) NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'ATIVO',
    observacoes TEXT,
    criado_em TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    atualizado_em TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    empenho_numero VARCHAR(30),
    valor_empenhado NUMERIC(12, 2) NOT NULL DEFAULT 0,
    valor_liquidado NUMERIC(12, 2) NOT NULL DEFAULT 0,
    vencimento_aviso_dias INTEGER NOT NULL DEFAULT 30,
    licitacao_id UUID REFERENCES tb_licitacao(id) ON DELETE SET NULL
);

CREATE TABLE contrato_evento (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    contrato_id UUID NOT NULL REFERENCES contrato(id) ON DELETE CASCADE,
    tipo VARCHAR(30) NOT NULL,
    descricao TEXT NOT NULL,
    criado_por UUID NOT NULL REFERENCES cpc_usuario(id),
    criado_em TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE contrato_aditivo (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenant(id),
    contrato_id UUID NOT NULL REFERENCES contrato(id) ON DELETE CASCADE,
    tipo VARCHAR(20) NOT NULL,
    descricao TEXT NOT NULL,
    justificativa TEXT,
    prazo_adicionado_dias INTEGER,
    novo_valor_total NUMERIC(12,2),
    aprovado BOOLEAN NOT NULL DEFAULT FALSE,
    criado_por UUID NOT NULL REFERENCES cpc_usuario(id),
    criado_em TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE contrato_apontamento (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenant(id),
    contrato_id UUID NOT NULL REFERENCES contrato(id) ON DELETE CASCADE,
    fiscal VARCHAR(100) NOT NULL,
    descricao TEXT NOT NULL,
    gravidade VARCHAR(20) NOT NULL DEFAULT 'MEDIA',
    resolvido BOOLEAN NOT NULL DEFAULT FALSE,
    resolvido_em TIMESTAMP WITH TIME ZONE,
    criado_por UUID NOT NULL REFERENCES cpc_usuario(id),
    criado_em TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE contrato_medicao (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenant(id),
    contrato_id UUID NOT NULL REFERENCES contrato(id) ON DELETE CASCADE,
    periodo VARCHAR(20) NOT NULL,
    valor_medido NUMERIC(12,2) NOT NULL DEFAULT 0,
    valor_pago NUMERIC(12,2) NOT NULL DEFAULT 0,
    pago_em DATE,
    observacao TEXT,
    criado_por UUID NOT NULL REFERENCES cpc_usuario(id),
    criado_em TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE contrato_sancao (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenant(id),
    contrato_id UUID NOT NULL REFERENCES contrato(id) ON DELETE CASCADE,
    tipo VARCHAR(30) NOT NULL,
    base_legal TEXT,
    descricao TEXT NOT NULL,
    percentual_multa NUMERIC(6,3),
    valor_multa NUMERIC(12,2),
    aplicada_em DATE NOT NULL,
    criado_por UUID NOT NULL REFERENCES cpc_usuario(id),
    criado_em TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE contrato_rescisao (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenant(id),
    contrato_id UUID NOT NULL REFERENCES contrato(id) ON DELETE CASCADE,
    tipo VARCHAR(20) NOT NULL,
    motivo TEXT NOT NULL,
    data_rescisao DATE NOT NULL,
    criado_por UUID NOT NULL REFERENCES cpc_usuario(id),
    criado_em TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE tb_pedido_compra (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenant(id),
    numero VARCHAR(20) NOT NULL,
    fornecedor_id UUID NOT NULL REFERENCES tb_fornecedor(id),
    data_emissao DATE NOT NULL,
    data_prevista_entrega DATE,
    valor_total NUMERIC(15, 2) NOT NULL,
    observacoes TEXT,
    status VARCHAR(30) NOT NULL DEFAULT 'EMITIDO',
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    contrato_id UUID REFERENCES contrato(id),
    criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    empenho_numero VARCHAR(30),
    objeto VARCHAR(255),
    prazo_entrega DATE,
    CONSTRAINT chk_pedido_status CHECK (status IN (
        'EMITIDO', 'PARCIALMENTE_RECEBIDO', 'RECEBIDO', 'CANCELADO'
    ))
);

CREATE INDEX idx_pedido_tenant ON tb_pedido_compra(tenant_id);

CREATE TABLE tb_pedido_compra_item (
    id UUID PRIMARY KEY,
    pedido_id UUID NOT NULL REFERENCES tb_pedido_compra(id) ON DELETE CASCADE,
    material_id UUID NOT NULL REFERENCES tb_material(id),
    quantidade NUMERIC(12, 3) NOT NULL,
    valor_unitario NUMERIC(15, 4) NOT NULL,
    valor_total NUMERIC(15, 4) NOT NULL,
    quantidade_recebida NUMERIC(15, 3) NOT NULL DEFAULT 0,
    tenant_id UUID NOT NULL REFERENCES tenant(id)
);

-- ============================================================
-- MÓDULO: Patrimônio Público
-- ============================================================

CREATE TABLE tb_patrimonio (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenant(id),
    tombamento VARCHAR(30),
    descricao VARCHAR(255) NOT NULL,
    categoria VARCHAR(60),
    estado VARCHAR(30) NOT NULL DEFAULT 'BOM',
    localizacao VARCHAR(120),
    data_aquisicao DATE,
    valor_aquisicao NUMERIC(15, 2),
    responsavel_nome VARCHAR(120),
    numero_nota_fiscal VARCHAR(30),
    observacoes TEXT,
    data_inicio_depreciacao DATE,
    taxa_depreciacao_mensal NUMERIC(5, 4),
    valor_depreciado NUMERIC(15, 2) NOT NULL DEFAULT 0,
    vida_util_meses INTEGER,
    ativo BOOLEAN DEFAULT TRUE,
    CONSTRAINT chk_patrimonio_estado CHECK (estado IN ('NOVO', 'OTIMO', 'BOM', 'REGULAR', 'INSERVIVEL'))
);

CREATE INDEX idx_patrimonio_tenant ON tb_patrimonio(tenant_id);
CREATE INDEX idx_patrimonio_tombamento ON tb_patrimonio(tombamento);

CREATE TABLE tb_inventario (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenant(id),
    descricao VARCHAR(200) NOT NULL,
    data_inicio DATE NOT NULL,
    data_fim DATE,
    status VARCHAR(20) NOT NULL DEFAULT 'EM_ANDAMENTO',
    criado_por VARCHAR(120),
    criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_inventario_status CHECK (status IN ('EM_ANDAMENTO', 'CONCLUIDO', 'CANCELADO'))
);

CREATE TABLE tb_inventario_item (
    id UUID PRIMARY KEY,
    inventario_id UUID NOT NULL REFERENCES tb_inventario(id),
    patrimonio_id UUID NOT NULL REFERENCES tb_patrimonio(id),
    patrimonio_tombamento VARCHAR(30),
    patrimonio_descricao VARCHAR(255),
    conferido BOOLEAN NOT NULL DEFAULT FALSE,
    conferido_por VARCHAR(120),
    data_conferencia TIMESTAMPTZ,
    resultado VARCHAR(20),
    observacao TEXT,
    CONSTRAINT uk_inventario_item UNIQUE (inventario_id, patrimonio_id),
    CONSTRAINT chk_inventario_item_resultado CHECK (resultado IN ('CONFORME', 'DIVERGENCIA'))
);

CREATE TABLE tb_transferencia_patrimonio (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenant(id),
    patrimonio_id UUID NOT NULL REFERENCES tb_patrimonio(id),
    localizacao_origem VARCHAR(120),
    localizacao_destino VARCHAR(120) NOT NULL,
    responsavel_origem VARCHAR(120),
    responsavel_destino VARCHAR(120),
    data_solicitacao TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    data_prevista DATE,
    data_efetivacao TIMESTAMPTZ,
    status VARCHAR(20) NOT NULL DEFAULT 'SOLICITADA',
    justificativa TEXT,
    aprovado_por VARCHAR(120),
    solicitado_por VARCHAR(120),
    criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_transferencia_status CHECK (status IN ('SOLICITADA', 'EM_TRANSITO', 'CONFIRMADA', 'CANCELADA'))
);

CREATE TABLE tb_desfazimento (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenant(id),
    patrimonio_id UUID NOT NULL REFERENCES tb_patrimonio(id),
    estado_bem VARCHAR(30) NOT NULL,
    tipo_desfazimento VARCHAR(30) NOT NULL,
    justificativa TEXT NOT NULL,
    responsavel_solicitacao VARCHAR(150) NOT NULL,
    data_solicitacao TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    parecer_comissao TEXT,
    aprovado BOOLEAN,
    data_aprovacao TIMESTAMP WITH TIME ZONE,
    data_baixa TIMESTAMP WITH TIME ZONE,
    status VARCHAR(30) NOT NULL DEFAULT 'EM_ANALISE',
    processo_numero VARCHAR(40),
    observacoes TEXT,
    criado_em TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    CONSTRAINT chk_desfazimento_estado_bem CHECK (estado_bem IN ('OCIOSO', 'RECUPERAVEL', 'ANTIECONOMICO', 'IRRECUPERAVEL')),
    CONSTRAINT chk_desfazimento_tipo CHECK (tipo_desfazimento IN ('DOACAO', 'TROCA', 'VENDA', 'CEDENCIA', 'RECICLAGEM', 'OUTROS')),
    CONSTRAINT chk_desfazimento_status CHECK (status IN ('EM_ANALISE', 'APROVADO', 'CANCELADO'))
);

CREATE TABLE tb_desfazimento_comissao (
    id UUID PRIMARY KEY,
    desfazimento_id UUID NOT NULL REFERENCES tb_desfazimento(id),
    membro_nome VARCHAR(150) NOT NULL,
    membro_cargo VARCHAR(120),
    membro_cpf VARCHAR(20),
    relator BOOLEAN DEFAULT FALSE,
    parecer TEXT,
    criado_em TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- ============================================================
-- MÓDULO: Frota
-- ============================================================

CREATE TABLE tb_frota_veiculo (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenant(id),
    placa VARCHAR(10) NOT NULL,
    renavam VARCHAR(20),
    marca VARCHAR(60),
    modelo VARCHAR(60),
    ano_fabricacao INTEGER,
    ano_modelo INTEGER,
    tipo VARCHAR(30),
    combustivel VARCHAR(30),
    status VARCHAR(20) NOT NULL DEFAULT 'ATIVO',
    odometro_atual NUMERIC(12, 1),
    observacoes TEXT,
    ativo BOOLEAN DEFAULT TRUE,
    CONSTRAINT chk_frota_status CHECK (status IN ('ATIVO', 'MANUTENCAO', 'INATIVO'))
);

CREATE INDEX idx_frota_veiculo_tenant ON tb_frota_veiculo(tenant_id);
CREATE INDEX idx_frota_veiculo_placa ON tb_frota_veiculo(placa);

CREATE TABLE tb_frota_abastecimento (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenant(id),
    veiculo_id UUID NOT NULL REFERENCES tb_frota_veiculo(id),
    data_hora TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    litros NUMERIC(12, 3) NOT NULL,
    valor_litro NUMERIC(15, 4) NOT NULL,
    valor_total NUMERIC(15, 4) NOT NULL,
    odometro_km NUMERIC(12, 1),
    posto VARCHAR(120),
    observacoes TEXT
);

CREATE INDEX idx_frota_abastecimento_tenant ON tb_frota_abastecimento(tenant_id);
CREATE INDEX idx_frota_abastecimento_veiculo ON tb_frota_abastecimento(veiculo_id);

-- ============================================================
-- MÓDULO: Protocolo & Tramitação
-- ============================================================

CREATE TABLE tb_protocolo (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenant(id),
    numero_protocolo VARCHAR(30) NOT NULL,
    tipo VARCHAR(40) NOT NULL,
    assunto VARCHAR(255) NOT NULL,
    descricao TEXT,
    remetente VARCHAR(150),
    destinatario VARCHAR(150),
    data_protocolo TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    status VARCHAR(30) NOT NULL DEFAULT 'RECEBIDO',
    responsavel VARCHAR(120),
    observacoes TEXT,
    ativo BOOLEAN DEFAULT TRUE,
    CONSTRAINT chk_protocolo_status CHECK (status IN ('RECEBIDO', 'TRIAGEM', 'EM_TRAMITACAO', 'ARQUIVADO', 'CANCELADO'))
);

CREATE INDEX idx_protocolo_tenant ON tb_protocolo(tenant_id);
CREATE INDEX idx_protocolo_numero ON tb_protocolo(numero_protocolo);

-- ============================================================
-- ADMIN PLATAFORMA (sem CPF, sem tenant; login em /admin/auth)
-- ============================================================

CREATE TABLE admin_plataforma (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(20) UNIQUE NOT NULL,
    senha_hash VARCHAR(255) NOT NULL,
    nome_completo VARCHAR(100),
    email VARCHAR(100) UNIQUE,
    ultimo_login TIMESTAMP WITH TIME ZONE,
    tentativas_login_falhas INTEGER DEFAULT 0,
    bloqueio_login_ate TIMESTAMP WITH TIME ZONE,
    ativo BOOLEAN DEFAULT TRUE,
    two_factor_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    two_factor_secret VARCHAR(64),
    criado_em TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    atualizado_em TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_admin_plataforma_username ON admin_plataforma(username);
CREATE INDEX idx_admin_plataforma_email ON admin_plataforma(email);

-- Códigos de recuperação de acesso do Administrator (8 códigos, exibidos
-- uma única vez no fim do bootstrap/confirm 2FA; hash SHA-256, uso único)
CREATE TABLE admin_recovery_code (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    admin_id UUID NOT NULL REFERENCES admin_plataforma(id) ON DELETE CASCADE,
    code_hash VARCHAR(64) NOT NULL,
    used_at TIMESTAMP WITH TIME ZONE,
    criado_em TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_admin_recovery_code_admin ON admin_recovery_code(admin_id);

-- Transferência de titularidade da empresa (wizard /perfil/titularidade):
-- 3 etapas (biometria, OTP do titular atual + confirmação do celular do novo
-- titular, OTP do e-mail corporativo do novo titular); TTL de 30 minutos.
CREATE TABLE titularidade_transferencia (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenant(id),
    solicitante_id UUID NOT NULL REFERENCES cpc_usuario(id),
    novo_titular_id UUID NOT NULL REFERENCES cpc_usuario(id),
    status VARCHAR(20) NOT NULL DEFAULT 'EM_ANDAMENTO',
    etapa_biometria BOOLEAN NOT NULL DEFAULT FALSE,
    etapa_celular BOOLEAN NOT NULL DEFAULT FALSE,
    etapa_email BOOLEAN NOT NULL DEFAULT FALSE,
    expira_em TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT (NOW() + INTERVAL '30 minutes'),
    concluida_em TIMESTAMP WITH TIME ZONE,
    criado_em TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_titularidade_status CHECK (status IN ('EM_ANDAMENTO', 'CONCLUIDA', 'CANCELADA'))
);

CREATE INDEX idx_titularidade_tenant ON titularidade_transferencia(tenant_id);
CREATE INDEX idx_titularidade_solicitante ON titularidade_transferencia(solicitante_id);

-- Códigos OTP (6 dígitos, hash bcrypt, validade 15 min, uso único) por etapa.
CREATE TABLE titularidade_codigo (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    transferencia_id UUID NOT NULL REFERENCES titularidade_transferencia(id) ON DELETE CASCADE,
    etapa VARCHAR(20) NOT NULL,
    codigo_hash VARCHAR(255) NOT NULL,
    expira_em TIMESTAMP WITH TIME ZONE NOT NULL,
    usado_em TIMESTAMP WITH TIME ZONE,
    criado_em TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_titularidade_etapa CHECK (etapa IN ('CELULAR', 'EMAIL'))
);

CREATE INDEX idx_titularidade_codigo_transferencia ON titularidade_codigo(transferencia_id);

-- ============================================================
-- CONFIGURAÇÕES, AUDITORIA E DIVERSOS
-- ============================================================

CREATE TABLE configuracao_fiscal (
    tenant_id UUID PRIMARY KEY REFERENCES tenant(id),
    numero_registro_inpi VARCHAR(30),
    cnpj_desenvolvedor VARCHAR(14),
    prtp_nome VARCHAR(150) NOT NULL DEFAULT 'CHRONOS PULSE',
    prtp_versao VARCHAR(20) NOT NULL DEFAULT '1.0.0',
    prtp_razao_desenv VARCHAR(150),
    prtp_email VARCHAR(50),
    cno VARCHAR(12),
    atualizado_em TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE TABLE tb_telemetria_evento (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID REFERENCES tenant(id),
    usuario_id UUID,
    modulo VARCHAR(30) NOT NULL,
    tipo VARCHAR(30) NOT NULL,
    endpoint VARCHAR(255),
    status_http INTEGER,
    latency_ms BIGINT,
    mensagem VARCHAR(500),
    detalhe TEXT,
    trace_id VARCHAR(64),
    app_versao VARCHAR(30),
    plataforma VARCHAR(20),
    criado_em TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_telemetria_tenant ON tb_telemetria_evento(tenant_id);
CREATE INDEX idx_telemetria_usuario ON tb_telemetria_evento(usuario_id);
CREATE INDEX idx_telemetria_modulo ON tb_telemetria_evento(modulo);
CREATE INDEX idx_telemetria_tipo ON tb_telemetria_evento(tipo);
CREATE INDEX idx_telemetria_criado_em ON tb_telemetria_evento(criado_em);

CREATE TABLE recuperacao_senha (
    id UUID PRIMARY KEY,
    cpf VARCHAR(11) NOT NULL,
    codigo_hash VARCHAR(255) NOT NULL,
    expira_em TIMESTAMPTZ NOT NULL,
    usado BOOLEAN DEFAULT FALSE,
    criado_em TIMESTAMPTZ DEFAULT NOW(),
    tentativas INTEGER NOT NULL DEFAULT 0
);

CREATE INDEX idx_recuperacao_senha_cpf ON recuperacao_senha (cpf);

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

CREATE INDEX idx_auditoria_entidade ON tb_auditoria(entidade, entidade_id);
CREATE INDEX idx_auditoria_tenant_data ON tb_auditoria(tenant_id, data_hora);

CREATE TABLE tb_consentimento_privacidade (
    id                 UUID PRIMARY KEY,
    cpc_id             UUID NOT NULL,
    tenant_id          UUID,
    versao_politica    VARCHAR(50)  NOT NULL,
    data_consentimento TIMESTAMP WITH TIME ZONE NOT NULL,
    aceito             BOOLEAN NOT NULL DEFAULT TRUE,
    ip_origem          VARCHAR(64),
    user_agent         VARCHAR(512),
    hash_termo         VARCHAR(64),
    UNIQUE (cpc_id, versao_politica)
);

CREATE INDEX idx_consentimento_privacidade_cpc ON tb_consentimento_privacidade(cpc_id);

CREATE TABLE tb_lead_empresa (
    id                  UUID PRIMARY KEY,
    cnpj                VARCHAR(14) NOT NULL,
    razao_social        VARCHAR(200) NOT NULL,
    contato_nome        VARCHAR(120) NOT NULL,
    contato_email       VARCHAR(160) NOT NULL,
    contato_telefone    VARCHAR(20),
    contato_celular     VARCHAR(20),
    endereco_logradouro VARCHAR(255),
    endereco_numero     VARCHAR(20),
    endereco_complemento VARCHAR(120),
    endereco_bairro     VARCHAR(120),
    endereco_cidade     VARCHAR(120),
    endereco_uf         VARCHAR(2),
    endereco_cep        VARCHAR(8),
    observacao          VARCHAR(500),
    status              VARCHAR(20) NOT NULL DEFAULT 'NOVO',
    criado_em           TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_lead_empresa_criado_em ON tb_lead_empresa (criado_em DESC);
CREATE INDEX idx_lead_empresa_status ON tb_lead_empresa (status);

CREATE TABLE acesso_dados_sensiveis (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    role VARCHAR(30) NOT NULL,
    pode_acessar_dados_clientes BOOLEAN DEFAULT FALSE,
    pode_acessar_dados_colaboradores_terceiros BOOLEAN DEFAULT FALSE,
    pode_exportar_dados_pessoais BOOLEAN DEFAULT FALSE,
    observacao VARCHAR(500),
    UNIQUE (role)
);

-- ============================================================
-- TRANSPARÊNCIA (LC 131/2009)
-- ============================================================

CREATE TABLE tb_transparencia_publicacao (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenant(id),
    competencia VARCHAR(7) NOT NULL,
    tipo_publicacao VARCHAR(30) NOT NULL,
    valor_total NUMERIC(15, 2) NOT NULL DEFAULT 0,
    itens_count INTEGER NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'EM_ELABORACAO',
    data_publicacao DATE,
    observacoes TEXT,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_publicacao_competencia_tipo UNIQUE (tenant_id, competencia, tipo_publicacao),
    CONSTRAINT chk_publicacao_competencia CHECK (competencia ~ '^\d{4}-(0[1-9]|1[0-2])$'),
    CONSTRAINT chk_publicacao_tipo CHECK (tipo_publicacao IN ('RECEITAS', 'DESPESAS', 'COMPRAS', 'LICITACOES', 'CONTRATOS', 'FROTA', 'PATRIMONIO', 'FOLHA')),
    CONSTRAINT chk_publicacao_status CHECK (status IN ('EM_ELABORACAO', 'PUBLICADO'))
);

CREATE INDEX idx_transparencia_competencia ON tb_transparencia_publicacao(tenant_id, competencia);
CREATE INDEX idx_transparencia_tenant ON tb_transparencia_publicacao(tenant_id);

-- ============================================================
-- COMPRAS (requisições, cotações, NFe)
-- ============================================================

CREATE TABLE tb_requisicao_compra (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenant(id),
    numero VARCHAR(20) NOT NULL,
    solicitante_cpc_id UUID NOT NULL REFERENCES cpc_usuario(id),
    justificativa TEXT NOT NULL,
    data_requisicao DATE NOT NULL DEFAULT CURRENT_DATE,
    observacoes TEXT,
    status VARCHAR(30) NOT NULL DEFAULT 'EM_ABERTO',
    criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_requisicao_numero_tenant UNIQUE (tenant_id, numero),
    CONSTRAINT chk_req_compra_status CHECK (status IN ('EM_ABERTO', 'COTADA', 'CANCELADA'))
);

CREATE TABLE tb_requisicao_compra_item (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenant(id),
    requisicao_id UUID NOT NULL REFERENCES tb_requisicao_compra(id),
    material_id UUID NOT NULL REFERENCES tb_material(id),
    quantidade NUMERIC(15, 3) NOT NULL,
    observacao VARCHAR(255)
);

CREATE TABLE tb_cotacao_compra (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenant(id),
    numero VARCHAR(20) NOT NULL,
    requisicao_id UUID NOT NULL REFERENCES tb_requisicao_compra(id),
    data_limite DATE,
    observacoes TEXT,
    status VARCHAR(30) NOT NULL DEFAULT 'EM_ANDAMENTO',
    pedido_gerado BOOLEAN NOT NULL DEFAULT FALSE,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_cotacao_numero_tenant UNIQUE (tenant_id, numero),
    CONSTRAINT uk_cotacao_requisicao UNIQUE (requisicao_id),
    CONSTRAINT chk_cotacao_status CHECK (status IN ('EM_ANDAMENTO', 'CONCLUIDA', 'CANCELADA'))
);

CREATE TABLE tb_cotacao_fornecedor (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenant(id),
    cotacao_id UUID NOT NULL REFERENCES tb_cotacao_compra(id),
    fornecedor_id UUID NOT NULL REFERENCES tb_fornecedor(id),
    CONSTRAINT uk_cotacao_fornecedor UNIQUE (cotacao_id, fornecedor_id)
);

CREATE TABLE tb_cotacao_proposta (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenant(id),
    cotacao_id UUID NOT NULL REFERENCES tb_cotacao_compra(id),
    fornecedor_id UUID NOT NULL REFERENCES tb_fornecedor(id),
    material_id UUID NOT NULL REFERENCES tb_material(id),
    valor_unitario NUMERIC(15, 4) NOT NULL,
    observacao VARCHAR(255),
    vencedor BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT uk_cotacao_proposta UNIQUE (cotacao_id, fornecedor_id, material_id)
);

CREATE TABLE tb_entrada_nfe (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenant(id),
    chave_nfe VARCHAR(44) NOT NULL,
    numero_nfe VARCHAR(9),
    serie VARCHAR(3),
    data_emissao DATE,
    valor_nota NUMERIC(15, 2),
    fornecedor_id UUID REFERENCES tb_fornecedor(id),
    pedido_id UUID NOT NULL REFERENCES tb_pedido_compra(id),
    contrato_id UUID REFERENCES contrato(id),
    empenho_numero VARCHAR(30),
    almoxarifado_id UUID NOT NULL REFERENCES tb_almoxarifado(id),
    tipo_termo VARCHAR(20) NOT NULL DEFAULT 'DEFINITIVO',
    numero_termo VARCHAR(30),
    observacoes TEXT,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    razao_emitente VARCHAR(160),
    xml_nfe TEXT,
    cnpj_emitente VARCHAR(14),
    CONSTRAINT chk_entrada_nfe_tipo_termo CHECK (tipo_termo IN ('PROVISORIO', 'DEFINITIVO'))
);

CREATE INDEX idx_entrada_nfe_chave ON tb_entrada_nfe(chave_nfe);
CREATE INDEX idx_entrada_nfe_emitente ON tb_entrada_nfe(tenant_id, cnpj_emitente);
CREATE INDEX idx_entrada_nfe_tenant ON tb_entrada_nfe(tenant_id);

-- ============================================================
-- LICITAÇÕES — itens, participantes, planejamento e lances
-- ============================================================

CREATE TABLE tb_licitacao_item (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenant(id),
    licitacao_id UUID NOT NULL REFERENCES tb_licitacao(id),
    material_id UUID NOT NULL REFERENCES tb_material(id),
    descricao VARCHAR(255) NOT NULL,
    quantidade NUMERIC(15, 3) NOT NULL,
    valor_estimado_unitario NUMERIC(15, 4)
);

CREATE TABLE tb_licitacao_participante (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenant(id),
    licitacao_id UUID NOT NULL REFERENCES tb_licitacao(id),
    fornecedor_id UUID NOT NULL REFERENCES tb_fornecedor(id),
    habilitado BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uk_licitacao_participante UNIQUE (licitacao_id, fornecedor_id)
);

CREATE TABLE tb_licitacao_proposta (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenant(id),
    licitacao_id UUID NOT NULL REFERENCES tb_licitacao(id),
    fornecedor_id UUID NOT NULL REFERENCES tb_fornecedor(id),
    material_id UUID NOT NULL REFERENCES tb_material(id),
    valor_unitario NUMERIC(15, 4) NOT NULL,
    vencedor BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT uk_licitacao_proposta UNIQUE (licitacao_id, fornecedor_id, material_id)
);

CREATE TABLE tb_licitacao_etp (
    id UUID PRIMARY KEY,
    licitacao_id UUID NOT NULL UNIQUE REFERENCES tb_licitacao(id),
    tenant_id UUID NOT NULL REFERENCES tenant(id),
    objeto TEXT NOT NULL,
    justificativa TEXT NOT NULL,
    requisitos TEXT NOT NULL,
    alternativas TEXT,
    valor_estimado NUMERIC(15, 2),
    riscos TEXT,
    conclusao TEXT,
    responsavel VARCHAR(255),
    status VARCHAR(20) NOT NULL DEFAULT 'RASCUNHO',
    data_aprovacao TIMESTAMPTZ,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_etp_status CHECK (status IN ('RASCUNHO', 'APROVADO'))
);

CREATE TABLE tb_licitacao_tr (
    id UUID PRIMARY KEY,
    licitacao_id UUID NOT NULL UNIQUE REFERENCES tb_licitacao(id),
    etp_id UUID NOT NULL UNIQUE REFERENCES tb_licitacao_etp(id),
    tenant_id UUID NOT NULL REFERENCES tenant(id),
    especificacoes TEXT NOT NULL,
    condicoes_fornecimento TEXT NOT NULL,
    obrigacoes TEXT NOT NULL,
    criterios_aceitacao TEXT NOT NULL,
    prazos_entrega TEXT NOT NULL,
    garantia TEXT,
    forma_pagamento TEXT,
    responsavel VARCHAR(255),
    status VARCHAR(20) NOT NULL DEFAULT 'RASCUNHO',
    data_aprovacao TIMESTAMPTZ,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_tr_status CHECK (status IN ('RASCUNHO', 'APROVADO'))
);

CREATE TABLE tb_licitacao_edital (
    id UUID PRIMARY KEY,
    licitacao_id UUID NOT NULL UNIQUE REFERENCES tb_licitacao(id),
    tr_id UUID REFERENCES tb_licitacao_tr(id),
    tenant_id UUID NOT NULL REFERENCES tenant(id),
    numero_processo VARCHAR(40),
    numero_edital VARCHAR(40),
    local_sessao VARCHAR(255),
    data_abertura_sessao DATE,
    horario_abertura TIME,
    forma_entrega_propostas VARCHAR(20),
    anexos TEXT,
    observacoes TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'EM_ELABORACAO',
    data_publicacao TIMESTAMPTZ,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_edital_status CHECK (status IN ('EM_ELABORACAO', 'PUBLICADO')),
    CONSTRAINT chk_edital_forma_entrega CHECK (
        forma_entrega_propostas IS NULL OR
        forma_entrega_propostas IN ('PRESENCIAL', 'ELETRONICA'))
);

CREATE TABLE tb_licitacao_lance (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenant(id),
    licitacao_id UUID NOT NULL REFERENCES tb_licitacao(id),
    licitacao_item_id UUID NOT NULL REFERENCES tb_licitacao_item(id),
    fornecedor_id UUID NOT NULL REFERENCES tb_fornecedor(id),
    valor_unitario NUMERIC(15, 4) NOT NULL,
    observacao VARCHAR(255),
    criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_lance_corrente UNIQUE (licitacao_id, licitacao_item_id, fornecedor_id)
);

CREATE INDEX idx_licitacao_item_licitacao ON tb_licitacao_item(licitacao_id);
CREATE INDEX idx_licitacao_item_tenant ON tb_licitacao_item(tenant_id);
CREATE INDEX idx_licitacao_participante_licitacao ON tb_licitacao_participante(licitacao_id);
CREATE INDEX idx_licitacao_participante_tenant ON tb_licitacao_participante(tenant_id);
CREATE INDEX idx_licitacao_proposta_licitacao ON tb_licitacao_proposta(licitacao_id);
CREATE INDEX idx_licitacao_proposta_tenant ON tb_licitacao_proposta(tenant_id);
CREATE INDEX idx_licitacao_etp_licitacao ON tb_licitacao_etp(licitacao_id);
CREATE INDEX idx_licitacao_etp_tenant ON tb_licitacao_etp(tenant_id);
CREATE INDEX idx_licitacao_tr_licitacao ON tb_licitacao_tr(licitacao_id);
CREATE INDEX idx_licitacao_tr_tenant ON tb_licitacao_tr(tenant_id);
CREATE INDEX idx_licitacao_edital_licitacao ON tb_licitacao_edital(licitacao_id);
CREATE INDEX idx_licitacao_edital_tenant ON tb_licitacao_edital(tenant_id);
CREATE INDEX idx_lance_fornecedor ON tb_licitacao_lance(fornecedor_id);
CREATE INDEX idx_lance_item ON tb_licitacao_lance(licitacao_item_id);
CREATE INDEX idx_lance_licitacao ON tb_licitacao_lance(licitacao_id);
CREATE INDEX idx_lance_tenant ON tb_licitacao_lance(tenant_id);

CREATE INDEX idx_contrato_aditivo_contrato_id ON contrato_aditivo(contrato_id);
CREATE INDEX idx_contrato_apontamento_contrato_id ON contrato_apontamento(contrato_id);
CREATE INDEX idx_contrato_evento_contrato_id ON contrato_evento(contrato_id);
CREATE INDEX idx_contrato_licitacao_id ON contrato(licitacao_id);
CREATE INDEX idx_contrato_medicao_contrato_id ON contrato_medicao(contrato_id);
CREATE INDEX idx_contrato_rescisao_contrato_id ON contrato_rescisao(contrato_id);
CREATE INDEX idx_contrato_sancao_contrato_id ON contrato_sancao(contrato_id);
CREATE INDEX idx_contrato_status ON contrato(status);
CREATE INDEX idx_contrato_tenant_id ON contrato(tenant_id);

CREATE INDEX idx_cotacao_fornecedor_cotacao ON tb_cotacao_fornecedor(cotacao_id);
CREATE INDEX idx_cotacao_fornecedor_tenant ON tb_cotacao_fornecedor(tenant_id);
CREATE INDEX idx_cotacao_proposta_cotacao ON tb_cotacao_proposta(cotacao_id);
CREATE INDEX idx_cotacao_proposta_tenant ON tb_cotacao_proposta(tenant_id);
CREATE INDEX idx_cotacao_requisicao ON tb_cotacao_compra(requisicao_id);
CREATE INDEX idx_cotacao_tenant ON tb_cotacao_compra(tenant_id);
CREATE INDEX idx_desfazimento_comissao_desfazimento ON tb_desfazimento_comissao(desfazimento_id);
CREATE INDEX idx_desfazimento_patrimonio ON tb_desfazimento(patrimonio_id);
CREATE INDEX idx_desfazimento_status ON tb_desfazimento(status);
CREATE INDEX idx_desfazimento_tenant ON tb_desfazimento(tenant_id);
CREATE INDEX idx_inventario_item_inventario ON tb_inventario_item(inventario_id);
CREATE INDEX idx_inventario_item_patrimonio ON tb_inventario_item(patrimonio_id);
CREATE INDEX idx_inventario_status ON tb_inventario(status);
CREATE INDEX idx_inventario_tenant ON tb_inventario(tenant_id);
CREATE INDEX idx_req_compra_solicitante ON tb_requisicao_compra(solicitante_cpc_id);
CREATE INDEX idx_req_compra_tenant ON tb_requisicao_compra(tenant_id);
CREATE INDEX idx_requisicao_item_requisicao ON tb_requisicao_compra_item(requisicao_id);
CREATE INDEX idx_requisicao_item_tenant ON tb_requisicao_compra_item(tenant_id);
CREATE INDEX idx_transferencia_patrimonio ON tb_transferencia_patrimonio(patrimonio_id);
CREATE INDEX idx_transferencia_status ON tb_transferencia_patrimonio(status);
CREATE INDEX idx_transferencia_tenant ON tb_transferencia_patrimonio(tenant_id);

-- ============================================================
-- SEEDS
-- ============================================================

-- Catálogo de módulos da plataforma
INSERT INTO modulo_plataforma (id, codigo, nome, descricao, ativo) VALUES
    ('11111111-1111-4111-8111-111111111101', 'PONTO', 'Ponto Eletrônico', 'Registro, espelho e exportação fiscal de ponto eletrônico', true),
    ('11111111-1111-4111-8111-111111111102', 'RECURSOS_HUMANOS', 'Recursos Humanos', 'Cadastro de colaboradores e gestão de equipe', true),
    ('11111111-1111-4111-8111-111111111103', 'ESTOQUE', 'Estoque & Almoxarifado', 'Controle de saldos, entradas, saídas e requisições', true),
    ('11111111-1111-4111-8111-111111111104', 'PATRIMONIO', 'Patrimônio Público', 'Tombamento e controle de bens patrimoniais', true),
    ('11111111-1111-4111-8111-111111111105', 'FROTA', 'Gestão de Frota', 'Veículos, abastecimentos e quilometragem', true),
    ('11111111-1111-4111-8111-111111111106', 'PROTOCOLO', 'Protocolo & Tramitação', 'Protocolo eletrônico de documentos e processos', true),
    ('11111111-1111-4111-8111-111111111107', 'COMPRAS', 'Compras & Fornecedores', 'Requisições, cotações, pedidos e NFe', true),
    ('11111111-1111-4111-8111-111111111108', 'LICITACOES', 'Licitações & Contratações', 'Licitações conforme Lei 14.133/2021', true),
    ('11111111-1111-4111-8111-111111111109', 'TRANSPARENCIA', 'Portal da Transparência', 'Portal público LC 131/2009', true),
    ('11111111-1111-4111-8111-111111111110', 'PRIVACIDADE', 'LGPD & Privacidade', 'Gestão de consentimento e dados pessoais', true)
ON CONFLICT (codigo) DO NOTHING;

-- Tenant de demonstração (ônus dos usuários de demonstração)
INSERT INTO tenant (id, nome, cnpj, slug, razao_social, nome_fantasia, produto, ativo, criado_em)
VALUES (
    'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
    'Demonstração',
    '01001001000101',
    'demonstracao',
    'Demonstração',
    'Chronos Pulse',
    'Chronos Suite',
    true,
    NOW()
)
ON CONFLICT (cnpj) DO NOTHING;

-- Tenant LJ Code (ex-Red Cape)
INSERT INTO tenant (id, nome, cnpj, slug, razao_social, nome_fantasia, produto, ativo, criado_em)
VALUES (
    'a0eebc99-0009-0009-0009-6bb9bd380a09',
    'LJ Code',
    '49262262000113',
    'lj-code',
    'LJ CODE QUALIDADE E SEGURANCA CIBERNETICA LTDA',
    'LJ Code',
    'Chronos Suite',
    true,
    NOW()
)
ON CONFLICT (cnpj) DO UPDATE SET
    nome = EXCLUDED.nome,
    slug = EXCLUDED.slug,
    razao_social = EXCLUDED.razao_social,
    nome_fantasia = EXCLUDED.nome_fantasia,
    produto = EXCLUDED.produto,
    ativo = EXCLUDED.ativo;

-- Módulos core para todos os tenants
INSERT INTO empresa_modulo (id, tenant_id, modulo_id, ativado_em)
SELECT gen_random_uuid()::uuid, t.id, m.id, NOW()
FROM tenant t
JOIN modulo_plataforma m ON m.codigo IN ('PONTO', 'RECURSOS_HUMANOS', 'ESTOQUE')
ON CONFLICT (tenant_id, modulo_id) DO NOTHING;

-- Tenant demo possui os 9 módulos (todos exceto PRIVACIDADE)
INSERT INTO empresa_modulo (id, tenant_id, modulo_id, ativado_em)
SELECT gen_random_uuid()::uuid, 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', m.id, NOW()
FROM modulo_plataforma m
WHERE m.codigo IN (
    'PONTO', 'RECURSOS_HUMANOS', 'ESTOQUE', 'PATRIMONIO', 'FROTA',
    'PROTOCOLO', 'COMPRAS', 'LICITACOES', 'TRANSPARENCIA'
)
ON CONFLICT (tenant_id, modulo_id) DO NOTHING;

-- Admin Plataforma: NÃO há seed em produção (zero-trace).
-- Provisionamento via first-run wizard: POST /admin/auth/bootstrap,
-- habilitado apenas enquanto admin_plataforma estiver vazia.
-- Em dev, o seed é feito por R__seed_admin_dev.sql (flyway locations
-- incluem classpath:db/seed apenas no profile dev).

-- Usuários de demonstração (tenant Demonstração)
-- Admin Empresa
INSERT INTO cpc_usuario (id, cpc_id, cpf, nome, email_corporativo, senha_hash, role, tenant_id, acesso_estoque, ativo, criado_em)
VALUES (
    '22222222-2222-4222-8222-222222222222',
    '22222222-2222-4222-8222-222222222222',
    '11111111111',
    'Admin Empresa',
    'rh@empresa.com.br',
    '$2a$10$reLinwh8IlJzgTjS9RMj.ux/0bUiI0fhT7EnBzqa4LjptHD44B.1K',
    'ADMIN_EMPRESA',
    'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
    TRUE,
    true,
    NOW()
)
ON CONFLICT (cpf) DO UPDATE SET
    nome = EXCLUDED.nome,
    senha_hash = EXCLUDED.senha_hash,
    role = EXCLUDED.role,
    tenant_id = EXCLUDED.tenant_id,
    acesso_estoque = EXCLUDED.acesso_estoque,
    ativo = TRUE,
    tentativas_login_falhas = 0,
    bloqueio_login_ate = NULL;

-- Gestor de RH
INSERT INTO cpc_usuario (id, cpc_id, cpf, nome, email_corporativo, senha_hash, role, tenant_id, acesso_estoque, ativo, criado_em)
VALUES (
    '77777777-7777-4777-8777-777777777777',
    '77777777-7777-4777-8777-777777777777',
    '22222222222',
    'Gestor de RH',
    'gestor.rh@empresa.com.br',
    '$2a$10$reLinwh8IlJzgTjS9RMj.ux/0bUiI0fhT7EnBzqa4LjptHD44B.1K',
    'GESTOR_RH',
    'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
    TRUE,
    true,
    NOW()
)
ON CONFLICT (cpf) DO UPDATE SET
    nome = EXCLUDED.nome,
    senha_hash = EXCLUDED.senha_hash,
    role = EXCLUDED.role,
    tenant_id = EXCLUDED.tenant_id,
    acesso_estoque = EXCLUDED.acesso_estoque,
    ativo = TRUE,
    tentativas_login_falhas = 0,
    bloqueio_login_ate = NULL;

-- Colaborador 1 (apenas ponto)
INSERT INTO cpc_usuario (id, cpc_id, cpf, nome, email_corporativo, senha_hash, role, tenant_id, ativo, criado_em)
VALUES (
    '33333333-3333-4333-8333-333333333333',
    '33333333-3333-4333-8333-333333333333',
    '12345678901',
    'Colaborador 1',
    'colaborador@empresa.com.br',
    '$2a$10$z5NHoUWdOVy7WmEBc94PcOEy0ACY2P6v8mVt6KW9yeHVdSr2Tldxu',
    'COLABORADOR',
    'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
    true,
    NOW()
)
ON CONFLICT (cpf) DO UPDATE SET
    nome = EXCLUDED.nome,
    senha_hash = EXCLUDED.senha_hash,
    role = EXCLUDED.role,
    tenant_id = EXCLUDED.tenant_id,
    ativo = TRUE,
    tentativas_login_falhas = 0,
    bloqueio_login_ate = NULL;

-- Colaborador 2 (ponto + estoque)
INSERT INTO cpc_usuario (id, cpc_id, cpf, nome, email_corporativo, senha_hash, role, tenant_id, acesso_estoque, ativo, criado_em)
VALUES (
    '55555555-5555-4555-8555-555555555555',
    '55555555-5555-4555-8555-555555555555',
    '98765432100',
    'Colaborador 2',
    'almoxarife@empresa.com.br',
    '$2a$10$z5NHoUWdOVy7WmEBc94PcOEy0ACY2P6v8mVt6KW9yeHVdSr2Tldxu',
    'COLABORADOR',
    'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
    TRUE,
    true,
    NOW()
)
ON CONFLICT (cpf) DO UPDATE SET
    nome = EXCLUDED.nome,
    senha_hash = EXCLUDED.senha_hash,
    role = EXCLUDED.role,
    tenant_id = EXCLUDED.tenant_id,
    acesso_estoque = EXCLUDED.acesso_estoque,
    ativo = TRUE,
    tentativas_login_falhas = 0,
    bloqueio_login_ate = NULL;

-- Segurança: nunca manter os antigos admins seed via CPF
DELETE FROM usuario_modulo WHERE usuario_id IN (
    SELECT id FROM cpc_usuario WHERE cpf IN ('00000000000', '99999999999')
);
DELETE FROM colaborador WHERE cpc_usuario_id IN (
    SELECT id FROM cpc_usuario WHERE cpf IN ('00000000000', '99999999999')
);
DELETE FROM cpc_usuario WHERE cpf IN ('00000000000', '99999999999');

-- Jornada padrão (tenant LJ Code)
INSERT INTO configuracao_jornada (id, tenant_id, nome, carga_horaria_diaria_minutos, exige_intervalo, intervalo_minimo_minutos, tolerancia_entrada_minutos, tolerancia_saida_minutos, interjornada_minima_minutos)
VALUES (
    'a0eebc99-0019-4019-8019-6bb9bd380a09',
    'a0eebc99-0009-0009-0009-6bb9bd380a09',
    'Padrão 8h', 480, true, 60, 10, 10, 660
)
ON CONFLICT (id) DO NOTHING;

-- Registros de colaborador dos usuários demo
INSERT INTO colaborador (id, cpc_usuario_id, tenant_id, matricula, cargo, departamento, data_nascimento, data_admissao, ativo)
VALUES
    ('44444444-4444-4444-8444-444444444444',
     '33333333-3333-4333-8333-333333333333',
     'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
     'MAT-001', 'Desenvolvedor', 'Engenharia', '1990-01-01', '2024-01-01', true),
    ('66666666-6666-4666-8666-666666666666',
     '55555555-5555-4555-8555-555555555555',
     'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
     'MAT-002', 'Almoxarife', 'Almoxarifado', '1992-05-15', '2024-03-01', true),
    ('88888888-8888-4888-8888-888888888888',
     '77777777-7777-4777-8777-777777777777',
     'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
     'MAT-003', 'Gestor de RH', 'Recursos Humanos', '1985-03-15', '2023-01-01', true)
ON CONFLICT (id) DO NOTHING;

-- Configuração fiscal padrão (tenant LJ Code)
INSERT INTO configuracao_fiscal (tenant_id, prtp_nome, prtp_versao, atualizado_em)
VALUES ('a0eebc99-0009-0009-0009-6bb9bd380a09', 'CHRONOS PULSE', '1.0.0', NOW())
ON CONFLICT (tenant_id) DO NOTHING;

-- Backfill: todo usuário ativo com tenant herda os módulos ativos do tenant
INSERT INTO usuario_modulo (id, tenant_id, usuario_id, codigo)
SELECT gen_random_uuid(), u.tenant_id, u.id, m.codigo
FROM cpc_usuario u
JOIN empresa_modulo em ON em.tenant_id = u.tenant_id
JOIN modulo_plataforma m ON m.id = em.modulo_id AND m.ativo = TRUE
WHERE u.ativo = TRUE
  AND u.tenant_id IS NOT NULL
ON CONFLICT DO NOTHING;

-- Ajustes finos por perfil de demonstração
-- Colaborador 1: apenas PONTO
DELETE FROM usuario_modulo
WHERE usuario_id = '33333333-3333-4333-8333-333333333333'
  AND codigo <> 'PONTO';

-- Colaborador 2: PONTO + ESTOQUE
DELETE FROM usuario_modulo
WHERE usuario_id = '55555555-5555-4555-8555-555555555555'
  AND codigo NOT IN ('PONTO', 'ESTOQUE');
