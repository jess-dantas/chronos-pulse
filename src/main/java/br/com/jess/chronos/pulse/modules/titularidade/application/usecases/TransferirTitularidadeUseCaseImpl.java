package br.com.jess.chronos.pulse.modules.titularidade.application.usecases;

import br.com.jess.chronos.pulse.modules.auth.domain.model.CpcUsuario;
import br.com.jess.chronos.pulse.modules.auth.domain.model.Role;
import br.com.jess.chronos.pulse.modules.auth.domain.ports.output.CpcUsuarioRepositoryPort;
import br.com.jess.chronos.pulse.modules.modulo.domain.ports.output.ModulosPort;
import br.com.jess.chronos.pulse.modules.notificacao.service.EmailCodigoTitularidadeService;
import br.com.jess.chronos.pulse.modules.titularidade.domain.model.TitularidadeCodigo;
import br.com.jess.chronos.pulse.modules.titularidade.domain.model.TitularidadeTransferencia;
import br.com.jess.chronos.pulse.modules.titularidade.domain.ports.input.TransferirTitularidadeUseCase;
import br.com.jess.chronos.pulse.modules.titularidade.domain.ports.output.TitularidadeRepositoryPort;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class TransferirTitularidadeUseCaseImpl implements TransferirTitularidadeUseCase {

    private static final long VALIDADE_CODIGO_MINUTOS = 15;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final TitularidadeRepositoryPort titularidadeRepository;
    private final CpcUsuarioRepositoryPort usuarioRepository;
    private final ModulosPort modulosPort;
    private final PasswordEncoder passwordEncoder;
    private final EmailCodigoTitularidadeService emailService;

    public TransferirTitularidadeUseCaseImpl(TitularidadeRepositoryPort titularidadeRepository,
                                             CpcUsuarioRepositoryPort usuarioRepository,
                                             ModulosPort modulosPort,
                                             PasswordEncoder passwordEncoder,
                                             EmailCodigoTitularidadeService emailService) {
        this.titularidadeRepository = titularidadeRepository;
        this.usuarioRepository = usuarioRepository;
        this.modulosPort = modulosPort;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    @Override
    public Iniciado iniciar(IniciarComando comando) {
        if (comando.tenantId() == null || comando.solicitanteId() == null) {
            throw new IllegalArgumentException("Sessão inválida para transferência de titularidade.");
        }
        if (comando.novoTitularId() == null) {
            throw new IllegalArgumentException("Novo titular é obrigatório.");
        }
        if (comando.novoTitularId().equals(comando.solicitanteId())) {
            throw new IllegalArgumentException("O novo titular deve ser diferente do titular atual.");
        }

        CpcUsuario solicitante = buscarUsuarioValido(
                comando.solicitanteId(), comando.tenantId(), "Titular atual");
        if (solicitante.getRole() != Role.ADMIN_EMPRESA) {
            throw new IllegalArgumentException("Apenas o titular da empresa pode transferir a titularidade.");
        }

        CpcUsuario novoTitular = buscarUsuarioValido(
                comando.novoTitularId(), comando.tenantId(), "Novo titular");

        titularidadeRepository.cancelarAbertas(comando.tenantId(), comando.solicitanteId());

        TitularidadeTransferencia transferencia = new TitularidadeTransferencia();
        transferencia.setTenantId(comando.tenantId());
        transferencia.setSolicitanteId(comando.solicitanteId());
        transferencia.setNovoTitularId(comando.novoTitularId());
        transferencia = titularidadeRepository.salvar(transferencia);

        return new Iniciado(
                transferencia.getId(),
                novoTitular.getNome(),
                mascararCelular(novoTitular.getCelular()));
    }

    @Override
    public void confirmarBiometria(BiometriaComando comando) {
        TitularidadeTransferencia transferencia = carregarAberta(comando);
        if (!comando.confirmado()) {
            throw new IllegalArgumentException("Confirmação de biometria é obrigatória.");
        }
        transferencia.marcarBiometria();
        titularidadeRepository.salvar(transferencia);
    }

    @Override
    public CodigoEnviado enviarCodigo(EnviarCodigoComando comando) {
        TitularidadeTransferencia transferencia = carregarAberta(comando);

        CpcUsuario destinatario;
        String etapa = normalizarEtapa(comando.etapa());
        if (ETAPA_CELULAR.equals(etapa)) {
            // Etapa 2: OTP vai para o e-mail do titular ATUAL + confirmação do
            // celular do novo titular (feita na verificação).
            if (!transferencia.isEtapaBiometria()) {
                throw new IllegalArgumentException("Confirme a biometria antes do código do titular atual.");
            }
            destinatario = buscarUsuarioValido(
                    transferencia.getSolicitanteId(), comando.tenantId(), "Titular atual");
        } else {
            // Etapa 3: OTP vai para o e-mail corporativo do NOVO titular.
            if (!transferencia.isEtapaBiometria() || !transferencia.isEtapaCelular()) {
                throw new IllegalArgumentException("Conclua a biometria e a confirmação do celular antes.");
            }
            destinatario = buscarUsuarioValido(
                    transferencia.getNovoTitularId(), comando.tenantId(), "Novo titular");
        }

        String email = destinatario.getEmailCorporativo() != null
                && !destinatario.getEmailCorporativo().isBlank()
                ? destinatario.getEmailCorporativo()
                : destinatario.getEmailPessoal();
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Destinatário sem e-mail cadastrado.");
        }

        String codigo = String.format(Locale.ROOT, "%06d", SECURE_RANDOM.nextInt(1_000_000));

        // Reenvio substitui o código anterior da mesma etapa.
        titularidadeRepository.removerCodigos(transferencia.getId(), etapa);

        TitularidadeCodigo entidade = new TitularidadeCodigo();
        entidade.setTransferenciaId(transferencia.getId());
        entidade.setEtapa(etapa);
        entidade.setCodigoHash(passwordEncoder.encode(codigo));
        entidade.setExpiraEm(Instant.now().plus(Duration.ofMinutes(VALIDADE_CODIGO_MINUTOS)));
        titularidadeRepository.salvarCodigo(entidade);

        emailService.enviarCodigoAsync(email, codigo);

        return new CodigoEnviado(mascararEmail(email));
    }

    @Override
    public void verificarCodigo(VerificarCodigoComando comando) {
        TitularidadeTransferencia transferencia = carregarAberta(comando);
        String etapa = normalizarEtapa(comando.etapa());

        if (ETAPA_CELULAR.equals(etapa)) {
            if (!Boolean.TRUE.equals(comando.celularConfirmado())) {
                throw new IllegalArgumentException(
                        "Confirme o celular do novo titular para continuar.");
            }
        } else if (comando.codigo() == null || comando.codigo().isBlank()) {
            throw new IllegalArgumentException("Código de verificação é obrigatório.");
        }

        TitularidadeCodigo codigo = titularidadeRepository
                .listarCodigos(transferencia.getId(), etapa)
                .stream()
                .filter(c -> !c.isUsado() && !c.isExpirado())
                .filter(c -> passwordEncoder.matches(comando.codigo().trim(), c.getCodigoHash()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Código inválido ou expirado."));

        codigo.marcarComoUsado();
        titularidadeRepository.salvarCodigo(codigo);

        if (ETAPA_CELULAR.equals(etapa)) {
            transferencia.marcarCelular();
        } else {
            transferencia.marcarEmail();
        }
        titularidadeRepository.salvar(transferencia);
    }

    @Override
    public void concluir(Comando comando) {
        TitularidadeTransferencia transferencia = carregarAberta(comando);

        if (!transferencia.isEtapaBiometria() || !transferencia.isEtapaCelular()
                || !transferencia.isEtapaEmail()) {
            throw new IllegalArgumentException(
                    "Conclua todas as etapas (biometria, celular e e-mail) antes de finalizar.");
        }

        CpcUsuario antigoTitular = buscarUsuarioValido(
                transferencia.getSolicitanteId(), comando.tenantId(), "Titular atual");
        CpcUsuario novoTitular = buscarUsuarioValido(
                transferencia.getNovoTitularId(), comando.tenantId(), "Novo titular");

        // Novo titular: ADMIN_EMPRESA + todos os módulos contratados do tenant.
        CpcUsuario novoAdmin = novoTitular.comRole(Role.ADMIN_EMPRESA, true, true, true, true);
        usuarioRepository.atualizar(novoAdmin);
        List<String> contratados = modulosPort.listarCodigosAtivos(comando.tenantId());
        modulosPort.definirModulosDoUsuario(novoTitular.getId(), comando.tenantId(), contratados);

        // Titular antigo: COLABORADOR, sem flags de acesso, baseline PONTO.
        CpcUsuario antigoColaborador =
                antigoTitular.comRole(Role.COLABORADOR, false, false, false, false);
        usuarioRepository.atualizar(antigoColaborador);
        modulosPort.definirModulosDoUsuario(
                antigoTitular.getId(), comando.tenantId(), List.of("PONTO"));

        transferencia.concluir();
        titularidadeRepository.salvar(transferencia);
    }

    @Override
    public void cancelar(Comando comando) {
        TitularidadeTransferencia transferencia = carregarAberta(comando);
        transferencia.cancelar();
        titularidadeRepository.salvar(transferencia);
    }

    private TitularidadeTransferencia carregarAberta(BiometriaComando comando) {
        return carregar(comando.transferenciaId(), comando.solicitanteId(), comando.tenantId());
    }

    private TitularidadeTransferencia carregarAberta(EnviarCodigoComando comando) {
        return carregar(comando.transferenciaId(), comando.solicitanteId(), comando.tenantId());
    }

    private TitularidadeTransferencia carregarAberta(VerificarCodigoComando comando) {
        return carregar(comando.transferenciaId(), comando.solicitanteId(), comando.tenantId());
    }

    private TitularidadeTransferencia carregarAberta(Comando comando) {
        return carregar(comando.transferenciaId(), comando.solicitanteId(), comando.tenantId());
    }

    private TitularidadeTransferencia carregar(UUID transferenciaId, UUID solicitanteId,
                                               UUID tenantId) {
        if (transferenciaId == null || solicitanteId == null || tenantId == null) {
            throw new IllegalArgumentException("Sessão inválida para transferência de titularidade.");
        }
        TitularidadeTransferencia transferencia = titularidadeRepository
                .buscarPorId(transferenciaId)
                .orElseThrow(() -> new IllegalArgumentException("Transferência não encontrada."));
        if (!tenantId.equals(transferencia.getTenantId())
                || !solicitanteId.equals(transferencia.getSolicitanteId())) {
            throw new IllegalArgumentException("Transferência não encontrada.");
        }
        if (!TitularidadeTransferencia.STATUS_EM_ANDAMENTO.equals(transferencia.getStatus())) {
            throw new IllegalArgumentException("Transferência não está em andamento.");
        }
        if (transferencia.isExpirada()) {
            transferencia.cancelar();
            titularidadeRepository.salvar(transferencia);
            throw new IllegalArgumentException("Transferência expirada. Inicie uma nova transferência.");
        }
        return transferencia;
    }

    private CpcUsuario buscarUsuarioValido(UUID id, UUID tenantId, String rotulo) {
        CpcUsuario usuario = usuarioRepository.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException(rotulo + " não encontrado."));
        if (!tenantId.equals(usuario.getTenantId())) {
            throw new IllegalArgumentException(rotulo + " não encontrado.");
        }
        if (!usuario.isAtivo()) {
            throw new IllegalArgumentException(rotulo + " está inativo.");
        }
        return usuario;
    }

    private String normalizarEtapa(String etapa) {
        if (ETAPA_CELULAR.equals(etapa) || ETAPA_EMAIL.equals(etapa)) {
            return etapa;
        }
        throw new IllegalArgumentException("Etapa inválida.");
    }

    static String mascararCelular(String celular) {
        if (celular == null || celular.isBlank()) {
            return null;
        }
        String digitos = celular.replaceAll("\\D", "");
        if (digitos.length() < 4) {
            return "****";
        }
        return "****-" + digitos.substring(digitos.length() - 4);
    }

    static String mascararEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "e-mail mascarado";
        }
        String[] partes = email.split("@", 2);
        String local = partes[0];
        String dominio = partes[1];
        String inicio = local.substring(0, Math.min(1, local.length()));
        return inicio + "***@" + dominio;
    }
}
