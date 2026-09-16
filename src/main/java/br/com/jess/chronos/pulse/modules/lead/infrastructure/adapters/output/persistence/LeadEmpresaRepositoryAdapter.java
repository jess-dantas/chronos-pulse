package br.com.jess.chronos.pulse.modules.lead.infrastructure.adapters.output.persistence;

import br.com.jess.chronos.pulse.modules.lead.domain.model.LeadEmpresa;
import br.com.jess.chronos.pulse.modules.lead.domain.ports.output.LeadEmpresaRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class LeadEmpresaRepositoryAdapter implements LeadEmpresaRepositoryPort {

    private final LeadEmpresaJpaRepository jpaRepository;
    private final LeadEmpresaMapper mapper;

    public LeadEmpresaRepositoryAdapter(LeadEmpresaJpaRepository jpaRepository, LeadEmpresaMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public LeadEmpresa salvar(LeadEmpresa lead) {
        return mapper.toModel(jpaRepository.save(mapper.toEntity(lead)));
    }

    @Override
    public List<LeadEmpresa> listarTodos() {
        return jpaRepository.findAllByOrderByCriadoEmDesc().stream()
                .map(mapper::toModel)
                .toList();
    }

    @Override
    public Optional<LeadEmpresa> buscarPorId(UUID id) {
        return jpaRepository.findById(id).map(mapper::toModel);
    }
}