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

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PatrimonioService {

    private final PatrimonioRepository patrimonioRepository;
    private final DepreciacaoService depreciacaoService;

    @Transactional
    public PatrimonioResponseDTO cadastrar(CadastrarPatrimonioDTO dto, UUID tenantId) {
        LocalDate inicioDepreciacao = dto.dataInicioDepreciacao() != null ? dto.dataInicioDepreciacao() : dto.dataAquisicao();

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
                .vidaUtilMeses(dto.vidaUtilMeses())
                .taxaDepreciacaoMensal(depreciacaoService.calcularTaxaMensal(dto.valorAquisicao(), dto.vidaUtilMeses()))
                .valorDepreciado(depreciacaoService.calcularDepreciado(dto.valorAquisicao(), dto.vidaUtilMeses(), inicioDepreciacao))
                .dataInicioDepreciacao(inicioDepreciacao)
                .ativo(true)
                .build();

        Patrimonio salvo = patrimonioRepository.save(patrimonio);
        return toResponseDTO(salvo);
    }

    @Transactional
    public PatrimonioResponseDTO atualizar(UUID id, CadastrarPatrimonioDTO dto, UUID tenantId) {
        Patrimonio p = patrimonioRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Patrimônio não encontrado"));

        if (dto.tombamento() != null) p.setTombamento(dto.tombamento());
        if (dto.descricao() != null) p.setDescricao(dto.descricao());
        if (dto.categoria() != null) p.setCategoria(dto.categoria());
        if (dto.estado() != null) p.setEstado(dto.estado().toUpperCase());
        if (dto.localizacao() != null) p.setLocalizacao(dto.localizacao());
        if (dto.dataAquisicao() != null) p.setDataAquisicao(dto.dataAquisicao());
        if (dto.valorAquisicao() != null) p.setValorAquisicao(dto.valorAquisicao());
        if (dto.responsavelNome() != null) p.setResponsavelNome(dto.responsavelNome());
        if (dto.numeroNotaFiscal() != null) p.setNumeroNotaFiscal(dto.numeroNotaFiscal());
        if (dto.observacoes() != null) p.setObservacoes(dto.observacoes());

        if (dto.vidaUtilMeses() != null) {
            p.setVidaUtilMeses(dto.vidaUtilMeses());
            LocalDate inicio = dto.dataInicioDepreciacao() != null ? dto.dataInicioDepreciacao()
                    : p.getDataInicioDepreciacao() != null ? p.getDataInicioDepreciacao() : p.getDataAquisicao();
            p.setDataInicioDepreciacao(inicio);
            p.setTaxaDepreciacaoMensal(depreciacaoService.calcularTaxaMensal(p.getValorAquisicao(), p.getVidaUtilMeses()));
            p.setValorDepreciado(depreciacaoService.calcularDepreciado(p.getValorAquisicao(), p.getVidaUtilMeses(), inicio));
        } else if (dto.dataInicioDepreciacao() != null) {
            p.setDataInicioDepreciacao(dto.dataInicioDepreciacao());
            p.setValorDepreciado(depreciacaoService.calcularDepreciado(p.getValorAquisicao(), p.getVidaUtilMeses(), dto.dataInicioDepreciacao()));
        }

        return toResponseDTO(patrimonioRepository.save(p));
    }

    @Transactional
    public PatrimonioResponseDTO desativar(UUID id, UUID tenantId) {
        Patrimonio p = patrimonioRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Patrimônio não encontrado"));
        p.setAtivo(false);
        return toResponseDTO(patrimonioRepository.save(p));
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

    @Transactional(readOnly = true)
    public PatrimonioResponseDTO buscarPorTombamento(String tombamento, UUID tenantId) {
        return patrimonioRepository.findByTombamentoAndTenantId(tombamento, tenantId)
                .map(this::toResponseDTO)
                .orElseThrow(() -> new IllegalArgumentException("Patrimônio não encontrado para o tombamento informado"));
    }

    @Transactional(readOnly = true)
    public PatrimonioResponseDTO buscarPorQrCode(String codigo, UUID tenantId) {
        Optional<Patrimonio> porTombamento = patrimonioRepository.findByTombamentoAndTenantId(codigo, tenantId);
        if (porTombamento.isPresent()) {
            return toResponseDTO(porTombamento.get());
        }
        try {
            UUID id = UUID.fromString(codigo);
            return patrimonioRepository.findByIdAndTenantId(id, tenantId)
                    .map(this::toResponseDTO)
                    .orElseThrow(() -> new IllegalArgumentException("Patrimônio não encontrado para o código informado"));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Patrimônio não encontrado para o código informado");
        }
    }

    @Transactional(readOnly = true)
    public List<PatrimonioResponseDTO> buscar(String termo, UUID tenantId) {
        String normalizado = termo != null ? termo.toLowerCase() : "";
        return patrimonioRepository.findAllByTenantIdAndAtivoTrue(tenantId).stream()
                .filter(p -> normalizado.isEmpty()
                        || (p.getTombamento() != null && p.getTombamento().toLowerCase().contains(normalizado))
                        || (p.getDescricao() != null && p.getDescricao().toLowerCase().contains(normalizado)))
                .map(this::toResponseDTO)
                .toList();
    }

    private PatrimonioResponseDTO toResponseDTO(Patrimonio p) {
        return new PatrimonioResponseDTO(
                p.getId(), p.getTombamento(), p.getDescricao(), p.getCategoria(),
                p.getEstado(), p.getLocalizacao(), p.getDataAquisicao(),
                p.getValorAquisicao(), p.getResponsavelNome(), p.getNumeroNotaFiscal(),
                p.getObservacoes(), p.getAtivo(),
                p.getVidaUtilMeses(), p.getTaxaDepreciacaoMensal(), p.getValorDepreciado(),
                p.getDataInicioDepreciacao(),
                depreciacaoService.calcularValorAtual(p.getValorAquisicao(), p.getValorDepreciado()));
    }
}