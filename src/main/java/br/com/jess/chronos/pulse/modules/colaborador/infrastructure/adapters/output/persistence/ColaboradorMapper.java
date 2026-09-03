package br.com.jess.chronos.pulse.modules.colaborador.infrastructure.adapters.output.persistence;

import br.com.jess.chronos.pulse.modules.colaborador.domain.model.Colaborador;
import org.mapstruct.Mapper;
import org.mapstruct.ObjectFactory;

@Mapper(componentModel = "spring")
public interface ColaboradorMapper {

    ColaboradorJpaEntity toEntity(Colaborador model);

    Colaborador toModel(ColaboradorJpaEntity entity);

    @ObjectFactory
    default Colaborador criarColaborador(ColaboradorJpaEntity e) {
        return new Colaborador(e.getId(), e.getCpcUsuarioId(), e.getTenantId(),
                e.getMatricula(), e.getCargo(), e.getDepartamento(),
                e.getDataNascimento(), e.getDataAdmissao(), e.getConfiguracaoJornadaId());
    }
}
