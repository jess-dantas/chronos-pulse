package br.com.jess.chronos.pulse.modules.admin.infrastructure.adapters.output.persistence;

import br.com.jess.chronos.pulse.modules.admin.domain.model.ContratoEvento;
import org.mapstruct.Mapper;
import org.mapstruct.ObjectFactory;

@Mapper(componentModel = "spring")
public interface ContratoEventoMapper {

    ContratoEventoJpaEntity toEntity(ContratoEvento model);

    ContratoEvento toModel(ContratoEventoJpaEntity entity);

    @ObjectFactory
    default ContratoEvento criarEvento(ContratoEventoJpaEntity e) {
        return new ContratoEvento(e.getId(), e.getContratoId(), e.getTipo(),
                e.getDescricao(), e.getCriadoPor());
    }
}
