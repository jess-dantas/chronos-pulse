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
            String responsavelSenha
    ) {}

    record Resultado(
            String accessToken,
            String refreshToken,
            String role,
            String cpcId,
            String nome,
            String email,
            String tenantId,
            boolean acessoEstoque
    ) {}

    Resultado executar(Comando comando);
}
