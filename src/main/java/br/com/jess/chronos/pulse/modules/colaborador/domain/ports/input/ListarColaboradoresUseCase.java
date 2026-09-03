package br.com.jess.chronos.pulse.modules.colaborador.domain.ports.input;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ListarColaboradoresUseCase {
    record ColaboradorItem(
            UUID id,
            UUID cpcUsuarioId,
            UUID tenantId,
            String cpf,
            String nome,
            String email,
            String matricula,
            String cargo,
            String departamento,
            LocalDate dataAdmissao,
            LocalDate dataNascimento,
            boolean acessoEstoque,
            boolean ativo
    ) {}

    List<ColaboradorItem> executar(UUID tenantId);
}
