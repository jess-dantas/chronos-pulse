package br.com.jess.chronos.pulse.modules.ponto.domain.ports.output;

import br.com.jess.chronos.pulse.modules.ponto.domain.model.RegistroPonto;
import br.com.jess.chronos.pulse.modules.ponto.domain.model.TipoRegistro;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RegistroPontoRepositoryPort {
    RegistroPonto salvar(RegistroPonto registro);
    Optional<RegistroPonto> buscarPorId(UUID id);
    Long obterProximoNsr();
    Optional<TipoRegistro> buscarUltimoTipoPorColaborador(UUID colaboradorId, UUID tenantId);
    List<RegistroPonto> listarPorColaboradorEPeriodo(UUID colaboradorId, UUID tenantId, Instant inicio, Instant fim);
    List<RegistroPonto> listarPorColaborador(UUID colaboradorId, UUID tenantId);
    List<RegistroPonto> listarPorTenant(UUID tenantId);
}