package br.com.jess.chronos.pulse.modules.auth.application.usecases;

import br.com.jess.chronos.pulse.modules.auth.domain.model.RecuperacaoSenha;
import br.com.jess.chronos.pulse.modules.auth.domain.ports.input.RedefinirSenhaUseCase;
import br.com.jess.chronos.pulse.modules.auth.domain.ports.output.CpcUsuarioRepositoryPort;
import br.com.jess.chronos.pulse.modules.auth.domain.ports.output.RecuperacaoSenhaRepositoryPort;
import org.springframework.security.crypto.password.PasswordEncoder;

public class RedefinirSenhaUseCaseImpl implements RedefinirSenhaUseCase {

    private final CpcUsuarioRepositoryPort usuarioRepository;
    private final RecuperacaoSenhaRepositoryPort recuperacaoSenhaRepository;
    private final PasswordEncoder passwordEncoder;

    public RedefinirSenhaUseCaseImpl(CpcUsuarioRepositoryPort usuarioRepository,
                                     RecuperacaoSenhaRepositoryPort recuperacaoSenhaRepository,
                                     PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.recuperacaoSenhaRepository = recuperacaoSenhaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void executar(Comando comando) {
        String cpf = comando.cpf().replaceAll("\\D", "");

        RecuperacaoSenha recuperacaoSenha = recuperacaoSenhaRepository.buscarUltimaPorCpf(cpf)
                .orElseThrow(() -> new IllegalArgumentException("Codigo de recuperacao invalido ou expirado."));

        if (recuperacaoSenha.isUsado()
                || recuperacaoSenha.isExpirada()
                || !passwordEncoder.matches(comando.codigo(), recuperacaoSenha.getCodigoHash())) {
            throw new IllegalArgumentException("Codigo de recuperacao invalido ou expirado.");
        }

        var usuario = usuarioRepository.buscarPorCpf(cpf)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        usuarioRepository.atualizar(usuario.comSenha(passwordEncoder.encode(comando.novaSenha())));

        recuperacaoSenha.marcarComoUsado();
        recuperacaoSenhaRepository.atualizar(recuperacaoSenha);
        recuperacaoSenhaRepository.marcarTodasComoUsadas(cpf);
    }
}