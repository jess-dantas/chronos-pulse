package br.com.jess.chronos.pulse.modules.modulo.service;

import br.com.jess.chronos.pulse.modules.auth.domain.model.CpcUsuario;
import br.com.jess.chronos.pulse.modules.auth.domain.model.Role;
import br.com.jess.chronos.pulse.modules.auth.domain.ports.output.CpcUsuarioRepositoryPort;
import br.com.jess.chronos.pulse.modules.modulo.domain.ports.output.ModulosPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class UsuarioModuloService {

    private final CpcUsuarioRepositoryPort usuarioRepository;
    private final ModulosPort modulosPort;

    public UsuarioModuloService(CpcUsuarioRepositoryPort usuarioRepository,
                                ModulosPort modulosPort) {
        this.usuarioRepository = usuarioRepository;
        this.modulosPort = modulosPort;
    }

    @Transactional
    public Map<String, Object> listar(UUID usuarioId, UUID tenantId, CpcUsuario solicitante) {
        validarTenant(solicitante, tenantId);
        CpcUsuario alvo = usuarioRepository.buscarPorId(usuarioId)
                .filter(u -> tenantId.equals(u.getTenantId()))
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado no seu tenant"));
        if (alvo.getRole() == Role.ADMIN_EMPRESA || alvo.getRole() == Role.ADMIN_PLATAFORMA) {
            return Map.of("usuarioId", alvo.getId().toString(), "modulos", modulosPort.listarCodigosAtivos(tenantId));
        }
        return Map.of("usuarioId", alvo.getId().toString(), "modulos",
                modulosPort.listarCodigosDoUsuario(alvo.getId(), tenantId));
    }

    @Transactional
    public Map<String, Object> atualizar(UUID usuarioId, UUID tenantId, List<String> codigos,
                                         CpcUsuario solicitante) {
        validarTenant(solicitante, tenantId);
        CpcUsuario alvo = usuarioRepository.buscarPorId(usuarioId)
                .filter(u -> tenantId.equals(u.getTenantId()))
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado no seu tenant"));
        if (alvo.getRole() == Role.ADMIN_EMPRESA || alvo.getRole() == Role.ADMIN_PLATAFORMA) {
            throw new IllegalArgumentException("Não é permitido alterar módulos de administradores.");
        }
        List<String> definidos = codigos == null ? List.of() : codigos;
        modulosPort.definirModulosDoUsuario(alvo.getId(), tenantId, definidos);

        // Mantém os flags legados (usados nas claims JWT) em sincronia com os códigos.
        CpcUsuario sincronizado = new CpcUsuario(
                alvo.getId(), alvo.getCpcId(), alvo.getCpf(),
                alvo.getNome(), alvo.getEmailCorporativo(),
                alvo.getSenhaHash(), alvo.getRole(), alvo.getTenantId(),
                definidos.contains("ESTOQUE"), definidos.contains("PATRIMONIO"),
                definidos.contains("FROTA"), definidos.contains("PROTOCOLO"), alvo.getFoto());
        usuarioRepository.atualizar(sincronizado);

        return Map.of("usuarioId", alvo.getId().toString(), "modulos",
                modulosPort.listarCodigosDoUsuario(alvo.getId(), tenantId));
    }

    private void validarTenant(CpcUsuario solicitante, UUID tenantId) {
        if (tenantId == null) {
            throw new IllegalArgumentException("Tenant ID obrigatório.");
        }
        boolean plataforma = solicitante.getRole() == Role.ADMIN_PLATAFORMA
                || solicitante.getRole() == Role.SUPORTE_N1
                || solicitante.getRole() == Role.SUPORTE_N2;
        if (!plataforma && !tenantId.equals(solicitante.getTenantId())) {
            throw new IllegalArgumentException("Não é permitido gerenciar módulos de outro tenant.");
        }
    }
}
