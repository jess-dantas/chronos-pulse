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
        if (!isImagemValida(comando.bytes())) {
            throw new IllegalArgumentException("Formato de imagem não suportado. Use JPEG, PNG ou WebP.");
        }

        String fotoBase64 = Base64.getEncoder().encodeToString(comando.bytes());

        var usuario = usuarioRepository.buscarPorCpf(comando.cpf())
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        usuarioRepository.atualizar(usuario.comFoto(fotoBase64));

        return fotoBase64;
    }

    private boolean isImagemValida(byte[] bytes) {
        if (bytes.length < 12) {
            return false;
        }
        // JPEG: FFD8FF
        if ((bytes[0] & 0xFF) == 0xFF && (bytes[1] & 0xFF) == 0xD8 && (bytes[2] & 0xFF) == 0xFF) {
            return true;
        }
        // PNG: 89504E470D0A1A0A
        if ((bytes[0] & 0xFF) == 0x89 && bytes[1] == 0x50 && bytes[2] == 0x4E && bytes[3] == 0x47
                && bytes[4] == 0x0D && bytes[5] == 0x0A && bytes[6] == 0x1A && bytes[7] == 0x0A) {
            return true;
        }
        // WebP: RIFF....WEBP
        if (bytes[0] == 'R' && bytes[1] == 'I' && bytes[2] == 'F' && bytes[3] == 'F'
                && bytes[8] == 'W' && bytes[9] == 'E' && bytes[10] == 'B' && bytes[11] == 'P') {
            return true;
        }
        return false;
    }
}