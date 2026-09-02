package br.com.jess.chronos.pulse.modules.ponto.infrastructure.adapters.output.persistence;

import br.com.jess.chronos.pulse.modules.ponto.domain.model.RegistroPonto;
import br.com.jess.chronos.pulse.modules.ponto.domain.ports.output.RegistroPontoRepositoryPort;
import org.springframework.stereotype.Component;
import java.util.Optional;
import java.util.UUID;

@Component
public class RegistroPontoRepositoryAdapter implements RegistroPontoRepositoryPort {

    private final RegistroPontoJpaRepository jpaRepository;
    private final RegistroPontoMapper mapper;

    public RegistroPontoRepositoryAdapter(RegistroPontoJpaRepository jpaRepository, RegistroPontoMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public RegistroPonto salvar(RegistroPonto registro) {
        return mapper.toModel(jpaRepository.save(mapper.toEntity(registro)));
    }

    @Override
    public Optional<RegistroPonto> buscarPorId(UUID id) {
        return jpaRepository.findById(id).map(mapper::toModel);
    }

    @Override
    public Long obterProximoNsr() {
        return jpaRepository.obterProximoNsr();
    }
}
