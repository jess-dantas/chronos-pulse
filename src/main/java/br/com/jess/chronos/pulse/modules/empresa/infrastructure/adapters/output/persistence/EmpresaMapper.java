package br.com.jess.chronos.pulse.modules.empresa.infrastructure.adapters.output.persistence;

import br.com.jess.chronos.pulse.modules.empresa.domain.model.Empresa;
import org.mapstruct.Mapper;
import org.mapstruct.ObjectFactory;

@Mapper(componentModel = "spring")
public interface EmpresaMapper {

    EmpresaJpaEntity toEntity(Empresa model);

    Empresa toModel(EmpresaJpaEntity entity);

    @ObjectFactory
    default Empresa criarEmpresa(EmpresaJpaEntity e) {
        return new Empresa(e.getId(), e.getCnpj(), e.getNome(),
                e.getResponsavelNome(), e.getResponsavelCpf(),
                e.getResponsavelEmail(), e.getResponsavelCelular());
    }
}
