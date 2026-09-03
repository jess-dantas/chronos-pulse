package br.com.jess.chronos.pulse.modules.estoque.service;

import br.com.jess.chronos.pulse.modules.estoque.domain.entity.Almoxarifado;
import br.com.jess.chronos.pulse.modules.estoque.domain.entity.Material;
import br.com.jess.chronos.pulse.modules.estoque.domain.entity.MaterialGrupo;
import br.com.jess.chronos.pulse.modules.estoque.repository.AlmoxarifadoRepository;
import br.com.jess.chronos.pulse.modules.estoque.repository.MaterialGrupoRepository;
import br.com.jess.chronos.pulse.modules.estoque.repository.MaterialRepository;
import br.com.jess.chronos.pulse.modules.estoque.web.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MaterialServiceTest {

    @Mock
    private MaterialRepository materialRepository;

    @Mock
    private MaterialGrupoRepository grupoRepository;

    @Mock
    private AlmoxarifadoRepository almoxarifadoRepository;

    @InjectMocks
    private MaterialService materialService;

    private UUID tenantId;
    private MaterialGrupo grupo;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        grupo = MaterialGrupo.builder()
                .id(UUID.randomUUID())
                .tenantId(tenantId)
                .codigo("EXP-01")
                .nome("Material de Escritório")
                .ativo(true)
                .build();
    }

    @Test
    @DisplayName("Deve cadastrar material associado a um grupo válido")
    void deveCadastrarMaterialComSucesso() {
        when(grupoRepository.findByIdAndTenantId(grupo.getId(), tenantId))
                .thenReturn(Optional.of(grupo));

        when(materialRepository.save(any(Material.class))).thenAnswer(i -> {
            Material m = i.getArgument(0);
            m.setId(UUID.randomUUID());
            return m;
        });

        CadastrarMaterialDTO dto = new CadastrarMaterialDTO(
                grupo.getId(),
                "CAT-5001",
                "Grampeador 26/6",
                "UN",
                new BigDecimal("5.000"),
                false
        );

        MaterialResponseDTO response = materialService.cadastrarMaterial(dto, tenantId);

        assertNotNull(response.id());
        assertEquals("Grampeador 26/6", response.descricao());
        assertEquals("UN", response.unidadeMedida());
        assertEquals(grupo.getId(), response.grupoId());
    }

    @Test
    @DisplayName("Deve cadastrar grupo de material com sucesso")
    void deveCadastrarGrupoComSucesso() {
        when(grupoRepository.save(any(MaterialGrupo.class))).thenAnswer(i -> {
            MaterialGrupo g = i.getArgument(0);
            g.setId(UUID.randomUUID());
            return g;
        });

        CadastrarMaterialGrupoDTO dto = new CadastrarMaterialGrupoDTO("LIM-01", "Material de Limpeza");
        MaterialGrupoResponseDTO response = materialService.cadastrarGrupo(dto, tenantId);

        assertNotNull(response.id());
        assertEquals("LIM-01", response.codigo());
        assertEquals("Material de Limpeza", response.nome());
    }

    @Test
    @DisplayName("Deve cadastrar almoxarifado com sucesso")
    void deveCadastrarAlmoxarifadoComSucesso() {
        when(almoxarifadoRepository.save(any(Almoxarifado.class))).thenAnswer(i -> {
            Almoxarifado a = i.getArgument(0);
            a.setId(UUID.randomUUID());
            return a;
        });

        CadastrarAlmoxarifadoDTO dto = new CadastrarAlmoxarifadoDTO("Almoxarifado Saúde", "Setorial da Saúde", UUID.randomUUID());
        AlmoxarifadoResponseDTO response = materialService.cadastrarAlmoxarifado(dto, tenantId);

        assertNotNull(response.id());
        assertEquals("Almoxarifado Saúde", response.nome());
    }
}
