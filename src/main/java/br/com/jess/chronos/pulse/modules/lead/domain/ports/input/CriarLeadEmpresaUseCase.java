package br.com.jess.chronos.pulse.modules.lead.domain.ports.input;

import br.com.jess.chronos.pulse.modules.lead.domain.model.LeadEmpresa;

public interface CriarLeadEmpresaUseCase {

    LeadEmpresa executar(LeadEmpresa lead);
}