package br.com.jess.chronos.pulse.modules.colaborador.infrastructure.adapters.input.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.UUID;

public record CadastrarColaboradorRequestDTO(
        @NotBlank @Size(min = 11, max = 11) String cpf,
        @NotBlank String nome,
        @NotBlank String emailCorporativo,
        @NotBlank String senha,
        String matricula,
        String cargo,
        String departamento,
        @NotNull LocalDate dataNascimento,
        @NotNull LocalDate dataAdmissao,
        UUID tenantId,
        UUID configuracaoJornadaId,
        Boolean acessoEstoque
) {}
