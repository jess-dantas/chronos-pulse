package br.com.jess.chronos.pulse.modules.auth.domain.ports.input;

import java.time.LocalDate;

public interface CadastrarEmpresaCompletoUseCase {

    record Comando(
            String cnpj,
            String nomeEmpresa,
            String responsavelNome,
            String responsavelCpf,
            String responsavelEmail,
            String responsavelCelular,
            String responsavelSenha,
            String responsavelTelefone,
            String enderecoLogradouro,
            String enderecoNumero,
            String enderecoComplemento,
            String enderecoBairro,
            String enderecoCidade,
            String enderecoUf,
            String enderecoCep
    ) {}

    record Resultado(
            String accessToken,
            String refreshToken,
            String role,
            String cpcId,
            String nome,
            String email,
            String tenantId,
            boolean acessoEstoque,
            String foto
    ) {}

    Resultado executar(Comando comando);
}
