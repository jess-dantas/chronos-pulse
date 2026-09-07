package br.com.jess.chronos.pulse.modules.colaborador.infrastructure.adapters.input.rest.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

public record AtualizarColaboradorRequestDTO(
        @NotBlank String nome,
        String emailCorporativo,
        String matricula,
        String cargo,
        String departamento,
        LocalDate dataNascimento,
        LocalDate dataAdmissao,
        LocalDate dataDesligamento,
        Boolean acessoEstoque,
        Boolean acessoPatrimonio,
        Boolean acessoFrota,
        Boolean acessoProtocolo
) {}
