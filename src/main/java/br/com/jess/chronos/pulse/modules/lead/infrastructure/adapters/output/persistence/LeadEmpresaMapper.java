package br.com.jess.chronos.pulse.modules.lead.infrastructure.adapters.output.persistence;

import br.com.jess.chronos.pulse.modules.lead.domain.model.LeadEmpresa;
import org.mapstruct.Mapper;
import org.mapstruct.ObjectFactory;

@Mapper(componentModel = "spring")
public interface LeadEmpresaMapper {

    LeadEmpresaJpaEntity toEntity(LeadEmpresa model);

    LeadEmpresa toModel(LeadEmpresaJpaEntity entity);

    @ObjectFactory
    default LeadEmpresa criarLead(LeadEmpresaJpaEntity entity) {
        return new LeadEmpresa(
                entity.getId(), entity.getCnpj(), entity.getRazaoSocial(), entity.getContatoNome(),
                entity.getContatoEmail(), entity.getContatoTelefone(), entity.getContatoCelular(),
                entity.getEnderecoLogradouro(), entity.getEnderecoNumero(), entity.getEnderecoComplemento(),
                entity.getEnderecoBairro(), entity.getEnderecoCidade(), entity.getEnderecoUf(),
                entity.getEnderecoCep(), entity.getObservacao(), entity.getStatus(), entity.getCriadoEm()
        );
    }
}