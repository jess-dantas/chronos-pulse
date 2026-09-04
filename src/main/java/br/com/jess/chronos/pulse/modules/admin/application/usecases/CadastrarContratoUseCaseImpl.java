package br.com.jess.chronos.pulse.modules.admin.application.usecases;

import br.com.jess.chronos.pulse.modules.admin.domain.model.Contrato;
import br.com.jess.chronos.pulse.modules.admin.domain.ports.input.CadastrarContratoUseCase;
import br.com.jess.chronos.pulse.modules.admin.domain.ports.output.ContratoRepositoryPort;
import br.com.jess.chronos.pulse.modules.empresa.domain.ports.output.EmpresaRepositoryPort;

public class CadastrarContratoUseCaseImpl implements CadastrarContratoUseCase {

    private final ContratoRepositoryPort contratoRepositoryPort;
    private final EmpresaRepositoryPort empresaRepositoryPort;

    public CadastrarContratoUseCaseImpl(ContratoRepositoryPort contratoRepositoryPort,
                                         EmpresaRepositoryPort empresaRepositoryPort) {
        this.contratoRepositoryPort = contratoRepositoryPort;
        this.empresaRepositoryPort = empresaRepositoryPort;
    }

    @Override
    public Contrato executar(Comando comando) {
        if (comando.tenantId() == null) {
            throw new IllegalArgumentException("Tenant é obrigatório para cadastro de contrato.");
        }
        if (empresaRepositoryPort.buscarPorId(comando.tenantId()).isEmpty()) {
            throw new IllegalArgumentException("Empresa (tenant) não encontrada: " + comando.tenantId());
        }
        if (comando.dataInicio() == null || comando.dataFim() == null) {
            throw new IllegalArgumentException("Data de início e fim são obrigatórias.");
        }
        if (comando.dataFim().isBefore(comando.dataInicio())) {
            throw new IllegalArgumentException("Data de fim não pode ser anterior à data de início.");
        }

        Contrato contrato = new Contrato(
                null,
                comando.tenantId(),
                comando.numero(),
                comando.objeto(),
                comando.dataInicio(),
                comando.dataFim(),
                comando.valorMensal(),
                comando.valorTotal(),
                "ATIVO",
                comando.observacoes()
        );

        return contratoRepositoryPort.salvar(contrato);
    }
}
