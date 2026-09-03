package br.com.jess.chronos.pulse.modules.estoque.service;

import br.com.jess.chronos.pulse.modules.estoque.domain.entity.Almoxarifado;
import br.com.jess.chronos.pulse.modules.estoque.domain.entity.Material;
import br.com.jess.chronos.pulse.modules.estoque.domain.entity.MaterialGrupo;
import br.com.jess.chronos.pulse.modules.estoque.repository.AlmoxarifadoRepository;
import br.com.jess.chronos.pulse.modules.estoque.repository.MaterialGrupoRepository;
import br.com.jess.chronos.pulse.modules.estoque.repository.MaterialRepository;
import br.com.jess.chronos.pulse.modules.estoque.web.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MaterialService {

    private final MaterialRepository materialRepository;
    private final MaterialGrupoRepository grupoRepository;
    private final AlmoxarifadoRepository almoxarifadoRepository;

    // --- Materiais ---

    @Transactional
    public MaterialResponseDTO cadastrarMaterial(CadastrarMaterialDTO dto, UUID tenantId) {
        MaterialGrupo grupo = grupoRepository.findByIdAndTenantId(dto.grupoId(), tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Grupo de material não encontrado"));

        Material material = Material.builder()
                .tenantId(tenantId)
                .grupo(grupo)
                .codigoCatmat(dto.codigoCatmat())
                .descricao(dto.descricao())
                .unidadeMedida(dto.unidadeMedida().toUpperCase())
                .estoqueMinimo(dto.estoqueMinimo())
                .controlaLoteValidade(dto.controlaLoteValidade() != null ? dto.controlaLoteValidade() : false)
                .ativo(true)
                .build();

        Material salvo = materialRepository.save(material);
        return toMaterialResponseDTO(salvo);
    }

    @Transactional(readOnly = true)
    public Page<MaterialResponseDTO> listarMateriais(UUID tenantId, Pageable pageable) {
        return materialRepository.findAllByTenantId(tenantId, pageable)
                .map(this::toMaterialResponseDTO);
    }

    @Transactional(readOnly = true)
    public MaterialResponseDTO buscarMaterialPorId(UUID id, UUID tenantId) {
        return materialRepository.findByIdAndTenantId(id, tenantId)
                .map(this::toMaterialResponseDTO)
                .orElseThrow(() -> new IllegalArgumentException("Material não encontrado"));
    }

    // --- Grupos ---

    @Transactional
    public MaterialGrupoResponseDTO cadastrarGrupo(CadastrarMaterialGrupoDTO dto, UUID tenantId) {
        MaterialGrupo grupo = MaterialGrupo.builder()
                .tenantId(tenantId)
                .codigo(dto.codigo())
                .nome(dto.nome())
                .ativo(true)
                .build();

        MaterialGrupo salvo = grupoRepository.save(grupo);
        return new MaterialGrupoResponseDTO(salvo.getId(), salvo.getCodigo(), salvo.getNome(), salvo.getAtivo());
    }

    @Transactional(readOnly = true)
    public List<MaterialGrupoResponseDTO> listarGrupos(UUID tenantId) {
        return grupoRepository.findAllByTenantIdAndAtivoTrue(tenantId).stream()
                .map(g -> new MaterialGrupoResponseDTO(g.getId(), g.getCodigo(), g.getNome(), g.getAtivo()))
                .toList();
    }

    // --- Almoxarifados ---

    @Transactional
    public AlmoxarifadoResponseDTO cadastrarAlmoxarifado(CadastrarAlmoxarifadoDTO dto, UUID tenantId) {
        Almoxarifado almoxarifado = Almoxarifado.builder()
                .tenantId(tenantId)
                .nome(dto.nome())
                .descricao(dto.descricao())
                .responsavelCpcId(dto.responsavelCpcId())
                .ativo(true)
                .build();

        Almoxarifado salvo = almoxarifadoRepository.save(almoxarifado);
        return new AlmoxarifadoResponseDTO(salvo.getId(), salvo.getNome(), salvo.getDescricao(), salvo.getResponsavelCpcId(), salvo.getAtivo());
    }

    @Transactional(readOnly = true)
    public List<AlmoxarifadoResponseDTO> listarAlmoxarifados(UUID tenantId) {
        return almoxarifadoRepository.findAllByTenantIdAndAtivoTrue(tenantId).stream()
                .map(a -> new AlmoxarifadoResponseDTO(a.getId(), a.getNome(), a.getDescricao(), a.getResponsavelCpcId(), a.getAtivo()))
                .toList();
    }

    private MaterialResponseDTO toMaterialResponseDTO(Material material) {
        return new MaterialResponseDTO(
                material.getId(),
                material.getGrupo().getId(),
                material.getGrupo().getNome(),
                material.getCodigoCatmat(),
                material.getDescricao(),
                material.getUnidadeMedida(),
                material.getEstoqueMinimo(),
                material.getControlaLoteValidade(),
                material.getAtivo()
        );
    }
}
