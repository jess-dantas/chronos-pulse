package br.com.jess.chronos.pulse.modules.admin.domain.ports.output;

import br.com.jess.chronos.pulse.modules.admin.domain.model.Contrato;
import br.com.jess.chronos.pulse.modules.admin.domain.model.ContratoEvento;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ContratoRepositoryPort {

    Contrato salvar(Contrato contrato);
    Optional<Contrato> buscarPorId(UUID id);
    List<Contrato> listarPorTenant(UUID tenantId);
    List<Contrato> listarTodos();
    long contarAtivos();

    ContratoEvento salvarEvento(ContratoEvento evento);
    List<ContratoEvento> listarEventosPorContrato(UUID contratoId);

    long contarTenantsAtivos();
    long contarTotalColaboradores();
}
