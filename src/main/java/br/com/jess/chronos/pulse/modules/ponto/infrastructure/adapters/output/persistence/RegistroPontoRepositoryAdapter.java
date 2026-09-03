package br.com.jess.chronos.pulse.modules.ponto.infrastructure.adapters.output.persistence;

import br.com.jess.chronos.pulse.modules.ponto.domain.model.RegistroPonto;
import br.com.jess.chronos.pulse.modules.ponto.domain.model.TipoRegistro;
import br.com.jess.chronos.pulse.modules.ponto.domain.ports.output.RegistroPontoRepositoryPort;
import org.springframework.stereotype.Component;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class RegistroPontoRepositoryAdapter implements RegistroPontoRepositoryPort {

    private final RegistroPontoJpaRepository jpaRepository;
    private final RegistroPontoMapper mapper;

    public RegistroPontoRepositoryAdapter(RegistroPontoJpaRepository jpaRepository, RegistroPontoMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public RegistroPonto salvar(RegistroPonto registro) {
        return mapper.toModel(jpaRepository.save(mapper.toEntity(registro)));
    }

    @Override
    public Optional<RegistroPonto> buscarPorId(UUID id) {
        return jpaRepository.findById(id).map(mapper::toModel);
    }

    @Override
    public Long obterProximoNsr() {
        return jpaRepository.obterProximoNsr();
    }

    @Override
    public Optional<TipoRegistro> buscarUltimoTipoPorColaborador(UUID colaboradorId, UUID tenantId) {
        return jpaRepository.buscarUltimoTipoPorColaborador(colaboradorId, tenantId);
    }

    @Override
    public List<RegistroPonto> listarPorColaboradorEPeriodo(UUID colaboradorId, UUID tenantId, Instant inicio, Instant fim) {
        return jpaRepository.findByColaboradorIdAndTenantIdAndDataHoraDispositivoBetweenOrderByDataHoraDispositivoAsc(
                colaboradorId, tenantId, inicio, fim).stream().map(mapper::toModel).toList();
    }

    @Override
    public List<RegistroPonto> listarPorColaborador(UUID colaboradorId, UUID tenantId) {
        return jpaRepository.findByColaboradorIdAndTenantIdOrderByDataHoraDispositivoAsc(
                colaboradorId, tenantId).stream().map(mapper::toModel).toList();
    }

    @Override
    public List<RegistroPonto> listarPorTenant(UUID tenantId) {
        return jpaRepository.findByTenantIdOrderByDataHoraDispositivoAsc(tenantId)
                .stream().map(mapper::toModel).toList();
    }
}
