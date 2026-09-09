package br.com.jess.chronos.pulse.modules.patrimonio.service;

import br.com.jess.chronos.pulse.modules.patrimonio.domain.entity.Inventario;
import br.com.jess.chronos.pulse.modules.patrimonio.domain.entity.InventarioItem;
import br.com.jess.chronos.pulse.modules.patrimonio.domain.entity.Patrimonio;
import br.com.jess.chronos.pulse.modules.patrimonio.repository.InventarioRepository;
import br.com.jess.chronos.pulse.modules.patrimonio.repository.PatrimonioRepository;
import br.com.jess.chronos.pulse.modules.patrimonio.web.dto.ConferirItemDTO;
import br.com.jess.chronos.pulse.modules.patrimonio.web.dto.CriarInventarioDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventarioServiceTest {

    @Mock
    private InventarioRepository inventarioRepository;

    @Mock
    private PatrimonioRepository patrimonioRepository;

    @InjectMocks
    private InventarioService inventarioService;

    private final UUID tenantId = UUID.randomUUID();
    private final UUID inventarioId = UUID.randomUUID();

    private Patrimonio bem(String tombamento) {
        return Patrimonio.builder()
                .id(UUID.randomUUID())
                .tenantId(tenantId)
                .tombamento(tombamento)
                .descricao("Bem " + tombamento)
                .ativo(true)
                .build();
    }

    private InventarioItem itemNaoConferido(Inventario inv, Patrimonio bem) {
        return InventarioItem.builder()
                .inventario(inv)
                .patrimonioId(bem.getId())
                .patrimonioTombamento(bem.getTombamento())
                .patrimonioDescricao(bem.getDescricao())
                .conferido(false)
                .build();
    }

    private Inventario inventarioComItens(boolean concluido) {
        Inventario inv = Inventario.builder()
                .id(inventarioId)
                .tenantId(tenantId)
                .descricao("Inventário do almoxarifado")
                .dataInicio(LocalDate.now().minusDays(3))
                .status(concluido ? "CONCLUIDO" : "EM_ANDAMENTO")
                .build();
        inv.setItens(new ArrayList<>(List.of(
                itemNaoConferido(inv, bem("TOM-0001")),
                itemNaoConferido(inv, bem("TOM-0002")))));
        return inv;
    }

    @Test
    void criarDevePopularItensComTodosBensAtivos() {
        when(inventarioRepository.save(any(Inventario.class))).thenAnswer(
                (Answer<Inventario>) inv -> inv.getArgument(0));
        when(patrimonioRepository.findAllByTenantIdAndAtivoTrue(tenantId))
                .thenReturn(List.of(bem("TOM-0001"), bem("TOM-0002"), bem("TOM-0003")));

        CriarInventarioDTO dto = new CriarInventarioDTO("Inventário anual", LocalDate.now().minusDays(1), null);

        var response = inventarioService.criar(dto, tenantId, "Admin Demo");

        assertThat(response.status()).isEqualTo("EM_ANDAMENTO");
        assertThat(response.totalItens()).isEqualTo(3);
        assertThat(response.totalConferidos()).isZero();
        assertThat(response.criadoPor()).isEqualTo("Admin Demo");
        verify(inventarioRepository).save(any(Inventario.class));
    }

    @Test
    void criarSemDataInicioDeveUsarHoje() {
        when(inventarioRepository.save(any(Inventario.class))).thenAnswer(
                (Answer<Inventario>) inv -> inv.getArgument(0));
        when(patrimonioRepository.findAllByTenantIdAndAtivoTrue(tenantId)).thenReturn(List.of());

        CriarInventarioDTO dto = new CriarInventarioDTO("Inventário rápido", null, null);

        var response = inventarioService.criar(dto, tenantId, "Admin");

        assertThat(response.dataInicio()).isEqualTo(LocalDate.now());
    }

    @Test
    void conferirItemDeveMarcarResultadoConforme() {
        Inventario inv = inventarioComItens(false);
        when(inventarioRepository.findByIdAndTenantId(inventarioId, tenantId)).thenReturn(Optional.of(inv));
        when(inventarioRepository.save(any(Inventario.class))).thenAnswer(
                (Answer<Inventario>) argument -> argument.getArgument(0));
        UUID patrimonioId = inv.getItens().get(0).getPatrimonioId();

        ConferirItemDTO dto = new ConferirItemDTO(patrimonioId, "conforme", null);

        var response = inventarioService.conferirItem(inventarioId, dto, tenantId, "Conferente");

        assertThat(response.totalConferidos()).isEqualTo(1);
        assertThat(response.totalConformes()).isEqualTo(1);
        assertThat(inv.getItens().get(0).getConferido()).isTrue();
        assertThat(inv.getItens().get(0).getConferidoPor()).isEqualTo("Conferente");
        assertThat(inv.getItens().get(0).getResultado()).isEqualTo("CONFORME");
    }

    @Test
    void conferirItemJaConferidoDeveLancarErro() {
        Inventario inv = inventarioComItens(false);
        InventarioItem primeiro = inv.getItens().get(0);
        primeiro.setConferido(true);
        primeiro.setResultado("CONFORME");
        when(inventarioRepository.findByIdAndTenantId(inventarioId, tenantId)).thenReturn(Optional.of(inv));

        ConferirItemDTO dto = new ConferirItemDTO(primeiro.getPatrimonioId(), "CONFORME", null);

        assertThatThrownBy(() -> inventarioService.conferirItem(inventarioId, dto, tenantId, "Conferente"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Item já conferido nesta sessão");
    }

    @Test
    void conferirItemForaSessaoDeveLancarErro() {
        Inventario inv = inventarioComItens(false);
        when(inventarioRepository.findByIdAndTenantId(inventarioId, tenantId)).thenReturn(Optional.of(inv));

        ConferirItemDTO dto = new ConferirItemDTO(UUID.randomUUID(), "CONFORME", null);

        assertThatThrownBy(() -> inventarioService.conferirItem(inventarioId, dto, tenantId, "Conferente"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Item informado não pertence à sessão de inventário");
    }

    @Test
    void finalizarDeveMarcarNaoConferidosComoDivergencia() {
        Inventario inv = inventarioComItens(false);
        when(inventarioRepository.findByIdAndTenantId(inventarioId, tenantId)).thenReturn(Optional.of(inv));
        when(inventarioRepository.save(any(Inventario.class))).thenAnswer(
                (Answer<Inventario>) argument -> argument.getArgument(0));

        var response = inventarioService.finalizar(inventarioId, tenantId);

        assertThat(response.status()).isEqualTo("CONCLUIDO");
        assertThat(response.totalItens()).isEqualTo(2);
        assertThat(response.totalConferidos()).isEqualTo(2);
        assertThat(response.totalDivergencias()).isEqualTo(2);
        assertThat(response.dataFim()).isEqualTo(LocalDate.now());
        assertThat(inv.getItens().get(0).getResultado()).isEqualTo("DIVERGENCIA");
    }

    @Test
    void finalizarJaConcluidoDeveLancarErro() {
        Inventario inv = inventarioComItens(true);
        when(inventarioRepository.findByIdAndTenantId(inventarioId, tenantId)).thenReturn(Optional.of(inv));

        assertThatThrownBy(() -> inventarioService.finalizar(inventarioId, tenantId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Somente inventários em andamento podem ser concluídos");
    }

    @Test
    void cancelarDeveMarcarComoCancelado() {
        Inventario inv = inventarioComItens(false);
        when(inventarioRepository.findByIdAndTenantId(inventarioId, tenantId)).thenReturn(Optional.of(inv));
        when(inventarioRepository.save(any(Inventario.class))).thenAnswer(
                (Answer<Inventario>) argument -> argument.getArgument(0));

        var response = inventarioService.cancelar(inventarioId, tenantId);

        assertThat(response.status()).isEqualTo("CANCELADO");
    }

    @Test
    void cancelarConcluidoDeveLancarErro() {
        Inventario inv = inventarioComItens(true);
        when(inventarioRepository.findByIdAndTenantId(inventarioId, tenantId)).thenReturn(Optional.of(inv));

        assertThatThrownBy(() -> inventarioService.cancelar(inventarioId, tenantId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Somente inventários em andamento podem ser cancelados");
    }
}