package br.com.jess.chronos.pulse.modules.admin.infrastructure.adapters.output.persistence;

import br.com.jess.chronos.pulse.modules.admin.domain.model.Contrato;
import br.com.jess.chronos.pulse.modules.admin.domain.model.ContratoEvento;
import br.com.jess.chronos.pulse.modules.admin.domain.ports.output.ContratoRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class ContratoRepositoryAdapter implements ContratoRepositoryPort {

    private final ContratoJpaRepository contratoJpaRepository;
    private final ContratoEventoJpaRepository eventoJpaRepository;
    private final ContratoMapper contratoMapper;
    private final ContratoEventoMapper eventoMapper;

    // Repositórios de outros módulos para métricas
    private final br.com.jess.chronos.pulse.modules.empresa.infrastructure.adapters.output.persistence.EmpresaJpaRepository empresaJpaRepository;
    private final br.com.jess.chronos.pulse.modules.auth.infrastructure.adapters.output.persistence.CpcUsuarioJpaRepository usuarioJpaRepository;

    public ContratoRepositoryAdapter(
            ContratoJpaRepository contratoJpaRepository,
            ContratoEventoJpaRepository eventoJpaRepository,
            ContratoMapper contratoMapper,
            ContratoEventoMapper eventoMapper,
            br.com.jess.chronos.pulse.modules.empresa.infrastructure.adapters.output.persistence.EmpresaJpaRepository empresaJpaRepository,
            br.com.jess.chronos.pulse.modules.auth.infrastructure.adapters.output.persistence.CpcUsuarioJpaRepository usuarioJpaRepository) {
        this.contratoJpaRepository = contratoJpaRepository;
        this.eventoJpaRepository = eventoJpaRepository;
        this.contratoMapper = contratoMapper;
        this.eventoMapper = eventoMapper;
        this.empresaJpaRepository = empresaJpaRepository;
        this.usuarioJpaRepository = usuarioJpaRepository;
    }

    @Override
    public Contrato salvar(Contrato contrato) {
        return contratoMapper.toModel(contratoJpaRepository.save(contratoMapper.toEntity(contrato)));
    }

    @Override
    public Optional<Contrato> buscarPorId(UUID id) {
        return contratoJpaRepository.findById(id).map(contratoMapper::toModel);
    }

    @Override
    public List<Contrato> listarPorTenant(UUID tenantId) {
        return contratoJpaRepository.findByTenantId(tenantId).stream()
                .map(contratoMapper::toModel).toList();
    }

    @Override
    public List<Contrato> listarTodos() {
        return contratoJpaRepository.findAll().stream()
                .map(contratoMapper::toModel).toList();
    }

    @Override
    public long contarAtivos() {
        return contratoJpaRepository.countByStatus("ATIVO");
    }

    @Override
    public ContratoEvento salvarEvento(ContratoEvento evento) {
        return eventoMapper.toModel(eventoJpaRepository.save(eventoMapper.toEntity(evento)));
    }

    @Override
    public List<ContratoEvento> listarEventosPorContrato(UUID contratoId) {
        return eventoJpaRepository.findByContratoIdOrderByCriadoEmDesc(contratoId).stream()
                .map(eventoMapper::toModel).toList();
    }

    @Override
    public long contarTenantsAtivos() {
        return empresaJpaRepository.count();
    }

    @Override
    public long contarTotalColaboradores() {
        return usuarioJpaRepository.count();
    }
}
