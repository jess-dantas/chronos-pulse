package br.com.jess.chronos.pulse.modules.auth.infrastructure.adapters.output.persistence;

import br.com.jess.chronos.pulse.modules.auth.domain.model.CpcUsuario;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ObjectFactory;
import org.mapstruct.AfterMapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CpcUsuarioMapper {

    CpcUsuarioJpaEntity toEntity(CpcUsuario model);

    @Mapping(target = "emailPessoal", ignore = true)
    @Mapping(target = "apelido", ignore = true)
    @Mapping(target = "celular", ignore = true)
    CpcUsuario toModel(CpcUsuarioJpaEntity entity);

    @ObjectFactory
    default CpcUsuario criarCpcUsuario(CpcUsuarioJpaEntity e) {
        return new CpcUsuario(e.getId(), e.getCpcId(), e.getCpf(), e.getNome(),
                e.getEmailCorporativo(), e.getSenhaHash(), e.getRole(), e.getTenantId());
    }

    @AfterMapping
    default void atribuirDadosPessoais(CpcUsuarioJpaEntity entity, @MappingTarget CpcUsuario model) {
        model.atualizarDadosPessoais(entity.getApelido(), entity.getCelular(), entity.getEmailPessoal());
    }
}
