package br.com.jess.chronos.pulse.modules.patrimonio.service;

import br.com.jess.chronos.pulse.modules.patrimonio.domain.entity.Patrimonio;
import br.com.jess.chronos.pulse.modules.patrimonio.domain.entity.TransferenciaPatrimonio;
import br.com.jess.chronos.pulse.modules.patrimonio.repository.PatrimonioRepository;
import br.com.jess.chronos.pulse.modules.patrimonio.repository.TransferenciaPatrimonioRepository;
import br.com.jess.chronos.pulse.modules.patrimonio.web.dto.SolicitarTransferenciaDTO;
import br.com.jess.chronos.pulse.modules.patrimonio.web.dto.TransferenciaResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransferenciaService {

    private final TransferenciaPatrimonioRepository transferenciaRepository;
    private final PatrimonioRepository patrimonioRepository;

    @Transactional
    public TransferenciaResponseDTO solicitar(SolicitarTransferenciaDTO dto, UUID tenantId, String nomeSolicitante) {
        Patrimonio bem = patrimonioRepository.findByIdAndTenantId(dto.patrimonioId(), tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Patrimônio não encontrado"));
        if (!Boolean.TRUE.equals(bem.getAtivo())) {
            throw new IllegalStateException("Bem inativo não pode ser transferido");
        }

        TransferenciaPatrimonio transferencia = TransferenciaPatrimonio.builder()
                .tenantId(tenantId)
                .patrimonioId(bem.getId())
                .localizacaoOrigem(dto.localizacaoOrigem() != null ? dto.localizacaoOrigem() : bem.getLocalizacao())
                .localizacaoDestino(dto.localizacaoDestino())
                .responsavelOrigem(dto.responsavelOrigem() != null ? dto.responsavelOrigem() : bem.getResponsavelNome())
                .responsavelDestino(dto.responsavelDestino())
                .dataPrevista(dto.dataPrevista())
                .status("SOLICITADA")
                .justificativa(dto.justificativa())
                .solicitadoPor(nomeSolicitante)
                .build();

        return toResponseDTO(transferenciaRepository.save(transferencia));
    }

    @Transactional(readOnly = true)
    public List<TransferenciaResponseDTO> listar(UUID tenantId) {
        return transferenciaRepository.findAllByTenantIdOrderByDataSolicitacaoDesc(tenantId).stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Transactional
    public TransferenciaResponseDTO confirmar(UUID id, UUID tenantId, String nomeAprovador) {
        TransferenciaPatrimonio transferencia = transferenciaRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Transferência não encontrada"));
        String status = transferencia.getStatus();
        if (!"SOLICITADA".equals(status) && !"EM_TRANSITO".equals(status)) {
            throw new IllegalStateException("Transferência já concluída ou cancelada");
        }

        transferencia.setStatus("CONFIRMADA");
        transferencia.setDataEfetivacao(OffsetDateTime.now());
        transferencia.setAprovadoPor(nomeAprovador);

        patrimonioRepository.findByIdAndTenantId(transferencia.getPatrimonioId(), tenantId)
                .ifPresent(bem -> {
                    bem.setLocalizacao(transferencia.getLocalizacaoDestino());
                    if (transferencia.getResponsavelDestino() != null) {
                        bem.setResponsavelNome(transferencia.getResponsavelDestino());
                    }
                    patrimonioRepository.save(bem);
                });

        return toResponseDTO(transferenciaRepository.save(transferencia));
    }

    @Transactional
    public TransferenciaResponseDTO cancelar(UUID id, UUID tenantId) {
        TransferenciaPatrimonio transferencia = transferenciaRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Transferência não encontrada"));
        String status = transferencia.getStatus();
        if (!"SOLICITADA".equals(status) && !"EM_TRANSITO".equals(status)) {
            throw new IllegalStateException("Transferência já concluída não pode ser cancelada");
        }
        transferencia.setStatus("CANCELADA");
        return toResponseDTO(transferenciaRepository.save(transferencia));
    }

    private TransferenciaResponseDTO toResponseDTO(TransferenciaPatrimonio t) {
        return new TransferenciaResponseDTO(
                t.getId(), t.getPatrimonioId(), t.getLocalizacaoOrigem(), t.getLocalizacaoDestino(),
                t.getResponsavelOrigem(), t.getResponsavelDestino(), t.getDataSolicitacao(),
                t.getDataPrevista(), t.getDataEfetivacao(), t.getStatus(), t.getJustificativa(),
                t.getAprovadoPor(), t.getSolicitadoPor());
    }
}