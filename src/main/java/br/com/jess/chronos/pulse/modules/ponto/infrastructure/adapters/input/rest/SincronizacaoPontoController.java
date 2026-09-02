package br.com.jess.chronos.pulse.modules.ponto.infrastructure.adapters.input.rest;

import br.com.jess.chronos.pulse.modules.ponto.domain.model.RegistroPonto;
import br.com.jess.chronos.pulse.modules.ponto.domain.ports.input.RegistrarPontoUseCase;
import br.com.jess.chronos.pulse.modules.ponto.infrastructure.adapters.input.rest.dto.ResultadoSincronizacaoDTO;
import br.com.jess.chronos.pulse.modules.ponto.infrastructure.adapters.input.rest.dto.SincronizacaoLoteDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pontos/sincronizar")
public class SincronizacaoPontoController {

    private final RegistrarPontoUseCase registrarPontoUseCase;

    public SincronizacaoPontoController(RegistrarPontoUseCase registrarPontoUseCase) {
        this.registrarPontoUseCase = registrarPontoUseCase;
    }

    @PostMapping
    public ResponseEntity<ResultadoSincronizacaoDTO> sincronizarLote(
            @RequestBody @Valid SincronizacaoLoteDTO lote,
            @RequestHeader("X-CPF-Colaborador") String cpf) {

        List<UUID> processadosComSucesso = new ArrayList<>();
        List<UUID> falhas = new ArrayList<>();

        for (var dto : lote.registros()) {
            try {
                RegistroPonto pontoDomain = dto.toDomain();
                registrarPontoUseCase.executar(pontoDomain, cpf);
                processadosComSucesso.add(dto.idLocal());
            } catch (Exception e) {
                falhas.add(dto.idLocal());
            }
        }

        return ResponseEntity.ok(new ResultadoSincronizacaoDTO(processadosComSucesso, falhas));
    }
}
