package br.com.jess.chronos.pulse.modules.modulo.domain.ports.output;

import java.util.List;
import java.util.UUID;

public interface ModulosPort {

    List<String> listarCodigosAtivos(UUID tenantId);

    boolean isAtivo(UUID tenantId, String codigo);

    void ativarModulosPadrao(UUID tenantId);
}