package br.com.jess.chronos.pulse.modules.auth.application.usecases;

import br.com.jess.chronos.pulse.modules.auth.domain.ports.input.AlterarFotoPerfilUseCase;
import br.com.jess.chronos.pulse.modules.auth.domain.ports.output.CpcUsuarioRepositoryPort;

import java.util.Base64;

public class AlterarFotoPerfilUseCaseImpl implements AlterarFotoPerfilUseCase {

    private static final long MAX_BYTES = 512L * 1024L;

    private final CpcUsuarioRepositoryPort usuarioRepository;

    public AlterarFotoPerfilUseCaseImpl(CpcUsuarioRepositoryPort usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public String executar(Comando comando) {
        if (comando.bytes() == null || comando.bytes().length == 0) {
            throw new IllegalArgumentException("Arquivo de imagem inválido.");
        }
        if (comando.bytes().length > MAX_BYTES) {
            throw new IllegalArgumentException("Imagem deve ter no maximo 512KB.");
        }

        String fotoBase64 = Base64.getEncoder().encodeToString(comando.bytes());

        var usuario = usuarioRepository.buscarPorCpf(comando.cpf())
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        usuarioRepository.atualizar(usuario.comFoto(fotoBase64));

        return fotoBase64;
    }
}