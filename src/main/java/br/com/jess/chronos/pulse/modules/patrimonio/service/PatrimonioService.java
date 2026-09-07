package br.com.jess.chronos.pulse.modules.patrimonio.service;

import br.com.jess.chronos.pulse.modules.patrimonio.domain.entity.Patrimonio;
import br.com.jess.chronos.pulse.modules.patrimonio.repository.PatrimonioRepository;
import br.com.jess.chronos.pulse.modules.patrimonio.web.dto.CadastrarPatrimonioDTO;
import br.com.jess.chronos.pulse.modules.patrimonio.web.dto.PatrimonioResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PatrimonioService {

    private final PatrimonioRepository patrimonioRepository;

    @Transactional
    public PatrimonioResponseDTO cadastrar(CadastrarPatrimonioDTO dto, UUID tenantId) {
        Patrimonio patrimonio = Patrimonio.builder()
                .tenantId(tenantId)
                .tombamento(dto.tombamento())
                .descricao(dto.descricao())
                .categoria(dto.categoria())
                .estado(dto.estado() != null ? dto.estado().toUpperCase() : "BOM")
                .localizacao(dto.localizacao())
                .dataAquisicao(dto.dataAquisicao())
                .valorAquisicao(dto.valorAquisicao())
                .responsavelNome(dto.responsavelNome())
                .numeroNotaFiscal(dto.numeroNotaFiscal())
                .observacoes(dto.observacoes())
                .ativo(true)
                .build();

        Patrimonio salvo = patrimonioRepository.save(patrimonio);
        return toResponseDTO(salvo);
    }

    @Transactional(readOnly = true)
    public Page<PatrimonioResponseDTO> listar(UUID tenantId, Pageable pageable) {
        return patrimonioRepository.findAllByTenantId(tenantId, pageable)
                .map(this::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public PatrimonioResponseDTO buscarPorId(UUID id, UUID tenantId) {
        return patrimonioRepository.findByIdAndTenantId(id, tenantId)
                .map(this::toResponseDTO)
                .orElseThrow(() -> new IllegalArgumentException("Patrimônio não encontrado"));
    }

    @Transactional(readOnly = true)
    public List<PatrimonioResponseDTO> listarAtivos(UUID tenantId) {
        return patrimonioRepository.findAllByTenantIdAndAtivoTrue(tenantId).stream()
                .map(this::toResponseDTO)
                .toList();
    }

    private PatrimonioResponseDTO toResponseDTO(Patrimonio p) {
        return new PatrimonioResponseDTO(
                p.getId(), p.getTombamento(), p.getDescricao(), p.getCategoria(),
                p.getEstado(), p.getLocalizacao(), p.getDataAquisicao(),
                p.getValorAquisicao(), p.getResponsavelNome(), p.getNumeroNotaFiscal(),
                p.getObservacoes(), p.getAtivo());
    }
}
