package br.com.jess.chronos.pulse.modules.empresa.domain.ports.input;

import br.com.jess.chronos.pulse.modules.empresa.domain.model.Empresa;

public interface CadastrarEmpresaUseCase {
    record Comando(
            String cnpj,
            String nome,
            String responsavelNome,
            String responsavelEmail,
            String responsavelTelefone,
            String responsavelCelular,
            String enderecoLogradouro,
            String enderecoNumero,
            String enderecoComplemento,
            String enderecoBairro,
            String enderecoCidade,
            String enderecoUf,
            String enderecoCep
    ) {}
    Empresa executar(Comando comando);
}