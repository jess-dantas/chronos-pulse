# ============================================================================
# CHRONOS PULSE - Jornadas de Teste (Backend Spring Boot)
# Cenários no padrão Gherkin (Given-When-Then), derivados dos testes em
# src/test/java (executar: mvnw test)
# ============================================================================

@backend @ecossistema @plataforma @empresa
Feature: Gestão de Empresas (Tenants) e Validação de CNPJ (Backend)
  Como administrador de plataforma
  Eu quero cadastrar empresas e validar CNPJs
  Para que cada tenant seja criado corretamente e com documento válido

  @usecase
  Scenario: Cadastro de empresa com CNPJ único é bem-sucedido
    Given não existe empresa cadastrada com o CNPJ "12345678000195"
    When o CadastrarEmpresaUseCase executa o comando (cnpj, nome = "Empresa Exemplo LTDA")
    Then uma empresa é persistida
    And o CNPJ retornado é "12345678000195"
    And o nome retornado é "Empresa Exemplo LTDA"

  @usecase @regra
  Scenario: Cadastro de empresa com CNPJ duplicado é rejeitado
    Given já existe uma empresa com o CNPJ "12345678000195"
    When o CadastrarEmpresaUseCase tenta cadastrar o mesmo CNPJ
    Then uma exceção IllegalArgumentException é lançada contendo "CNPJ já cadastrado"
    And nenhuma empresa é persistida

  @validacao @cnpj
  Scenario: CNPJ numérico válido é aceito
    Given o CNPJ "12345678000195"
    When o validador é aplicado
    Then o resultado é válido
    And o CNPJ "49262262000113" também é válido

  @validacao @cnpj
  Scenario: CNPJ alfanumérico válido é aceito
    Given o CNPJ "12ABC34501DE45"
    When o validador é aplicado
    Then o resultado é válido

  @validacao @cnpj
  Scenario: CNPJ com pontuação é normalizado e aceito
    Given os CNPJs "49.262.262/0001-13" e "12.ABC.345/01DE-45"
    When o validador é aplicado
    Then ambos são válidos
    And a normalização converte "12.ABC.345/01DE-45" em "12ABC34501DE45"
    And a normalização converte letras minúsculas em maiúsculas

  @validacao @cnpj
  Scenario: CNPJ com dígito verificador inválido é rejeitado
    Given os CNPJs "12345678000188" e "12ABC34501DE44"
    When o validador é aplicado
    Then ambos são inválidos

  @validacao @cnpj
  Scenario: CNPJ com tamanho inválido é rejeitado
    Given os valores "1234567800019", "123456780001951" e ""
    When o validador é aplicado
    Then todos são inválidos

@backend @ecossistema @auth
Feature: Autenticação de Usuários (Backend)
  Como usuário do sistema
  Eu quero me autenticar com CPF e senha
  Para que eu receba tokens de acesso e segredo os módulos habilitados

  @usecase @login
  Scenario: Autenticação com credenciais válidas gera tokens e dados do usuário
    Given existe um usuário com CPF "12345678901", senha correta "senha123" e role "COLABORADOR"
    And o tenant do usuário está com o módulo "PONTO" ativo
    When o AutenticarUsuarioUseCase executa o comando (cpf, senha)
    Then o access token é gerado
    And o refresh token é gerado
    And a role retornada é "COLABORADOR"
    And o CPF retornado é "12345678901"
    And o id do colaborador (cpcId) é retornado
    And a lista de módulos ativos contém exatamente "PONTO"

  @usecase @login @regra
  Scenario: Autenticação com CPF inexistente é rejeitada
    Given não existe usuário com o CPF "12345678901"
    When o AutenticarUsuarioUseCase tenta autenticar
    Then uma exceção IllegalArgumentException é lançada com a mensagem "Credenciais inválidas"

  @usecase @login @regra
  Scenario: Autenticação com senha incorreta é rejeitada
    Given existe um usuário com CPF "12345678901" porém a senha "senhaErrada" não confere
    When o AutenticarUsuarioUseCase tenta autenticar
    Then uma exceção IllegalArgumentException é lançada com a mensagem "Credenciais inválidas"

@backend @ecossistema @colaborador
Feature: Gestão de Colaboradores (Backend)
  Como gestor de RH
  Eu quero cadastrar e listar colaboradores
  Para que a força de trabalho seja administrada por tenant

  @usecase @cadastro
  Scenario: Cadastro de colaborador com CPF único é bem-sucedido
    Given não existe usuário cadastrado com o CPF "12345678901"
    When o CadastrarColaboradorUseCase executa o comando com dados completos e tenant válido
    Then um usuário CpcUsuario é salvo com a senha codificada
    And um Colaborador é salvo com matrícula "MAT001" e cargo "Desenvolvedor"
    And o resultado retorna o id do colaborador (cpcUsuarioId) e a matrícula "MAT001"

  @usecase @cadastro @regra
  Scenario: Cadastro de colaborador com CPF duplicado é rejeitado
    Given já existe um usuário com o CPF "12345678901"
    When o CadastrarColaboradorUseCase tenta cadastrar o mesmo CPF
    Then uma exceção IllegalArgumentException é lançada contendo "CPF já cadastrado"
    And nenhum usuário é salvo
    And nenhum colaborador é salvo

  @usecase @listagem
  Scenario: Listagem de colaboradores por tenant traz detalhes completos
    Given existe um colaborador "MAT100" (Analista/Administrativo) vinculado ao tenant e à usuária "Maria Silva" com CPF "11122233344"
    And a usuária possui flag de acesso ao estoque ativo
    When o ListarColaboradoresUseCase executa para o tenant
    Then a lista retorna 1 item
    And o item contém id, nome "Maria Silva", CPF "11122233344", cargo "Analista" e departamento "Administrativo"
    And acessoEstoque é verdadeiro
    And ativo é verdadeiro

@backend @ecossistema @ponto
Feature: Registro e Sequência de Batidas de Ponto (Backend)
  Como colaborador
  Eu quero registrar batidas seguindo uma sequência lógica de jornada
  Para que o sistema atribua ENTRADA, INTERVALO, RETORNO e SAÍDA corretamente

  @usecase @sequencia
  Scenario: Primeira batida do dia é atribuída como ENTRADA
    Given não há batida anterior para o colaborador
    And o próximo NSR disponível é 1
    When o RegistrarPontoUseCase executa o registro
    Then o tipo de registro é ENTRADA
    And o NSR é 1
    And um hash de integridade SHA-256 de 64 caracteres é gerado
    And o registro é persistido

  @usecase @sequencia
  Scenario: Batida seguinte após ENTRADA é atribuída como INTERVALO
    Given a última batida do colaborador foi ENTRADA
    And o próximo NSR disponível é 2
    When o RegistrarPontoUseCase executa o registro
    Then o tipo de registro é INTERVALO
    And o NSR é 2

  @usecase @sequencia
  Scenario: Batida após SAÍDA reinicia o ciclo como ENTRADA
    Given a última batida do colaborador foi SAIDA
    And o próximo NSR disponível é 3
    When o RegistrarPontoUseCase executa o registro
    Then o tipo de registro é ENTRADA
    And o NSR é 3

  @usecase @falha
  Scenario: Falha de persistência é propagada ao chamador
    Given o repositório lança um erro "DB error" ao salvar
    When o RegistrarPontoUseCase executa o registro
    Then a exceção RuntimeException "DB error" é propagada

  @hash
  Scenario: Hash de integridade é determinístico e distinto por CPF
    Given um registro de ponto com dados fixos
    When o GeradorHashService gera o hash
    Then o hash tem 64 caracteres em formato hexadecimal
    And gerar duas vezes para o mesmo CPF produz o mesmo hash
    And gerar para CPFs diferentes produz hashes diferentes
    And a geração funciona mesmo com tipo de registro nulo

@backend @ecossistema @ponto
Feature: Espelho de Ponto, Ajuste Manual e Arquivo AFD/AEJ (Backend)
  Como colaborador e RH
  Eu quero consultar o espelho mensal, solicitar ajustes e gerar arquivo fiscal
  Para que a frequência seja transparente e auditável

  @espelho
  Scenario: Consulta de espelho por mês e ano retorna registros do período
    Given existem registros do colaborador no mês 9 de 2026
    When o ConsultarEspelhoPontoUseCase consulta (mês = 9, ano = 2026)
    Then o repositório é consultado por colaborador e período
    And a lista retorna os registros encontrados

  @espelho
  Scenario: Consulta sem mês/ano retorna todos os registros
    Given existem registros históricos do colaborador
    When o ConsultarEspelhoPontoUseCase consulta (mês = null, ano = null)
    Then o repositório é consultado por colaborador sem filtro de período
    And a lista retorna todos os registros

  @ajuste
  Scenario: Ajuste manual com justificativa é registrado com sucesso
    Given o colaborador fornece justificativa "Esquecimento de marcação"
    When o AjustarPontoManualUseCase executa o comando
    Then o registro é salvo com flag ajusteManual verdadeiro
    And a justificativa é "Esquecimento de marcação"
    And a observação é "Cheguei às 08:00 normalmente"
    And o NSR atribuído é 10
    And o hash de integridade de 64 caracteres é gerado

  @ajuste @regra
  Scenario: Ajuste manual sem justificativa é rejeitado
    Given o colaborador envia justificativa em branco ("   ")
    When o AjustarPontoManualUseCase tenta executar
    Then uma exceção IllegalArgumentException é lançada contendo "justificativa é obrigatória"

  @fiscal @aej
  Scenario: Arquivo AEJ gera cabeçalho com CNPJ e razão social
    Given um tenant com CNPJ "12345678000195" e razão social "Empresa Teste"
    When o GeradorArquivoAEJAdapter gera o conteúdo
    Then o conteúdo inicia com "1|12345678000195|Empresa Teste"

  @fiscal @aej
  Scenario: Arquivo AEJ gera linha de registro com campos e hash
    Given um registro ENTRADA com NSR 1 no horário 08:00 de 15/01/2024 e hash "abc123"
    When o GeradorArquivoAEJAdapter gera o conteúdo
    Then o conteúdo contém "2|1|12345678901|15012024|0800|ENTRADA|abc123"
    And o rodapé contém "9|TOTAL_REGISTROS=1"

  @fiscal @aej
  Scenario: Arquivo AEJ gera jornada completa de quatro batidas
    Given uma jornada com ENTRADA, INTERVALO, RETORNO e SAIDA
    When o GeradorArquivoAEJAdapter gera o conteúdo
    Then o conteúdo contém "ENTRADA", "INTERVALO", "RETORNO" e "SAIDA"
    And o rodapé contém "9|TOTAL_REGISTROS=4"

  @fiscal @aej
  Scenario: Arquivo AEJ sem registros contém apenas cabeçalho e rodapé
    Given nenhum registro de ponto existente
    When o GeradorArquivoAEJAdapter gera o conteúdo
    Then o conteúdo possui exatamente 2 linhas

@backend @ecossistema @ponto @api
Feature: Sincronização de Pontos via API (Backend)
  Como aplicativo móvel
  Eu quero enviar lotes de batidas capturadas no dispositivo
  Para que os pontos sejam persistidos no servidor

  @api @sincronizacao
  Scenario: Lote de sincronização processado com sucesso
    Given um lote contendo 1 registro válido
    When o SincronizacaoPontoController processa o lote com autenticação do colaborador
    Then a resposta HTTP é 200
    And o id do registro está em idsSucesso
    And idsFalha está vazio

  @api @sincronizacao
  Scenario: Falha em um registro do lote é isolada em idsFalha
    Given um registro cuja persistência lança erro
    When o SincronizacaoPontoController processa o lote
    Then o id do registro está em idsFalha
    And idsSucesso está vazio

  @api @sincronizacao
  Scenario: Lote misto retorna sucesso e falha separadamente
    Given um lote com 2 registros onde o segundo falha ao persistir
    When o SincronizacaoPontoController processa o lote
    Then o primeiro id está em idsSucesso
    And o segundo id está em idsFalha

  @api @repositorio
  Scenario: Repositório de ponto salva e consulta registros
    Given uma entidade RegistroPonto persistível
    When o RegistroPontoRepositoryAdapter salva e busca o registro
    Then o modelo é retornado ao salvar
    And buscarPorId retorna o modelo para um id existente
    And buscarPorId retorna vazio para um id inexistente
    And obterProximoNsr retorna o próximo contador (ex. 42)
    And buscarUltimoTipoPorColaborador retorna o último tipo (ex. ENTRADA)

@backend @ecossistema @estoque
Feature: Catálogo de Materiais e Almoxarifados (Backend)
  Como responsável de estoque
  Eu quero cadastrar materiais, grupos e almoxarifados
  Para que o catálogo esteja organizado por tenant

  @catalogo
  Scenario: Cadastro de material vinculado a grupo válido
    Given existe um grupo "Material de Escritório" no tenant
    When o MaterialService cadastra um material do grupo com código CATMAR "CAT-5001" e descrição "Grampeador 26/6"
    Then o material recebe um id
    And a descrição retornada é "Grampeador 26/6"
    And a unidade de medida é "UN"
    And o grupo do material é o grupo informado

  @catalogo
  Scenario: Cadastro de grupo de material
    Given o tenant autenticado
    When o MaterialService cadastra o grupo (código "LIM-01", nome "Material de Limpeza")
    Then o grupo recebe um id
    And o código retornado é "LIM-01"
    And o nome retornado é "Material de Limpeza"

  @catalogo
  Scenario: Cadastro de almoxarifado
    Given o tenant autenticado
    When o MaterialService cadastra o almoxarifado "Almoxarifado Saúde" (setorial)
    Then o almoxarifado recebe um id
    And o nome retornado é "Almoxarifado Saúde"

@backend @ecossistema @estoque
Feature: Movimentações de Estoque com Custo Médio Ponderado - PMP (Backend)
  Como almoxarife
  Eu quero registrar entradas e saídas de materiais calculando o custo médio (PMP MCASP)
  Para que saldos e valores financeiros estejam corretos

  @pmp
  Scenario: PMP é definido pela primeira entrada quando saldo é zero
    Given o saldo inicial é 0 unidades
    When a CalculadoraPmpService processa entrada de 100 unidades a R$ 15,5000
    Then o novo custo médio é R$ 15,5000

  @pmp
  Scenario: PMP é recalculado de forma ponderada com múltiplas aquisições a preços diferentes
    Given o saldo tem 100 unidades a R$ 10,0000
    When entra entrada de 50 unidades a R$ 16,0000
    Then o novo custo médio é R$ 12,0000
    And a quantidade passa a 150 unidades

  @pmp
  Scenario: Arredondamento do PMP usa HALF_UP com 4 casas decimais
    Given o saldo tem 10 unidades a R$ 3,3300
    When entra entrada de 7 unidades a R$ 4,5500
    Then 65,15 ÷ 17 resulta em R$ 3,8324

  @pmp @regra
  Scenario: Entrada com quantidade zero ou negativa é rejeitada
    Given uma entrada com quantidade 0 ou -5
    When a CalculadoraPmpService processa o cálculo
    Then uma exceção IllegalArgumentException é lançada

  @pmp @saida
  Scenario: Saída usa o PMP atual para o valor total sem alterar o custo unitário
    Given o saldo tem custo médio de R$ 12,5000
    When é calculada uma saída de 25 unidades
    Then o valor total da saída é R$ 312,5000

  @pmp @saida @regra
  Scenario: Saída com quantidade menor ou igual a zero é rejeitada
    Given uma saída com quantidade 0
    When a CalculadoraPmpService processa o cálculo do valor total
    Then uma exceção IllegalArgumentException é lançada

  @movimentacao @entrada
  Scenario: Entrada de material recalcula PMP do saldo pré-existente
    Given existe saldo de 100 unidades do material com custo médio R$ 10,0000 no lote "LOTE-01"
    And almoxarifado e material pertencem ao tenant
    When o EstoqueMovimentacaoService registra entrada de 50 unidades a R$ 16,0000 com NF "NF-12345"
    Then o saldo passa a 150 unidades com custo médio R$ 12,0000
    And é gravada uma movimentação tipo "ENTRADA_NFE" de 50 unidades com valor total R$ 800,0000

  @movimentacao @saida
  Scenario: Saída de material abate o saldo sem alterar o custo médio
    Given existe saldo de 150 unidades com custo médio R$ 12,0000 no lote "LOTE-01"
    When o EstoqueMovimentacaoService registra saída de 30 unidades vinculada a "REQ-1001"
    Then o saldo passa a 120 unidades
    And o custo médio permanece R$ 12,0000
    And é gravada uma movimentação tipo "SAIDA_REQUISICAO" de 30 unidades com valor total R$ 360,0000

  @movimentacao @saida @regra
  Scenario: Saída com saldo insuficiente é bloqueada
    Given existe saldo de 10 unidades no lote "LOTE-01"
    When o EstoqueMovimentacaoService tenta registrar saída de 20 unidades
    Then uma exceção IllegalArgumentException é lançada
    And nenhum saldo é gravado
    And nenhuma movimentação é gravada

@backend @ecossistema @estoque @requisicao
Feature: Requisições de Materiais (Backend)
  Como solicitante e almoxarife
  Eu quero criar, aprovar e atender requisições de materiais
  Para que o fluxo de suprimentos siga com aprovação e baixa de estoque

  @requisicao @fluxo
  Scenario: Criação de requisição inicia como PENDENTE
    Given almoxarifado e material pertencem ao tenant
    When o RequisicaoService cria uma requisição para o almoxarifado com 1 item de 50 unidades
    Then a requisição recebe um id
    And o status inicial é PENDENTE
    And o departamento é "Secretaria de Educação"
    And há exatamente 1 item com quantidade solicitada 50.000

  @requisicao @fluxo
  Scenario: Aprovação de requisição pendente
    Given existe uma requisição PENDENTE do tenant
    When o RequisicaoService aprova a requisição
    Then o status passa a APROVADA

  @requisicao @fluxo @baixa
  Scenario: Atendimento de requisição dispara baixa de estoque
    Given existe uma requisição APROVADA com 1 item de 20 unidades
    When o RequisicaoService atende a requisição pelo atendente autenticado
    Then o status passa a ATENDIDA
    And a data e o id do atendente são registrados
    And o EstoqueMovimentacaoService regista a saída dos itens do estoque

@backend @ecossistema @notificacao @email
Feature: Envio de Comprovantes de Ponto por E-mail (Backend)
  Como colaborador
  Eu quero receber o comprovante da batida por e-mail
  Para que eu tenha registro imediato da marcação

  @email
  Scenario: Comprovante é enviado quando o e-mail está habilitado e há destinatário
    Given o serviço de e-mail está habilitado
    And há um registro de ponto com hash de integridade
    When o EmailComprovantePontoService envia o comprovante para "colaborador@empresa.com.br"
    Then um MimeMessage é montado e enviado ao SMTP
    And nenhuma exceção é propagada

  @email @regra
  Scenario: Nenhum e-mail é enviado quando o serviço está desabilitado
    Given o serviço de e-mail está desabilitado
    When o EmailComprovantePontoService recebe a solicitação de envio
    Then o send do JavaMailSender nunca é chamado

  @email @regra
  Scenario: Nenhum e-mail é enviado quando o destinatário é nulo ou vazio
    Given o serviço de e-mail está habilitado
    When o destinatário informado é "" ou null
    Then o send do JavaMailSender nunca é chamado

  @email @resiliencia
  Scenario: Falha no SMTP é tratada silenciosamente sem derrubar a aplicação
    Given o SMTP lança "SMTP Timeout" ao enviar
    When o EmailComprovantePontoService tenta enviar o comprovante
    Then nenhuma exceção é propagada para o chamador

@backend @ecossistema @seguranca @cors
Feature: Segurança CORS e Bootstrap (Backend)
  Como plataforma hospedada
  Eu quero liberar o acesso cross-origin apenas para origens confiáveis
  Para que o frontend (Netlify) consuma a API sem bloqueios

  @cors
  Scenario: Preflight CORS a partir do Netlify é liberado
    Given uma requisição OPTIONS em /api/v1/auth/login com origin "https://chronos-pulse.netlify.app"
    When o preflight é processado
    Then a resposta é HTTP 200
    And o header Access-Control-Allow-Origin é "https://chronos-pulse.netlify.app"
    And o header Access-Control-Allow-Credentials é "true"

  @cors
  Scenario: Preflight CORS a partir do localhost é liberado
    Given uma requisição OPTIONS em /api/v1/auth/login com origin "http://localhost:5173"
    When o preflight é processado
    Then o header Access-Control-Allow-Origin é "http://localhost:5173"
    And o header Access-Control-Allow-Credentials é "true"

  @cors
  Scenario: Resposta com GET /auth/ping inclui cabeçalhos CORS
    Given uma requisição GET em /api/v1/auth/ping com origin do Netlify
    When a resposta é gerada
    Then o status é HTTP 200
    And o header Access-Control-Allow-Origin é "https://chronos-pulse.netlify.app"

  @bootstrap
  Scenario: Contexto da aplicação Spring inicia sem erros
    Given o perfil "test" ativo
    When o contexto da aplicação é carregado
    Then o carregamento ocorre sem falhas