package br.com.jess.chronos.pulse.modules.modulo.domain.ports.output;

import java.util.List;
import java.util.UUID;

public interface ModulosPort {

    List<String> listarCodigosAtivos(UUID tenantId);

    List<String> listarCodigosDoUsuario(UUID usuarioId, UUID tenantId);

    boolean usuarioModuloAtivo(UUID usuarioId, UUID tenantId, String codigo);

    void definirModulosDoUsuario(UUID usuarioId, UUID tenantId, List<String> codigos);

    boolean isAtivo(UUID tenantId, String codigo);

    void ativarModulosPadrao(UUID tenantId);
}