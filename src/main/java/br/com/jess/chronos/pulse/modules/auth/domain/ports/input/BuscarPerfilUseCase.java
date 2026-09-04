package br.com.jess.chronos.pulse.modules.auth.domain.ports.input;

public interface BuscarPerfilUseCase {

    record Resultado(
            String cpf,
            String nome,
            String email,
            String role,
            String tenantId,
            String cpcId,
            boolean acessoEstoque
    ) {}

    Resultado executar(String cpf);
}
