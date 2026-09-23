package br.com.jess.chronos.pulse.modules.titularidade.domain.ports.output;

import br.com.jess.chronos.pulse.modules.titularidade.domain.model.TitularidadeCodigo;
import br.com.jess.chronos.pulse.modules.titularidade.domain.model.TitularidadeTransferencia;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TitularidadeRepositoryPort {

    TitularidadeTransferencia salvar(TitularidadeTransferencia transferencia);

    Optional<TitularidadeTransferencia> buscarPorId(UUID id);

    void cancelarAbertas(UUID tenantId, UUID solicitanteId);

    TitularidadeCodigo salvarCodigo(TitularidadeCodigo codigo);

    List<TitularidadeCodigo> listarCodigos(UUID transferenciaId, String etapa);

    void removerCodigos(UUID transferenciaId, String etapa);
}
