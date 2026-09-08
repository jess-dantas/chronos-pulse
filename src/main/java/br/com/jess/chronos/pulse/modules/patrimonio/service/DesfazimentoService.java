package br.com.jess.chronos.pulse.modules.patrimonio.service;

import br.com.jess.chronos.pulse.modules.patrimonio.domain.entity.Desfazimento;
import br.com.jess.chronos.pulse.modules.patrimonio.domain.entity.DesfazimentoComissao;
import br.com.jess.chronos.pulse.modules.patrimonio.domain.entity.Patrimonio;
import br.com.jess.chronos.pulse.modules.patrimonio.repository.DesfazimentoRepository;
import br.com.jess.chronos.pulse.modules.patrimonio.repository.PatrimonioRepository;
import br.com.jess.chronos.pulse.modules.patrimonio.web.dto.CadastrarDesfazimentoDTO;
import br.com.jess.chronos.pulse.modules.patrimonio.web.dto.DesfazimentoResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DesfazimentoService {

    private final DesfazimentoRepository desfazimentoRepository;
    private final PatrimonioRepository patrimonioRepository;

    @Transactional
    public DesfazimentoResponseDTO cadastrar(CadastrarDesfazimentoDTO dto, UUID tenantId) {
        Patrimonio bem = patrimonioRepository.findByIdAndTenantId(dto.patrimonioId(), tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Patrimônio não encontrado"));

        Desfazimento desfazimento = Desfazimento.builder()
                .tenantId(tenantId)
                .patrimonioId(bem.getId())
                .estadoBem(dto.estadoBem().toUpperCase())
                .tipoDesfazimento(dto.tipoDesfazimento().toUpperCase())
                .justificativa(dto.justificativa())
                .responsavelSolicitacao(dto.responsavelSolicitacao())
                .processoNumero(dto.processoNumero())
                .observacoes(dto.observacoes())
                .build();

        List<DesfazimentoComissao> comissao = montarComissao(dto.membrosComissao());
        comissao.forEach(m -> m.setDesfazimento(desfazimento));
        desfazimento.setComissao(comissao);

        return toResponseDTO(desfazimentoRepository.save(desfazimento));
    }

    @Transactional(readOnly = true)
    public Page<DesfazimentoResponseDTO> listar(UUID tenantId, Pageable pageable) {
        return desfazimentoRepository.findAllByTenantId(tenantId, pageable)
                .map(this::toResponseDTO);
    }

    @Transactional
    public DesfazimentoResponseDTO aprovar(UUID id, UUID tenantId, String parecerComissao) {
        Desfazimento d = buscar(id, tenantId);
        if ("APROVADO".equals(d.getStatus())) {
            throw new IllegalStateException("Desfazimento já aprovado.");
        }
        if ("CANCELADO".equals(d.getStatus())) {
            throw new IllegalStateException("Desfazimento cancelado não pode ser aprovado.");
        }
        d.setParecerComissao(parecerComissao);
        d.setStatus("APROVADO");
        d.setAprovado(true);
        d.setDataAprovacao(OffsetDateTime.now());
        d.setDataBaixa(OffsetDateTime.now());
        desfazimentoRepository.save(d);

        patrimonioRepository.findByIdAndTenantId(d.getPatrimonioId(), tenantId)
                .ifPresent(bem -> {
                    bem.setAtivo(false);
                    patrimonioRepository.save(bem);
                });
        return toResponseDTO(d);
    }

    @Transactional
    public DesfazimentoResponseDTO cancelar(UUID id, UUID tenantId) {
        Desfazimento d = buscar(id, tenantId);
        if ("APROVADO".equals(d.getStatus())) {
            throw new IllegalStateException("Desfazimento baixado não pode ser cancelado.");
        }
        d.setStatus("CANCELADO");
        d.setAprovado(false);
        desfazimentoRepository.save(d);
        return toResponseDTO(d);
    }

    private Desfazimento buscar(UUID id, UUID tenantId) {
        return desfazimentoRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Desfazimento não encontrado"));
    }

    private List<DesfazimentoComissao> montarComissao(List<CadastrarDesfazimentoDTO.MembroDTO> membros) {
        if (membros == null || membros.isEmpty()) {
            return List.of();
        }
        return membros.stream().map(m -> DesfazimentoComissao.builder()
                        .membroNome(m.nome())
                        .membroCargo(m.cargo())
                        .membroCpf(m.cpf())
                        .relator(m.relator() != null ? m.relator() : false)
                        .parecer(m.parecer())
                        .build())
                .toList();
    }

    private DesfazimentoResponseDTO toResponseDTO(Desfazimento d) {
        String descricao = null;
        String tombamento = null;
        if (d.getPatrimonioId() != null) {
            var bem = patrimonioRepository.findById(d.getPatrimonioId());
            descricao = bem.map(Patrimonio::getDescricao).orElse(null);
            tombamento = bem.map(Patrimonio::getTombamento).orElse(null);
        }
        List<DesfazimentoResponseDTO.MembroResponse> membros =
                d.getComissao().stream().map(m -> new DesfazimentoResponseDTO.MembroResponse(
                        m.getId(), m.getMembroNome(), m.getMembroCargo(),
                        m.getMembroCpf(), m.getRelator(), m.getParecer()
                )).toList();
        return new DesfazimentoResponseDTO(
                d.getId(), d.getTenantId(), d.getPatrimonioId(), descricao, tombamento,
                d.getEstadoBem(), d.getTipoDesfazimento(), d.getJustificativa(),
                d.getResponsavelSolicitacao(), d.getDataSolicitacao(), d.getParecerComissao(),
                d.getAprovado(), d.getDataAprovacao(), d.getDataBaixa(), d.getStatus(),
                d.getProcessoNumero(), d.getObservacoes(), membros);
    }
}