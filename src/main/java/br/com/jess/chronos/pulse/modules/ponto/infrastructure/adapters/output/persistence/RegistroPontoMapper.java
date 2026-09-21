package br.com.jess.chronos.pulse.modules.ponto.infrastructure.adapters.output.persistence;

import br.com.jess.chronos.pulse.modules.ponto.domain.model.RegistroPonto;
import br.com.jess.chronos.pulse.modules.ponto.domain.model.AjusteStatus;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface RegistroPontoMapper {

    RegistroPontoJpaEntity toEntity(RegistroPonto model);

    @Mapping(target = "hashIntegridade", ignore = true)
    RegistroPonto toModel(RegistroPontoJpaEntity entity);

    @ObjectFactory
    default RegistroPonto criarRegistroPonto(RegistroPontoJpaEntity e) {
        return new RegistroPonto(
                e.getId(), e.getColaboradorId(), e.getTenantId(), e.getDataHoraDispositivo(),
                e.getDataHoraServidor(), e.getTipoRegistro(), e.getLatitude(), e.getLongitude(),
                e.getPrecisaoGps(), e.getFotoUrl(), e.getSincronizadoOffline(), e.getNsr(),
                e.getNsrLogico(), e.getAjusteManual(), e.getJustificativa(), e.getObservacao(),
                e.getAjusteStatus(), e.getAjusteMotivoRejeicao(), e.getAprovadoPor(), e.getAprovadoEm()
        );
    }

    @AfterMapping
    default void atribuirHash(RegistroPontoJpaEntity entity, @MappingTarget RegistroPonto model) {
        model.atribuirHash(entity.getHashIntegridade());
    }
}
