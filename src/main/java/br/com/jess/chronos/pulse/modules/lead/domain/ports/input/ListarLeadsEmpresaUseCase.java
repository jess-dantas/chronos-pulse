package br.com.jess.chronos.pulse.modules.lead.domain.ports.input;

import br.com.jess.chronos.pulse.modules.lead.domain.model.LeadEmpresa;

import java.util.List;

public interface ListarLeadsEmpresaUseCase {

    List<LeadEmpresa> executar();
}