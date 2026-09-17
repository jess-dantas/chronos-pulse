package br.com.jess.chronos.pulse.modules.ponto.domain.ports.output;

import br.com.jess.chronos.pulse.modules.ponto.domain.model.ConfiguracaoFiscal;

import java.util.Optional;
import java.util.UUID;

public interface ConfiguracaoFiscalRepositoryPort {

    Optional<ConfiguracaoFiscal> buscarPorTenant(UUID tenantId);

    ConfiguracaoFiscal salvar(ConfiguracaoFiscal configuracao);
}