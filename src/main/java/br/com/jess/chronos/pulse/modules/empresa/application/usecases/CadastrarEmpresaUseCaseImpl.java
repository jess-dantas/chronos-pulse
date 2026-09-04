package br.com.jess.chronos.pulse.modules.empresa.application.usecases;

import br.com.jess.chronos.pulse.modules.empresa.domain.model.Empresa;
import br.com.jess.chronos.pulse.modules.empresa.domain.ports.input.CadastrarEmpresaUseCase;
import br.com.jess.chronos.pulse.modules.empresa.domain.ports.output.EmpresaRepositoryPort;
import br.com.jess.chronos.pulse.shared.util.CnpjValidator;

public class CadastrarEmpresaUseCaseImpl implements CadastrarEmpresaUseCase {

    private final EmpresaRepositoryPort repositoryPort;

    public CadastrarEmpresaUseCaseImpl(EmpresaRepositoryPort repositoryPort) {
        this.repositoryPort = repositoryPort;
    }

    @Override
    public Empresa executar(Comando comando) {
        String cnpj = CnpjValidator.normalizar(comando.cnpj());
        if (!CnpjValidator.validar(cnpj)) {
            throw new IllegalArgumentException("CNPJ inválido: " + comando.cnpj());
        }
        if (repositoryPort.existePorCnpj(cnpj)) {
            throw new IllegalArgumentException("CNPJ já cadastrado: " + cnpj);
        }
        return repositoryPort.salvar(new Empresa(null, cnpj, comando.nome()));
    }
}
