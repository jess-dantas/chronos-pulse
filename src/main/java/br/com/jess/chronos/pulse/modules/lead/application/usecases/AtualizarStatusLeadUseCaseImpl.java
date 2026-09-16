package br.com.jess.chronos.pulse.modules.lead.application.usecases;

import br.com.jess.chronos.pulse.modules.lead.domain.model.LeadEmpresa;
import br.com.jess.chronos.pulse.modules.lead.domain.model.LeadEmpresaStatus;
import br.com.jess.chronos.pulse.modules.lead.domain.ports.input.AtualizarStatusLeadUseCase;
import br.com.jess.chronos.pulse.modules.lead.domain.ports.output.LeadEmpresaRepositoryPort;

import java.util.UUID;

public class AtualizarStatusLeadUseCaseImpl implements AtualizarStatusLeadUseCase {

    private final LeadEmpresaRepositoryPort repositoryPort;

    public AtualizarStatusLeadUseCaseImpl(LeadEmpresaRepositoryPort repositoryPort) {
        this.repositoryPort = repositoryPort;
    }

    @Override
    public LeadEmpresa executar(UUID id, LeadEmpresaStatus status) {
        return repositoryPort.buscarPorId(id)
                .map(lead -> montarComStatus(lead, status))
                .map(repositoryPort::salvar)
                .orElseThrow(() -> new IllegalArgumentException("Lead não encontrado."));
    }

    private LeadEmpresa montarComStatus(LeadEmpresa lead, LeadEmpresaStatus status) {
        return new LeadEmpresa(
                lead.getId(), lead.getCnpj(), lead.getRazaoSocial(), lead.getContatoNome(),
                lead.getContatoEmail(), lead.getContatoTelefone(), lead.getContatoCelular(),
                lead.getEnderecoLogradouro(), lead.getEnderecoNumero(), lead.getEnderecoComplemento(),
                lead.getEnderecoBairro(), lead.getEnderecoCidade(), lead.getEnderecoUf(),
                lead.getEnderecoCep(), lead.getObservacao(), status, lead.getCriadoEm()
        );
    }
}