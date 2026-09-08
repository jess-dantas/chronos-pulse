package br.com.jess.chronos.pulse.modules.privacidade.service;

import br.com.jess.chronos.pulse.modules.auditoria.service.AuditoriaService;
import br.com.jess.chronos.pulse.modules.auth.domain.model.CpcUsuario;
import br.com.jess.chronos.pulse.modules.auth.domain.ports.output.CpcUsuarioRepositoryPort;
import br.com.jess.chronos.pulse.modules.colaborador.domain.ports.output.ColaboradorRepositoryPort;
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

    public Map<String, Object> politicaAtual() {
        Map<String, Object> politica = new LinkedHashMap<>();
        politica.put("versao", VERSAO_POLITICA_ATUAL);
        politica.put("dataPublicacao", "2026-09-08");
        politica.put("texto", TEXTO_POLITICA);
        return politica;
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
                                       boolean aceito, String ipOrigem) {
        if (!aceito) {
            throw new IllegalArgumentException(
                    "O consentimento deve ser explícito e afirmativo (aceito=true).");
        }
        if (!VERSAO_POLITICA_ATUAL.equals(versaoPolitica)) {
            throw new IllegalArgumentException(
                    "Versão da política de privacidade inválida: " + versaoPolitica);
        }

        consentimentoRepository.save(ConsentimentoPrivacidade.builder()
                .id(UUID.randomUUID())
                .cpcId(usuario.getCpcId())
                .versaoPolitica(versaoPolitica)
                .dataConsentimento(OffsetDateTime.now())
                .aceito(true)
                .ipOrigem(ipOrigem)
                .build());

        auditoriaService.registrar("CONSENTIMENTO_PRIVACIDADE", "cpc_usuario", usuario.getId(),
                "Consentimento registrado para a política de privacidade versão " + versaoPolitica + ".",
                usuario.getTenantId(), usuario.getCpcId(), usuario.getCpf(),
                usuario.getRole().name(), null, null, ipOrigem);
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

    private static final String TEXTO_POLITICA = """
            POLÍTICA DE PRIVACIDADE — Chronos Pulse

            Controladora: Chronos Pulse Sistemas de Gestão Pública.
            Esta política descreve o tratamento de dados pessoais na plataforma, em conformidade com a Lei 13.709/2018 (LGPD).

            1. Dados coletados: nome, CPF, e-mail, telefone, foto, matrícula, cargo, departamento, data de nascimento, dados de admissão, dados de ponto (GPS e fotografia), localizacão e consentimentos.

            2. Finalidades: registro de ponto eletrônico, gestão de RH/escala, controle patrimonial, de estoque, de frota e de protocolo, segurança da informação e cumprimento de obrigações legais (ex.: geração de AEJ e espelho de ponto).

            3. Bases legais: cumprimento de obrigação legal/regulatória, execução de contrato administrativo e interesse público, além do consentimento para dados não exigidos por norma.

            4. Compartilhamento: dados serão compartilhados somente com autoridades, órgãos de controle e prestadores de serviço essenciais (ex.: armazenamento em nuvem), observada a necessidade de contrato de tratamento.

            5. Retenção: mantemos os dados pelo prazo legal (ex.: registro de ponto por 5 anos) e a trilha de auditoria é imutável por exigência de transparência pública. Após o prazo, os dados são anonimizados ou destruídos.

            6. Direitos do titular: acesso, correção, portabilidade/exportação, informação sobre compartilhamento, revogação do consentimento e eliminação de dados pessoais (via anonimização), exercíveis na tela Privacidade & LGPD do aplicativo e/ou pelo e-mail do Encarregado (DPO).

            7. Encarregado (DPO): atendimento por privacidade@chronos-pulse.com.br.

            8. Segurança: senhas armazenadas com hash (BCrypt), tokens em armazenamento seguro do dispositivo (Keystore/Keychain) e transporte criptografado (TLS).

            Ao utilizar a plataforma, o titular manifesta consentimento de forma livre e inequívoca para as finalidades acima; o consentimento pode ser revogado a qualquer momento.
            """;
}