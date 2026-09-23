package br.com.jess.chronos.pulse.modules.modulo.infrastructure.adapters.output;

import br.com.jess.chronos.pulse.modules.modulo.domain.ports.output.ModulosPort;
import br.com.jess.chronos.pulse.modules.modulo.repository.UsuarioModuloJpaEntity;
import br.com.jess.chronos.pulse.modules.modulo.repository.UsuarioModuloJpaRepository;
import br.com.jess.chronos.pulse.modules.modulo.service.ModuloService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ModulosPortAdapter implements ModulosPort {

    private final ModuloService moduloService;
    private final UsuarioModuloJpaRepository usuarioModuloRepository;

    @Override
    public List<String> listarCodigosAtivos(UUID tenantId) {
        return moduloService.listarCodigosAtivos(tenantId);
    }

    @Override
    public List<String> listarCodigosDoUsuario(UUID usuarioId, UUID tenantId) {
        if (tenantId == null) {
            return List.of();
        }
        var ativos = listarCodigosAtivos(tenantId);
        var doUsuario = usuarioModuloRepository.findByUsuarioIdAndTenantId(usuarioId, tenantId)
                .stream()
                .map(UsuarioModuloJpaEntity::getCodigo)
                .toList();
        return ativos.stream().filter(doUsuario::contains).toList();
    }

    @Override
    public boolean usuarioModuloAtivo(UUID usuarioId, UUID tenantId, String codigo) {
        return listarCodigosDoUsuario(usuarioId, tenantId).contains(codigo);
    }

    @Override
    public void definirModulosDoUsuario(UUID usuarioId, UUID tenantId, List<String> codigos) {
        if (tenantId == null) {
            throw new IllegalArgumentException("Tenant ID obrigatório.");
        }
        var permitidos = new HashSet<>(listarCodigosAtivos(tenantId));
        var validos = codigos == null ? List.<String>of()
                : codigos.stream().filter(permitidos::contains).distinct().toList();
        usuarioModuloRepository.deleteByUsuarioIdAndTenantId(usuarioId, tenantId);
        for (String codigo : validos) {
            var entidade = new UsuarioModuloJpaEntity();
            entidade.setUsuarioId(usuarioId);
            entidade.setTenantId(tenantId);
            entidade.setCodigo(codigo);
            usuarioModuloRepository.save(entidade);
        }
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
