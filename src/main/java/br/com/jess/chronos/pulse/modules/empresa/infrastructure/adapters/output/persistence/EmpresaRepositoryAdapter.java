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
    public boolean existePorCnpj(String cnpj) {
        return jpaRepository.existsByCnpj(cnpj);
    }

    @Override
    public List<Empresa> listarTodos() {
        return jpaRepository.findAll().stream().map(mapper::toModel).toList();
    }
}
