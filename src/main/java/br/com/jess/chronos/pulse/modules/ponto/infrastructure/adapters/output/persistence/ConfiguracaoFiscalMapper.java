package br.com.jess.chronos.pulse.modules.ponto.infrastructure.adapters.output.persistence;

import br.com.jess.chronos.pulse.modules.ponto.domain.model.ConfiguracaoFiscal;
import org.mapstruct.Mapper;
import org.mapstruct.ObjectFactory;

@Mapper(componentModel = "spring")
public interface ConfiguracaoFiscalMapper {

    ConfiguracaoFiscalJpaEntity toEntity(ConfiguracaoFiscal model);

    @ObjectFactory
    default ConfiguracaoFiscal toModel(ConfiguracaoFiscalJpaEntity e) {
        return new ConfiguracaoFiscal(e.getTenantId(), e.getNumeroRegistroInpi(),
                e.getCnpjDesenvolvedor(), e.getPrtpNome(), e.getPrtpVersao(),
                e.getPrtpRazaoDesenv(), e.getPrtpEmail(), e.getCno());
    }
}