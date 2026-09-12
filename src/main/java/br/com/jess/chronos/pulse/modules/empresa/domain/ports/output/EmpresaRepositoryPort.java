package br.com.jess.chronos.pulse.modules.empresa.domain.ports.output;

import br.com.jess.chronos.pulse.modules.empresa.domain.model.Empresa;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EmpresaRepositoryPort {
    Empresa salvar(Empresa empresa);
    Optional<Empresa> buscarPorId(UUID id);
    Optional<Empresa> buscarPorCnpj(String cnpj);
    Optional<Empresa> buscarPorSlug(String slug);
    boolean existePorCnpj(String cnpj);
    boolean existePorSlug(String slug);
    List<Empresa> listarTodos();
    Empresa atualizar(UUID id, String nome, Boolean ativo);
}
