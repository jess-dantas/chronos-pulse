package br.com.jess.chronos.pulse.modules.estoque.web.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public record CadastrarAlmoxarifadoDTO(
        @NotBlank(message = "Nome é obrigatório")
        String nome,

        String descricao,

        UUID responsavelCpcId
) {}
