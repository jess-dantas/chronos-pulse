package br.com.jess.chronos.pulse.modules.empresa.application.usecases;

import br.com.jess.chronos.pulse.modules.empresa.domain.model.Empresa;
import br.com.jess.chronos.pulse.modules.empresa.domain.ports.input.CadastrarEmpresaUseCase;
import br.com.jess.chronos.pulse.modules.empresa.domain.ports.output.EmpresaRepositoryPort;
import br.com.jess.chronos.pulse.modules.modulo.domain.ports.output.ModulosPort;
import br.com.jess.chronos.pulse.shared.util.CnpjValidator;

public class CadastrarEmpresaUseCaseImpl implements CadastrarEmpresaUseCase {

    private final EmpresaRepositoryPort repositoryPort;
    private final ModulosPort modulosPort;

    public CadastrarEmpresaUseCaseImpl(EmpresaRepositoryPort repositoryPort, ModulosPort modulosPort) {
        this.repositoryPort = repositoryPort;
        this.modulosPort = modulosPort;
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
        Empresa empresa = repositoryPort.salvar(new Empresa(
                null, cnpj, comando.nome(),
                comando.responsavelNome(), null, comando.responsavelEmail(),
                comando.responsavelCelular(), comando.responsavelTelefone(),
                comando.enderecoLogradouro(), comando.enderecoNumero(),
                comando.enderecoComplemento(), comando.enderecoBairro(),
                comando.enderecoCidade(), comando.enderecoUf(),
                comando.enderecoCep()));
        modulosPort.ativarModulosPadrao(empresa.getId());
        return empresa;
    }
}