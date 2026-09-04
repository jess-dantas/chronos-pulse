package br.com.jess.chronos.pulse.modules.admin.infrastructure.adapters.output.persistence;

import br.com.jess.chronos.pulse.modules.admin.domain.model.Contrato;
import org.mapstruct.Mapper;
import org.mapstruct.ObjectFactory;

@Mapper(componentModel = "spring")
public interface ContratoMapper {

    ContratoJpaEntity toEntity(Contrato model);

    Contrato toModel(ContratoJpaEntity entity);

    @ObjectFactory
    default Contrato criarContrato(ContratoJpaEntity e) {
        return new Contrato(e.getId(), e.getTenantId(), e.getNumero(), e.getObjeto(),
                e.getDataInicio(), e.getDataFim(), e.getValorMensal(), e.getValorTotal(),
                e.getStatus(), e.getObservacoes());
    }
}
