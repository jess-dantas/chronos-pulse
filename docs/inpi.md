# Roteiro INPI — Registro de Programa de Computador e Marca

> Guia prático para a **Chronos Pulse** registrar o software e proteger a marca,
> com os números exigidos pelo **REP-P** (Portaria MTP n. 671/2021, art. 91).
> Escopo: pedidos 100% **eletrônicos** no site do INPI (gratuitos para pessoas
> físicas; taxas GRU via boleto/Pix para pessoas jurídicas).

## 1. Por que isso é obrigatório para o REP-P?

O **REP-P** é o registrador eletrônico de ponto desenvolvido por (e registrado em
nome de) **programa** — a própria Portaria 671/2021 exige:

- **Art. 4º-A/art. 91**: o sistema de registro eletrônico de ponto (REP-P) deve
  ter seu **programa de computador registrado no INPI**, sob inscrição do
  fabricante ou desenvolvedor;
- **Anexo V (AFD, cabeçalho, posições 190–206)**: campo *"número de registro no
  INPI"* — é o valor que o backend preenche em `GeradorArquivoAFDAdapter`
  (`numeroRegistroInpi`), no leiaute oficial;
- **Anexo VI (AEJ)**: o arquivo também referencia os REPs/desenvolvedor e deve
  ser assinado com **certificado ICP-Brasil** (art. 86/88) — ver etapa 4.

A marca (Classe 9 para software SaaS) não é exigência legal da portaria, mas
protege o uso comercial do nome "Chronos Pulse" e impede terceiros de usar a
mesma denominação.

## 2. Registro do Programa de Computador (software)

Programa de computador é **registrado** (não patenteado); a Lei n. 9.609/1998 dá
a proteção pela criação, mas o **registro no INPI** gera a data certa e é o
documento que a fiscalização do trabalho pede.

**Passo a passo (portal: [servico.inpi.gov.br](https://servico.inpi.gov.br))**

1. Cadastre a empresa (gov.br) e o responsável no portal do INPI.
2. Acesse *Serviços → Programa de Computador → Registrar programa de computador*.
3. Escolha a modalidade de depósito (**eletrônico**, gratuito quando o titular é
   pessoa física; para PJ há GRU).
4. Anexe:
   - o(s) arquivo(s) do código-fonte (*algumas centenas de linhas; não é o projeto inteiro*);
   - declaração de autoria/cessão (modelo do próprio INPI).
5. Emita a **GRU** (código de custas de programa de computador), pague por
   boleto/Pix e conclua a conferência.
6. Acompanhe `[INPI]` → o registro demora em média **9–12 meses**; o número
   (formato `BR 51 xxxx-xxxx`) sai na RPI (Revista da Propriedade Industrial).

**Documentos-chave**

| Item | Onde aplicar |
|---|---|
| Nº de registro no INPI (`BR 51 ...`) | Campo *número de registro no INPI* do **AFD** (Anexo V) |
| Folha de rosto do software | Kit de conformidade entregue ao cliente junto com o Atestado Técnico (art. 89) |

## 3. Registro da Marca ("Chronos Pulse")

- **Classe indicada:** **Classe 9** — softwares, programas de computador, SaaS
  (embora o SaaS também dialogue com a Classe 42 para *serviços de TI*, a classe
  de **produto de software** é a 9; empresas SaaS geralmente registram em 9 e/ou 42).
- **Especificação sugerida (Classe 9):** "Programas de computador; aplicativos
  móveis baixáveis para controle de ponto eletrônico; software de gestão de
  empresas e de contratações públicas; plataforma SaaS (software como serviço)".
- **Registro:** via **e-Marca** (servico.inpi.gov.br → Marca → e-Marca), com busca
  de anterioridade prévia na mesma classe; taxa de depósito PJ ~R$ 355 (+ sinal
  e porte) e ~R$ 142,60 para ME/MEI/empresário individual.
- **Prazo:** registro em média **12–18 meses**, com uso comprovado se houver
  oposição. Usar o nome com a marca registrada desde o início (® apenas após
  concedido).

## 4. Assinatura ICP-Brasil (integração com o REP-P)

O AFD e o AEJ precisam de **assinatura eletrônica qualificada** (art. 86/88):
certificado digital **ICP-Brasil** (.pfx) da **Chronos Pulse** (e-CNPJ ICP-Brasil
é o usual).

**Estado atual (implementado no backend):**
- Leiaute: AFD e AEJ terminam com o campo **`assinDigital`** (100 A) preenchido com
  o literal `ASSINATURA_DIGITAL_EM_ARQUIVO_P7S` (espaços à direita) — última linha
  do arquivo, conforme FAQ oficial do MTE (perguntas 28/29).
- AEJ (Anexo VI): registro **`08`** (Identificação do PTRP — nome, versão, tipo de
  identificador, CNPJ/CPF, razão social/nome e e-mail do desenvolvedor) emitido e
  **contado no trailer `99`**.
- Assinatura **CAdES (CMS) destacada**: `AssinadorCadesAdapter` gera o arquivo
  **`.p7s`** (SHA-256withRSA / ECDSA sobre o byte-a-byte do `.txt`), usando o PFX
  fornecido por **segredos de ambiente** (não versionados):
  - `FISCAL_PFX_BASE64` (PFX em Base64) e `FISCAL_PFX_SENHA`;
  - espelhados em `chronos.fiscal.assinatura.*` (`application.yml`).
- Endpoints novos: `GET /fiscal/afd/assinatura` e `GET /fiscal/aej/assinatura`
  (retornam `application/pkcs7-signature`, nome `*.txt.p7s`); retornam `503` quando
  o certificado não está configurado.
- O `.p7s` deve ser mantido junto ao `.txt` (ambos são necessários para a fiscalização).

#### 5.1. Estado da implementação (nº INPI)

O campo está **pronto para receber o número** assim que o registro sair na RPI:

- **AFD** (Anexo V, pos. 190–206 do cabeçalho): preenchido em `GeradorArquivoAFDAdapter`
  via parâmetro `numeroRegistroInpi` do endpoint `/fiscal/afd/download`;
- **AEJ** (Anexo VI): `numeroRegistroInpi` no endpoint `/fiscal/aej/download` emite o
  registro `02` (REP-P) e é referenciado nas marcações (`idRepAej`).

## 5. Checklist de execução

- [ ] Abertura do pedido de **programa de computador** no INPI (portaria art. 91)
- [ ] Nº INPI testado no **AFD** (campo 190–206 do cabeçalho) e no **AEJ** (registro `02`)
      via endpoint; futuramente automático via **settings por tenant**
- [ ] Pedido de **marca** (Classe 9) no e-Marca
- [ ] Certificado **e-CNPJ ICP-Brasil** adquirido e testado (CAdES `.p7s` já
      implementado: `AssinadorCadesAdapter` + `/fiscal/{afd,aej}/assinatura`,
      via `FISCAL_PFX_BASE64`/`FISCAL_PFX_SENHA`)
- [ ] Homologação dos arquivos com o **leiaute oficial** (gov.br) e o
      [validador AEJ do MPT/MTE](https://peticionamento.prt7.mpt.mp.br/arquivos/aej_leiaute.pdf)
- [ ] Atualizar `ROADMAP.md` (R32) quando o nº de registro estiver disponível