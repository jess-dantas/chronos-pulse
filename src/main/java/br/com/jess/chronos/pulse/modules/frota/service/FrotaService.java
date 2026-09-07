package br.com.jess.chronos.pulse.modules.frota.service;

import br.com.jess.chronos.pulse.modules.frota.domain.entity.FrotaAbastecimento;
import br.com.jess.chronos.pulse.modules.frota.domain.entity.FrotaVeiculo;
import br.com.jess.chronos.pulse.modules.frota.repository.FrotaAbastecimentoRepository;
import br.com.jess.chronos.pulse.modules.frota.repository.FrotaVeiculoRepository;
import br.com.jess.chronos.pulse.modules.frota.web.dto.AbastecimentoResponseDTO;
import br.com.jess.chronos.pulse.modules.frota.web.dto.CadastrarAbastecimentoDTO;
import br.com.jess.chronos.pulse.modules.frota.web.dto.CadastrarFrotaVeiculoDTO;
import br.com.jess.chronos.pulse.modules.frota.web.dto.FrotaVeiculoResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FrotaService {

    private final FrotaVeiculoRepository veiculoRepository;
    private final FrotaAbastecimentoRepository abastecimentoRepository;

    // --- Veículos ---

    @Transactional
    public FrotaVeiculoResponseDTO cadastrarVeiculo(CadastrarFrotaVeiculoDTO dto, UUID tenantId) {
        FrotaVeiculo veiculo = FrotaVeiculo.builder()
                .tenantId(tenantId)
                .placa(dto.placa().toUpperCase())
                .renavam(dto.renavam())
                .marca(dto.marca())
                .modelo(dto.modelo())
                .anoFabricacao(dto.anoFabricacao())
                .anoModelo(dto.anoModelo())
                .tipo(dto.tipo())
                .combustivel(dto.combustivel())
                .status(dto.status() != null ? dto.status().toUpperCase() : "ATIVO")
                .odometroAtual(dto.odometroAtual())
                .observacoes(dto.observacoes())
                .ativo(true)
                .build();

        FrotaVeiculo salvo = veiculoRepository.save(veiculo);
        return toVeiculoResponseDTO(salvo);
    }

    @Transactional(readOnly = true)
    public Page<FrotaVeiculoResponseDTO> listarVeiculos(UUID tenantId, Pageable pageable) {
        return veiculoRepository.findAllByTenantId(tenantId, pageable)
                .map(this::toVeiculoResponseDTO);
    }

    @Transactional(readOnly = true)
    public FrotaVeiculoResponseDTO buscarVeiculoPorId(UUID id, UUID tenantId) {
        return veiculoRepository.findByIdAndTenantId(id, tenantId)
                .map(this::toVeiculoResponseDTO)
                .orElseThrow(() -> new IllegalArgumentException("Veículo não encontrado"));
    }

    // --- Abastecimentos ---

    @Transactional
    public AbastecimentoResponseDTO registrarAbastecimento(CadastrarAbastecimentoDTO dto, UUID tenantId) {
        FrotaVeiculo veiculo = veiculoRepository.findByIdAndTenantId(dto.veiculoId(), tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Veículo não encontrado"));

        BigDecimal valorTotal = dto.litros().multiply(dto.valorLitro());

        FrotaAbastecimento abastecimento = FrotaAbastecimento.builder()
                .tenantId(tenantId)
                .veiculo(veiculo)
                .dataHora(dto.dataHora() != null ? dto.dataHora() : OffsetDateTime.now())
                .litros(dto.litros())
                .valorLitro(dto.valorLitro())
                .valorTotal(valorTotal)
                .odometroKm(dto.odometroKm())
                .posto(dto.posto())
                .observacoes(dto.observacoes())
                .build();

        if (dto.odometroKm() != null
                && (veiculo.getOdometroAtual() == null || dto.odometroKm().compareTo(veiculo.getOdometroAtual()) > 0)) {
            veiculo.setOdometroAtual(dto.odometroKm());
            veiculoRepository.save(veiculo);
        }

        FrotaAbastecimento salvo = abastecimentoRepository.save(abastecimento);
        return toAbastecimentoResponseDTO(salvo);
    }

    @Transactional(readOnly = true)
    public Page<AbastecimentoResponseDTO> listarAbastecimentos(UUID tenantId, Pageable pageable) {
        return abastecimentoRepository.findAllByTenantId(tenantId, pageable)
                .map(this::toAbastecimentoResponseDTO);
    }

    private FrotaVeiculoResponseDTO toVeiculoResponseDTO(FrotaVeiculo v) {
        return new FrotaVeiculoResponseDTO(
                v.getId(), v.getPlaca(), v.getRenavam(), v.getMarca(), v.getModelo(),
                v.getAnoFabricacao(), v.getAnoModelo(), v.getTipo(), v.getCombustivel(),
                v.getStatus(), v.getOdometroAtual(), v.getObservacoes(), v.getAtivo());
    }

    private AbastecimentoResponseDTO toAbastecimentoResponseDTO(FrotaAbastecimento a) {
        return new AbastecimentoResponseDTO(
                a.getId(), a.getVeiculo().getId(), a.getVeiculo().getPlaca(),
                a.getDataHora(), a.getLitros(), a.getValorLitro(), a.getValorTotal(),
                a.getOdometroKm(), a.getPosto(), a.getObservacoes());
    }
}
