package br.com.jess.chronos.pulse.modules.auth.domain.ports.input;

import java.util.List;

public interface BuscarPerfilUseCase {

    record Resultado(
            String cpf,
            String nome,
            String email,
            String role,
            String tenantId,
            String cpcId,
            boolean acessoEstoque,
            boolean acessoPatrimonio,
            boolean acessoFrota,
            boolean acessoProtocolo,
            String foto,
            List<String> modulos
    ) {}

    Resultado executar(String cpf);
}
