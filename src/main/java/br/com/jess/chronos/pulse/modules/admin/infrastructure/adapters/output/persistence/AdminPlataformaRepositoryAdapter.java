package br.com.jess.chronos.pulse.modules.admin.infrastructure.adapters.output.persistence;

import br.com.jess.chronos.pulse.modules.admin.domain.model.AdminPlataforma;
import br.com.jess.chronos.pulse.modules.admin.domain.ports.output.AdminPlataformaRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class AdminPlataformaRepositoryAdapter implements AdminPlataformaRepositoryPort {

    private final AdminPlataformaJpaRepository jpaRepository;
    private final AdminPlataformaMapper mapper;

    public AdminPlataformaRepositoryAdapter(AdminPlataformaJpaRepository jpaRepository,
                                            AdminPlataformaMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public AdminPlataforma salvar(AdminPlataforma admin) {
        return mapper.toModel(jpaRepository.save(mapper.toEntity(admin)));
    }

    @Override
    public Optional<AdminPlataforma> buscarPorId(UUID id) {
        return jpaRepository.findById(id).map(mapper::toModel);
    }

    @Override
    public Optional<AdminPlataforma> buscarPorUsername(String username) {
        return jpaRepository.findByUsername(username).map(mapper::toModel);
    }

    @Override
    public Optional<AdminPlataforma> buscarPorEmail(String email) {
        return jpaRepository.findByEmail(email).map(mapper::toModel);
    }

    @Override
    public boolean existsByUsername(String username) {
        return jpaRepository.existsByUsername(username);
    }

    @Override
    public long count() {
        return jpaRepository.count();
    }
}