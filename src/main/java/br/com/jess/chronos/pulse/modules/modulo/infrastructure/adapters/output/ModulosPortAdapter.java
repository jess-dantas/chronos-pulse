package br.com.jess.chronos.pulse.modules.modulo.infrastructure.adapters.output;

import br.com.jess.chronos.pulse.modules.modulo.domain.ports.output.ModulosPort;
import br.com.jess.chronos.pulse.modules.modulo.service.ModuloService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ModulosPortAdapter implements ModulosPort {

    private final ModuloService moduloService;

    @Override
    public List<String> listarCodigosAtivos(UUID tenantId) {
        return moduloService.listarCodigosAtivos(tenantId);
    }

    @Override
    public boolean isAtivo(UUID tenantId, String codigo) {
        return moduloService.isAtivo(tenantId, codigo);
    }

    @Override
    public void ativarModulosPadrao(UUID tenantId) {
        moduloService.ativarModulosPadrao(tenantId);
    }
}