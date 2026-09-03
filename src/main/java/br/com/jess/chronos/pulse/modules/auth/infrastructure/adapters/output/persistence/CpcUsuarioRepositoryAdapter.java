package br.com.jess.chronos.pulse.modules.auth.infrastructure.adapters.output.persistence;

import br.com.jess.chronos.pulse.modules.auth.domain.model.CpcUsuario;
import br.com.jess.chronos.pulse.modules.auth.domain.ports.output.CpcUsuarioRepositoryPort;
import org.springframework.stereotype.Component;
import java.util.Optional;
import java.util.UUID;

@Component
public class CpcUsuarioRepositoryAdapter implements CpcUsuarioRepositoryPort {

    private final CpcUsuarioJpaRepository jpaRepository;
    private final CpcUsuarioMapper mapper;

    public CpcUsuarioRepositoryAdapter(CpcUsuarioJpaRepository jpaRepository, CpcUsuarioMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public CpcUsuario salvar(CpcUsuario usuario) {
        return mapper.toModel(jpaRepository.save(mapper.toEntity(usuario)));
    }

    @Override
    public Optional<CpcUsuario> buscarPorCpf(String cpf) {
        return jpaRepository.findByCpf(cpf).map(mapper::toModel);
    }

    @Override
    public Optional<CpcUsuario> buscarPorId(UUID id) {
        return jpaRepository.findById(id).map(mapper::toModel);
    }

    @Override
    public boolean existePorCpf(String cpf) {
        return jpaRepository.existsByCpf(cpf);
    }
}
