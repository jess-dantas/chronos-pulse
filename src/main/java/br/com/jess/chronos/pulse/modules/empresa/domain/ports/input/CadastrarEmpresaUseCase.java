package br.com.jess.chronos.pulse.modules.empresa.domain.ports.input;

import br.com.jess.chronos.pulse.modules.empresa.domain.model.Empresa;

public interface CadastrarEmpresaUseCase {
    record Comando(String cnpj, String nome) {}
    Empresa executar(Comando comando);
}
