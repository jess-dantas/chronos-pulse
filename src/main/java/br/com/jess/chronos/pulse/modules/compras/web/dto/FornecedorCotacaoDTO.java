package br.com.jess.chronos.pulse.modules.compras.web.dto;

import java.util.UUID;

public record FornecedorCotacaoDTO(
        UUID fornecedorId,
        String razaoSocial
) {}