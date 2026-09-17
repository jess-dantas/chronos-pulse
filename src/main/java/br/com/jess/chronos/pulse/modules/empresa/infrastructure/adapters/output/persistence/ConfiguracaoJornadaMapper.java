package br.com.jess.chronos.pulse.modules.empresa.infrastructure.adapters.output.persistence;

import br.com.jess.chronos.pulse.modules.empresa.domain.model.ConfiguracaoJornada;
import org.mapstruct.Mapper;
import org.mapstruct.ObjectFactory;

@Mapper(componentModel = "spring")
public interface ConfiguracaoJornadaMapper {

    ConfiguracaoJornadaJpaEntity toEntity(ConfiguracaoJornada model);

    ConfiguracaoJornada toModel(ConfiguracaoJornadaJpaEntity entity);

    @ObjectFactory
    default ConfiguracaoJornada criarModel(ConfiguracaoJornadaJpaEntity e) {
        return new ConfiguracaoJornada(e.getId(), e.getTenantId(), e.getNome(),
                e.getCargaHorariaDiariaMinutos(), e.getToleranciaEntradaMinutos(),
                e.getToleranciaSaidaMinutos(), e.getInterjornadaMinimaMinutos());
    }
}