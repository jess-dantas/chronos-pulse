package br.com.jess.chronos.pulse.modules.titularidade.infrastructure.adapters.output.persistence;

import br.com.jess.chronos.pulse.modules.titularidade.domain.model.TitularidadeCodigo;
import br.com.jess.chronos.pulse.modules.titularidade.domain.model.TitularidadeTransferencia;
import br.com.jess.chronos.pulse.modules.titularidade.domain.ports.output.TitularidadeRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class TitularidadeRepositoryAdapter implements TitularidadeRepositoryPort {

    private final TitularidadeTransferenciaJpaRepository transferenciaJpaRepository;
    private final TitularidadeCodigoJpaRepository codigoJpaRepository;

    public TitularidadeRepositoryAdapter(
            TitularidadeTransferenciaJpaRepository transferenciaJpaRepository,
            TitularidadeCodigoJpaRepository codigoJpaRepository) {
        this.transferenciaJpaRepository = transferenciaJpaRepository;
        this.codigoJpaRepository = codigoJpaRepository;
    }

    @Override
    public TitularidadeTransferencia salvar(TitularidadeTransferencia transferencia) {
        return transferenciaJpaRepository.save(transferencia);
    }

    @Override
    public Optional<TitularidadeTransferencia> buscarPorId(UUID id) {
        return transferenciaJpaRepository.findById(id);
    }

    @Override
    public void cancelarAbertas(UUID tenantId, UUID solicitanteId) {
        transferenciaJpaRepository.cancelarAbertas(tenantId, solicitanteId);
    }

    @Override
    public TitularidadeCodigo salvarCodigo(TitularidadeCodigo codigo) {
        return codigoJpaRepository.save(codigo);
    }

    @Override
    public List<TitularidadeCodigo> listarCodigos(UUID transferenciaId, String etapa) {
        return codigoJpaRepository
                .findByTransferenciaIdAndEtapaOrderByCriadoEmDesc(transferenciaId, etapa);
    }

    @Override
    public void removerCodigos(UUID transferenciaId, String etapa) {
        codigoJpaRepository.deleteByTransferenciaIdAndEtapa(transferenciaId, etapa);
    }
}
