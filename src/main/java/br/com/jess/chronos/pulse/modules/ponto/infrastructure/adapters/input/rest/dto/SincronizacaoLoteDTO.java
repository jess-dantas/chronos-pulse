package br.com.jess.chronos.pulse.modules.ponto.infrastructure.adapters.input.rest.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.UUID;

public record SincronizacaoLoteDTO(
        /// Identidade do dono do lote (defesa em profundidade): quando enviado,
        /// precisa casar com o colaborador autenticado (sessão ou vínculo de
        /// dispositivo) — senão o lote é rejeitado. Opcional por
        /// compatibilidade com clientes antigos.
        UUID colaboradorId,
        @NotEmpty(message = "O lote de sincronização não pode estar vazio")
        List<@Valid RegistroPontoDTO> registros
) {}
