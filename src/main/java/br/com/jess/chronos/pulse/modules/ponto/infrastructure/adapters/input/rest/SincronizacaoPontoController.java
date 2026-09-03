package br.com.jess.chronos.pulse.modules.ponto.infrastructure.adapters.input.rest;

import br.com.jess.chronos.pulse.modules.auth.domain.model.CpcUsuario;
import br.com.jess.chronos.pulse.modules.ponto.domain.ports.input.RegistrarPontoUseCase;
import br.com.jess.chronos.pulse.modules.ponto.infrastructure.adapters.input.rest.dto.ResultadoSincronizacaoDTO;
import br.com.jess.chronos.pulse.modules.ponto.infrastructure.adapters.input.rest.dto.SincronizacaoLoteDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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
    @PreAuthorize("hasAnyRole('COLABORADOR', 'ADMIN_EMPRESA', 'GESTOR_RH', 'ADMIN_PLATAFORMA')")
    public ResponseEntity<ResultadoSincronizacaoDTO> sincronizarLote(
            @RequestBody @Valid SincronizacaoLoteDTO lote,
            Authentication authentication) {

        CpcUsuario usuario = (CpcUsuario) authentication.getPrincipal();
        UUID tenantId = usuario.getTenantId();
        String cpf = usuario.getCpf();

        // colaboradorId = cpcId do usuário autenticado (identidade global)
        UUID colaboradorId = usuario.getCpcId();

        List<UUID> processadosComSucesso = new ArrayList<>();
        List<UUID> falhas = new ArrayList<>();

        for (var dto : lote.registros()) {
            try {
                registrarPontoUseCase.executar(dto.toDomain(colaboradorId, tenantId), cpf, tenantId);
                processadosComSucesso.add(dto.idLocal());
            } catch (Exception e) {
                falhas.add(dto.idLocal());
            }
        }

        return ResponseEntity.ok(new ResultadoSincronizacaoDTO(processadosComSucesso, falhas));
    }
}
