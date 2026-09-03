package br.com.jess.chronos.pulse.modules.colaborador.infrastructure.adapters.output.persistence;

import br.com.jess.chronos.pulse.modules.colaborador.domain.model.Colaborador;
import br.com.jess.chronos.pulse.modules.colaborador.domain.ports.output.ColaboradorRepositoryPort;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class ColaboradorRepositoryAdapter implements ColaboradorRepositoryPort {

    private final ColaboradorJpaRepository jpaRepository;
    private final ColaboradorMapper mapper;

    public ColaboradorRepositoryAdapter(ColaboradorJpaRepository jpaRepository, ColaboradorMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Colaborador salvar(Colaborador colaborador) {
        return mapper.toModel(jpaRepository.save(mapper.toEntity(colaborador)));
    }

    @Override
    public Optional<Colaborador> buscarPorId(UUID id) {
        return jpaRepository.findById(id).map(mapper::toModel);
    }

    @Override
    public Optional<Colaborador> buscarPorCpcUsuarioId(UUID cpcUsuarioId) {
        return jpaRepository.findByCpcUsuarioId(cpcUsuarioId).map(mapper::toModel);
    }

    @Override
    public List<Colaborador> listarPorTenant(UUID tenantId) {
        return jpaRepository.findByTenantId(tenantId).stream().map(mapper::toModel).toList();
    }

    @Override
    public List<Colaborador> listarTodos() {
        return jpaRepository.findAll().stream().map(mapper::toModel).toList();
    }
}
