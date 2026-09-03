package br.com.jess.chronos.pulse.modules.empresa.application.usecases;

import br.com.jess.chronos.pulse.modules.empresa.domain.model.Empresa;
import br.com.jess.chronos.pulse.modules.empresa.domain.ports.input.CadastrarEmpresaUseCase;
import br.com.jess.chronos.pulse.modules.empresa.domain.ports.output.EmpresaRepositoryPort;

public class CadastrarEmpresaUseCaseImpl implements CadastrarEmpresaUseCase {

    private final EmpresaRepositoryPort repositoryPort;

    public CadastrarEmpresaUseCaseImpl(EmpresaRepositoryPort repositoryPort) {
        this.repositoryPort = repositoryPort;
    }

    @Override
    public Empresa executar(Comando comando) {
        if (repositoryPort.existePorCnpj(comando.cnpj())) {
            throw new IllegalArgumentException("CNPJ já cadastrado: " + comando.cnpj());
        }
        return repositoryPort.salvar(new Empresa(null, comando.cnpj(), comando.nome()));
    }
}
