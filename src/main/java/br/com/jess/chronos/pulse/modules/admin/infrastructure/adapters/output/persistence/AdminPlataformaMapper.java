package br.com.jess.chronos.pulse.modules.admin.infrastructure.adapters.output.persistence;

import br.com.jess.chronos.pulse.modules.admin.domain.model.AdminPlataforma;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface AdminPlataformaMapper {

    AdminPlataformaJpaEntity toEntity(AdminPlataforma model);

    @Mapping(target = "criadoEm", ignore = true)
    @Mapping(target = "atualizadoEm", ignore = true)
    AdminPlataforma toModel(AdminPlataformaJpaEntity entity);

    @ObjectFactory
    default AdminPlataforma criarAdminPlataforma(AdminPlataformaJpaEntity entity) {
        return AdminPlataforma.builder()
                .id(entity.getId())
                .username(entity.getUsername())
                .senhaHash(entity.getSenhaHash())
                .nomeCompleto(entity.getNomeCompleto())
                .email(entity.getEmail())
                .ultimoLogin(entity.getUltimoLogin())
                .tentativasLoginFalhas(entity.getTentativasLoginFalhas())
                .bloqueioLoginAte(entity.getBloqueioLoginAte())
                .ativo(entity.isAtivo())
                .twoFactorEnabled(entity.isTwoFactorEnabled())
                .twoFactorSecret(entity.getTwoFactorSecret())
                .criadoEm(entity.getCriadoEm())
                .atualizadoEm(entity.getAtualizadoEm())
                .build();
    }
}