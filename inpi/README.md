# Kit de depósito no INPI (programa de computador)

Artefatos para o pedido de **Registro de Programa de Computador (RPC)** no
Sistema **e-Software** do INPI, conforme a **Lei 9.609/98** (software) e o
art. 91 da Portaria MTP 671/2021 (pré-requisito do REP-P).

> O INPI **não aceita instalador**. O depósito é feito com o **código-fonte OU
> o resumo digital (hash)** + a **Declaração de Veracidade (DV)** assinada
> digitalmente com certificado **ICP-Brasil** + a **GRU** paga.

## O que há aqui

| Arquivo | Finalidade |
|---|---|
| `gerar_resumo_hash.ps1` | Gera o manifest SHA-256 do código-fonte (Windows/pwsh) |
| `gerar_resumo_hash.sh`   | Idem para Linux/CI (mesmo algoritmo, resultados iguais) |
| `declaracao_de_veracidade.md` | Modelo da DV para preencher e assinar com o e-CNPJ |
| `resumo_hash.txt` (gerado) | Manifest de hashes + hash final para o e-Software |

## Passo a passo (resumo)

1. **Certificado ICP-Brasil qualificado** (o e-CNPJ do CNPJ desenvolvedor).
   Certificados como gov.br `AC OAB` **não são aceitos**.
2. **GRU** — serviço **"Pedido de Registro de Programa de Computador – RPC"**,
   código **730** (R$ 210,00). Gere e pague em gov.br/inpi → Programas de
   Computador → e-Software, **antes** de enviar o pedido.
3. **Resumo digital** — rode `./gerar_resumo_hash.ps1` na raiz do repositório e
   use o **hash final** + o `resumo_hash.txt` como comprovação.
4. **Declaração de Veracidade** — preencha o modelo e **assine digitalmente**
   (A1/A3) com o e-CNPJ; salve como PDF.
5. **e-Software** — cadastro em `certificados.inpi.gov.br` com o certificado;
   novo pedido: *Programa de Computador*, título (ex.: `Chronos Pulse`),
   **Campo de Aplicação** `AD` – `AD02-Função Adm` (e/ou `AD01-Administr`),
   **Tipo de Programa** `AP03 – Controle`; anexe código-fonte **ou** resumo
   digital e a DV assinada.
6. Acompanhe a publicação na **RPI** para obter o nº do registro e gravá-lo em
   `PUT /api/v1/fiscal/configuracao`.

## Algoritmo do resumo digital

Determinístico e reproduzível entre `ps1`/`sh`:

1. Coleta todos os arquivos dos diretórios passados (padrão: backend `chronos-pulse`
   `src/main` + `src/test` e app `chronos_pulse_app/lib` + `test`), **excluindo**
   artefatos de build/VCS (`.git`, `target`, `build`, `.dart_tool`, etc.).
2. Para cada arquivo (ordem alfabética do caminho relativo): linha
   `<caminho relativo> TAB <SHA-256 do conteúdo em UTF-8>`.
3. `hash_manifesto` = SHA-256 do conteúdo concatenado das linhas (UTF-8, separadas por `\n`).
4. `hash_concatenacao` = SHA-256 do conteúdo bruto (UTF-8) de todos os arquivos
   concatenados na mesma ordem (alternativa exigida por alguns validadores).

Os dois hashes SÃO impressos e gravados em `resumo_hash.txt`.

## Uso

```powershell
# Windows
.\inpi\gerar_resumo_hash.ps1
Set-Location C:\app\chronos-pulse   # se sair da raiz do repositório
& .\inpi\gerar_resumo_hash.ps1 -Backend C:\app\chronos-pulse -App C:\app\chronos_pulse_app
```

```bash
# Linux/CI
./inpi/gerar_resumo_hash.sh
```

Saída: `inpi/resumo_hash.txt` + hashes no console.