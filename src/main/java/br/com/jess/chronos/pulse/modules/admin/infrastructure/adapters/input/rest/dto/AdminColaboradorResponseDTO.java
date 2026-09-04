package br.com.jess.chronos.pulse.modules.admin.infrastructure.adapters.input.rest.dto;

import br.com.jess.chronos.pulse.modules.colaborador.domain.ports.input.ListarColaboradoresUseCase;

import java.time.LocalDate;
import java.util.UUID;

public record AdminColaboradorResponseDTO(
        UUID id,
        UUID cpcUsuarioId,
        UUID tenantId,
        String tenantNome,
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
) {
    public static AdminColaboradorResponseDTO fromItem(
            ListarColaboradoresUseCase.ColaboradorItem item,
            String tenantNome) {
        return new AdminColaboradorResponseDTO(
                item.id(),
                item.cpcUsuarioId(),
                item.tenantId(),
                tenantNome,
                item.cpf(),
                item.nome(),
                item.email(),
                item.matricula(),
                item.cargo(),
                item.departamento(),
                item.dataAdmissao(),
                item.dataNascimento(),
                item.acessoEstoque(),
                item.ativo()
        );
    }
}