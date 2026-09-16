package br.com.jess.chronos.pulse.modules.lead.domain.ports.input;

import br.com.jess.chronos.pulse.modules.lead.domain.model.LeadEmpresa;
import br.com.jess.chronos.pulse.modules.lead.domain.model.LeadEmpresaStatus;

import java.util.UUID;

public interface AtualizarStatusLeadUseCase {

    LeadEmpresa executar(UUID id, LeadEmpresaStatus status);
}