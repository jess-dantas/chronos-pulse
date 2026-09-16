package br.com.jess.chronos.pulse.modules.lead.application.usecases;

import br.com.jess.chronos.pulse.modules.lead.domain.model.LeadEmpresa;
import br.com.jess.chronos.pulse.modules.lead.domain.ports.input.CriarLeadEmpresaUseCase;
import br.com.jess.chronos.pulse.modules.lead.domain.ports.output.LeadEmpresaRepositoryPort;

public class CriarLeadEmpresaUseCaseImpl implements CriarLeadEmpresaUseCase {

    private static final int CNPJ_DIGITOS = 14;

    private final LeadEmpresaRepositoryPort repositoryPort;

    public CriarLeadEmpresaUseCaseImpl(LeadEmpresaRepositoryPort repositoryPort) {
        this.repositoryPort = repositoryPort;
    }

    @Override
    public LeadEmpresa executar(LeadEmpresa lead) {
        String cnpj = somenteDigitos(lead.getCnpj());
        if (cnpj == null || cnpj.length() != CNPJ_DIGITOS) {
            throw new IllegalArgumentException("CNPJ inválido: deve conter 14 dígitos.");
        }
        return repositoryPort.salvar(lead.criarComCnpj(cnpj));
    }

    private static String somenteDigitos(String valor) {
        return valor == null ? null : valor.replaceAll("\\D", "");
    }
}