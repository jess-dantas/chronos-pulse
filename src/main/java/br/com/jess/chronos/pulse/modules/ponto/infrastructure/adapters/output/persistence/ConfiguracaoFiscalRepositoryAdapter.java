package br.com.jess.chronos.pulse.modules.ponto.infrastructure.adapters.output.persistence;

import br.com.jess.chronos.pulse.modules.ponto.domain.model.ConfiguracaoFiscal;
import br.com.jess.chronos.pulse.modules.ponto.domain.ports.output.ConfiguracaoFiscalRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class ConfiguracaoFiscalRepositoryAdapter implements ConfiguracaoFiscalRepositoryPort {

    private final ConfiguracaoFiscalJpaRepository jpaRepository;
    private final ConfiguracaoFiscalMapper mapper;

    public ConfiguracaoFiscalRepositoryAdapter(ConfiguracaoFiscalJpaRepository jpaRepository,
                                               ConfiguracaoFiscalMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<ConfiguracaoFiscal> buscarPorTenant(UUID tenantId) {
        return jpaRepository.findById(tenantId).map(mapper::toModel);
    }

    @Override
    public ConfiguracaoFiscal salvar(ConfiguracaoFiscal configuracao) {
        return mapper.toModel(jpaRepository.save(mapper.toEntity(configuracao)));
    }
}