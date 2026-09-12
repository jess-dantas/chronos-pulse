package br.com.jess.chronos.pulse.modules.empresa.infrastructure.adapters.output.persistence;

import br.com.jess.chronos.pulse.modules.empresa.domain.model.Empresa;
import br.com.jess.chronos.pulse.modules.empresa.domain.ports.output.EmpresaRepositoryPort;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class EmpresaRepositoryAdapter implements EmpresaRepositoryPort {

    private final EmpresaJpaRepository jpaRepository;
    private final EmpresaMapper mapper;

    public EmpresaRepositoryAdapter(EmpresaJpaRepository jpaRepository, EmpresaMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Empresa salvar(Empresa empresa) {
        return mapper.toModel(jpaRepository.save(mapper.toEntity(empresa)));
    }

    @Override
    public Optional<Empresa> buscarPorId(UUID id) {
        return jpaRepository.findById(id).map(mapper::toModel);
    }

    @Override
    public Optional<Empresa> buscarPorCnpj(String cnpj) {
        return jpaRepository.findByCnpj(cnpj).map(mapper::toModel);
    }

    @Override
    public Optional<Empresa> buscarPorSlug(String slug) {
        return jpaRepository.findBySlug(slug).map(mapper::toModel);
    }

    @Override
    public boolean existePorCnpj(String cnpj) {
        return jpaRepository.existsByCnpj(cnpj);
    }

    @Override
    public boolean existePorSlug(String slug) {
        return jpaRepository.existsBySlug(slug);
    }

    @Override
    public List<Empresa> listarTodos() {
        return jpaRepository.findAll().stream().map(mapper::toModel).toList();
    }

    @Override
    public Empresa atualizar(UUID id, String nome, Boolean ativo) {
        var entity = jpaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Empresa não encontrada"));
        if (nome != null && !nome.isBlank()) {
            entity.setNome(nome);
        }
        if (ativo != null) {
            entity.setAtivo(ativo);
        }
        return mapper.toModel(jpaRepository.save(entity));
    }
}
