package br.com.jess.chronos.pulse.modules.auth.application.usecases;

import br.com.jess.chronos.pulse.modules.auth.domain.ports.input.AlterarSenhaUseCase;
import br.com.jess.chronos.pulse.modules.auth.domain.ports.output.CpcUsuarioRepositoryPort;
import br.com.jess.chronos.pulse.modules.auth.domain.service.PasswordPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;

public class AlterarSenhaUseCaseImpl implements AlterarSenhaUseCase {

    private final CpcUsuarioRepositoryPort usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public AlterarSenhaUseCaseImpl(CpcUsuarioRepositoryPort usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void executar(Comando comando) {
        var usuario = usuarioRepository.buscarPorCpf(comando.cpf())
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        PasswordPolicy.validar(comando.novaSenha(), usuario.getRole())
                .ifPresent(mensagem -> {
                    throw new IllegalArgumentException(mensagem);
                });

        var usuarioAtualizado = usuario.comSenha(passwordEncoder.encode(comando.novaSenha()));
        usuarioRepository.atualizar(usuarioAtualizado);
    }
}