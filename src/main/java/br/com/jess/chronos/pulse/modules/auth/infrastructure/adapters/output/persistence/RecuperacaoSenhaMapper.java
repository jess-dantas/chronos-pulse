package br.com.jess.chronos.pulse.modules.auth.infrastructure.adapters.output.persistence;

import br.com.jess.chronos.pulse.modules.auth.domain.model.RecuperacaoSenha;
import org.mapstruct.Mapper;
import org.mapstruct.ObjectFactory;

@Mapper(componentModel = "spring")
public interface RecuperacaoSenhaMapper {

    RecuperacaoSenhaJpaEntity toEntity(RecuperacaoSenha model);

    @ObjectFactory
    default RecuperacaoSenha criarRecuperacaoSenha(RecuperacaoSenhaJpaEntity e) {
        return new RecuperacaoSenha(e.getId(), e.getCpf(), e.getCodigoHash(),
                e.getExpiraEm(), e.isUsado(), e.getCriadoEm());
    }

    RecuperacaoSenha toModel(RecuperacaoSenhaJpaEntity entity);
}