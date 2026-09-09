package br.com.jess.chronos.pulse.modules.telemetria.web.dto;

import org.springframework.data.domain.Page;

import java.util.List;

public record PaginaEventosDTO<T>(
        List<T> conteudo,
        int pagina,
        int tamanho,
        long totalElementos,
        int totalPaginas
) {
    public static <T> PaginaEventosDTO<T> of(Page<T> page) {
        return new PaginaEventosDTO<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages());
    }
}