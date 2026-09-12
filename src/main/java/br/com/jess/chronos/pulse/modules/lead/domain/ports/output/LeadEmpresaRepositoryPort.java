package br.com.jess.chronos.pulse.modules.lead.domain.ports.output;

import br.com.jess.chronos.pulse.modules.lead.domain.model.LeadEmpresa;

public interface LeadEmpresaRepositoryPort {

    LeadEmpresa salvar(LeadEmpresa lead);
}