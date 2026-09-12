package br.com.jess.chronos.pulse.modules.lead.infrastructure.adapters.output.persistence;

import br.com.jess.chronos.pulse.modules.lead.domain.model.LeadEmpresa;
import br.com.jess.chronos.pulse.modules.lead.domain.ports.output.LeadEmpresaRepositoryPort;
import org.springframework.stereotype.Component;

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
}