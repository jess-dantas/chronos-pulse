package br.com.jess.chronos.pulse.modules.privacidade.service;

import br.com.jess.chronos.pulse.modules.auditoria.service.AuditoriaService;
import br.com.jess.chronos.pulse.modules.auth.domain.model.CpcUsuario;
import br.com.jess.chronos.pulse.modules.auth.domain.model.Role;
import br.com.jess.chronos.pulse.modules.auth.domain.ports.output.CpcUsuarioRepositoryPort;
import br.com.jess.chronos.pulse.modules.colaborador.domain.ports.output.ColaboradorRepositoryPort;
import br.com.jess.chronos.pulse.modules.modulo.domain.ports.output.ModulosPort;
import br.com.jess.chronos.pulse.modules.privacidade.domain.ConsentimentoPrivacidade;
import br.com.jess.chronos.pulse.modules.privacidade.repository.ConsentimentoPrivacidadeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PrivacidadeService {

    public static final String VERSAO_POLITICA_ATUAL = "1.0";

    private final CpcUsuarioRepositoryPort usuarioRepository;
    private final ColaboradorRepositoryPort colaboradorRepository;
    private final ConsentimentoPrivacidadeRepository consentimentoRepository;
    private final AuditoriaService auditoriaService;
    private final ModulosPort modulosPort;

    public Map<String, Object> politicaAtual() {
        Map<String, Object> politica = new LinkedHashMap<>();
        politica.put("versao", VERSAO_POLITICA_ATUAL);
        politica.put("dataPublicacao", "2026-09-08");
        politica.put("texto", TEXTO_POLITICA);
        politica.put("hashTermo", sha256Hex(TEXTO_POLITICA));
        return politica;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> statusConsentimento(CpcUsuario usuario) {
        var ultimo = consentimentoRepository
                .findTopByCpcIdOrderByDataConsentimentoDesc(usuario.getCpcId())
                .filter(ConsentimentoPrivacidade::isAceito)
                .orElse(null);

        String versaoAceita = ultimo != null ? ultimo.getVersaoPolitica() : null;
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("versaoAtual", VERSAO_POLITICA_ATUAL);
        status.put("versaoAceita", versaoAceita);
        status.put("dataConsentimento",
                ultimo != null ? ultimo.getDataConsentimento().toString() : null);
        status.put("aceitePendente", !VERSAO_POLITICA_ATUAL.equals(versaoAceita));
        return status;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> exportarMeusDados(CpcUsuario usuario) {
        Map<String, Object> dados = new LinkedHashMap<>();
        dados.put("geradoEm", OffsetDateTime.now().toString());

        Map<String, Object> usuarioData = new LinkedHashMap<>();
        usuarioData.put("cpcId", usuario.getCpcId());
        usuarioData.put("cpf", mascararCpf(usuario.getCpf()));
        usuarioData.put("nome", usuario.getNome());
        usuarioData.put("emailCorporativo", usuario.getEmailCorporativo());
        usuarioData.put("emailPessoal", usuario.getEmailPessoal());
        usuarioData.put("apelido", usuario.getApelido());
        usuarioData.put("celular", usuario.getCelular());
        usuarioData.put("fotoUrl", usuario.getFoto());
        usuarioData.put("papel", usuario.getRole().name());
        usuarioData.put("tenantId", usuario.getTenantId());
        usuarioData.put("ativo", usuario.isAtivo());
        usuarioData.put("criadoEm", usuario.getCriadoEm());
        dados.put("usuario", usuarioData);

        colaboradorRepository.buscarPorCpcUsuarioId(usuario.getCpcId()).ifPresent(colaborador -> {
            Map<String, Object> colaboradorData = new LinkedHashMap<>();
            colaboradorData.put("matricula", colaborador.getMatricula());
            colaboradorData.put("cargo", colaborador.getCargo());
            colaboradorData.put("departamento", colaborador.getDepartamento());
            colaboradorData.put("dataNascimento", colaborador.getDataNascimento());
            colaboradorData.put("dataAdmissao", colaborador.getDataAdmissao());
            colaboradorData.put("dataDesligamento", colaborador.getDataDesligamento());
            dados.put("colaborador", colaboradorData);
        });

        dados.put("consentimentos", consentimentoRepository
                .findByCpcIdOrderByDataConsentimentoDesc(usuario.getCpcId())
                .stream()
                .map(c -> {
                    Map<String, Object> cMap = new LinkedHashMap<>();
                    cMap.put("versaoPolitica", c.getVersaoPolitica());
                    cMap.put("dataConsentimento", c.getDataConsentimento());
                    cMap.put("aceito", c.isAceito());
                    return cMap;
                })
                .toList());

        return dados;
    }

    @Transactional
    public void registrarConsentimento(CpcUsuario usuario, String versaoPolitica,
                                       boolean aceito, String ipOrigem, String userAgent) {
        if (!aceito) {
            throw new IllegalArgumentException(
                    "O consentimento deve ser explícito e afirmativo (aceito=true).");
        }
        if (!VERSAO_POLITICA_ATUAL.equals(versaoPolitica)) {
            throw new IllegalArgumentException(
                    "Versão da política de privacidade inválida: " + versaoPolitica);
        }

        // Idempotente: reenvio (modal reaberto, corrida na restauração de
        // sessão) não duplica registro nem regrava auditoria.
        if (consentimentoRepository.existsByCpcIdAndVersaoPoliticaAndAceitoTrue(
                usuario.getCpcId(), versaoPolitica)) {
            return;
        }

        consentimentoRepository.save(ConsentimentoPrivacidade.builder()
                .id(UUID.randomUUID())
                .cpcId(usuario.getCpcId())
                .tenantId(usuario.getTenantId())
                .versaoPolitica(versaoPolitica)
                .dataConsentimento(OffsetDateTime.now())
                .aceito(true)
                .ipOrigem(ipOrigem)
                .userAgent(truncar(userAgent, 512))
                .hashTermo(sha256Hex(TEXTO_POLITICA))
                .build());

        auditoriaService.registrar("CONSENTIMENTO_PRIVACIDADE", "cpc_usuario", usuario.getId(),
                "Consentimento registrado para a política de privacidade versão " + versaoPolitica + ".",
                usuario.getTenantId(), usuario.getCpcId(), usuario.getCpf(),
                usuario.getRole().name(), null, null, ipOrigem);

        // Admin Empresa: na primeira aceitação, associa todos os módulos
        // contratados pela empresa ao seu usuário (raiz da hierarquia de acesso).
        if (usuario.getRole() == Role.ADMIN_EMPRESA && usuario.getTenantId() != null) {
            var contratados = modulosPort.listarCodigosAtivos(usuario.getTenantId());
            if (!contratados.isEmpty()) {
                modulosPort.definirModulosDoUsuario(usuario.getId(), usuario.getTenantId(), contratados);
            }
        }
    }

    @Transactional
    public void anonimizarMeusDados(CpcUsuario usuario, String ipOrigem) {
        CpcUsuario anonimizado = usuario.anonimizar();
        usuarioRepository.atualizar(anonimizado);

        auditoriaService.registrar("EXCLUSAO_DADOS_PESSOAIS", "cpc_usuario", usuario.getId(),
                "Dados pessoais anonimizados (LGPD art. 18, VI): nome/e-mails/celular/foto removidos "
                        + "e acessos desativados; registros operacionais e trilha de auditoria retidos por exigência legal.",
                usuario.getTenantId(), usuario.getCpcId(), usuario.getCpf(),
                usuario.getRole().name(), null, null, ipOrigem);
    }

    private String mascararCpf(String cpf) {
        if (cpf == null || cpf.length() != 11) {
            return cpf;
        }
        return cpf.substring(0, 3) + ".***.***-" + cpf.substring(9);
    }

    private static String truncar(String valor, int tamanhoMaximo) {
        if (valor == null) {
            return null;
        }
        return valor.length() <= tamanhoMaximo ? valor : valor.substring(0, tamanhoMaximo);
    }

    private static String sha256Hex(String texto) {
        try {
            java.security.MessageDigest digest =
                    java.security.MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(texto.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponível", e);
        }
    }

    /**
     * Termo de Ciência e Transparência de Tratamento de Dados Pessoais
     * (Colaborador) — versão 1.0. A base legal do ponto é obrigação legal
     * (CLT / Portaria MTP 671/2021); o aceite aqui é ciência, não consentimento
     * revogável para a marcação de jornada.
     */
    static final String TEXTO_POLITICA = """
            TERMO DE CIÊNCIA E TRANSPARÊNCIA DE TRATAMENTO DE DADOS PESSOAIS (COLABORADOR)
            Plataforma: Chronos Pulse Suite
            Versão: 1.0

            1. QUAIS DADOS SÃO COLETADOS?
            Para o correto registro da sua jornada de trabalho e gestão de atividades, a plataforma coleta:
            - Dados de identificação: Nome completo, CPF, matrícula, e-mail e cargo.
            - Dados de registro de ponto: Horários de entrada/saída, endereço IP, coordenadas de geolocalização (GPS) no momento da marcação e fotografia facial (se habilitado pela sua empresa para validação antifraude).

            2. PARA QUAL FINALIDADE OS DADOS SÃO USADOS?
            Seus dados são tratados estritamente para:
            - Cumprimento de obrigações legais de controle de jornada (CLT e Portaria MTP 671/2021).
            - Emissão de comprovantes de registro de ponto, Espelho de Ponto e Arquivo Eletrônico de Jornada (AEJ).
            - Garantia da segurança da informação e auditoria contra fraudes no registro.

            3. TEMPO DE GUARDA DOS DADOS (RETENÇÃO)
            Os dados referentes ao registro de ponto serão mantidos armazenados pelo prazo mínimo obrigatório exigido pela legislação trabalhista e fiscal (mínimo de 5 anos).

            4. COMPARTILHAMENTO DE DADOS
            Seus dados não serão comercializados. O compartilhamento ocorre exclusivamente com:
            - O seu Empregador (Controlador dos dados).
            - Órgãos fiscalizadores do trabalho e Justiça do Trabalho, quando formalmente solicitado.
            - Provedores de infraestrutura e nuvem essenciais para a operação do sistema.

            5. SEUS DIREITOS (ART. 18 DA LGPD)
            Você possui o direito de consultar seus dados armazenados, emitir comprovantes da sua jornada e solicitar correções ao setor de RH do seu empregador. Você pode exercer seus direitos ou tirar dúvidas através da aba "Privacidade" do aplicativo ou pelo e-mail do Encarregado (DPO): dpo@chronos-pulse.com.br.
            """;
}