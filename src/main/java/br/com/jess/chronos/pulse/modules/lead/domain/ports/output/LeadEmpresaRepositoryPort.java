package br.com.jess.chronos.pulse.modules.lead.domain.ports.output;

import br.com.jess.chronos.pulse.modules.lead.domain.model.LeadEmpresa;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LeadEmpresaRepositoryPort {

    LeadEmpresa salvar(LeadEmpresa lead);

    List<LeadEmpresa> listarTodos();

    Optional<LeadEmpresa> buscarPorId(UUID id);
}