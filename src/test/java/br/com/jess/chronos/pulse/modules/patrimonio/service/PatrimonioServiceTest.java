package br.com.jess.chronos.pulse.modules.patrimonio.service;

import br.com.jess.chronos.pulse.modules.patrimonio.domain.entity.Patrimonio;
import br.com.jess.chronos.pulse.modules.patrimonio.repository.PatrimonioRepository;
import br.com.jess.chronos.pulse.modules.patrimonio.web.dto.CadastrarPatrimonioDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PatrimonioServiceTest {

    @Mock
    private PatrimonioRepository patrimonioRepository;

    @Spy
    private DepreciacaoService depreciacaoService;

    @InjectMocks
    private PatrimonioService patrimonioService;

    private final UUID tenantId = UUID.randomUUID();
    private final UUID patrimonioId = UUID.randomUUID();

    private Patrimonio bemDefault() {
        return Patrimonio.builder()
                .id(patrimonioId)
                .tenantId(tenantId)
                .descricao("Notebook Dell")
                .tombamento("TOM-0010")
                .localizacao("Gabinete")
                .valorAquisicao(new BigDecimal("1000.00"))
                .ativo(true)
                .build();
    }

    @Test
    void cadastrarComVidaUtilDeveCalcularDepreciacao() {
        when(patrimonioRepository.save(any(Patrimonio.class))).thenAnswer(
                (Answer<Patrimonio>) inv -> {
                    Patrimonio p = inv.getArgument(0);
                    p.setId(UUID.randomUUID());
                    return p;
                });

        CadastrarPatrimonioDTO dto = new CadastrarPatrimonioDTO(
                "TOM-0011", "Notebook Dell", "INFORMATICA", "BOM", "Gabinete",
                LocalDate.now().minusMonths(5), new BigDecimal("1000.00"),
                "Fulano", "NF 1", "obs", 60, null);

        var response = patrimonioService.cadastrar(dto, tenantId);

        assertThat(response.taxaDepreciacaoMensal()).isEqualByComparingTo("15.00");
        assertThat(response.valorDepreciado()).isEqualByComparingTo("75.00");
        assertThat(response.valorAtual()).isEqualByComparingTo("925.00");
        assertThat(response.dataInicioDepreciacao()).isEqualTo(dto.dataAquisicao());
        verify(patrimonioRepository).save(any(Patrimonio.class));
    }

    @Test
    void cadastrarSemVidaUtilDeveManterDepreciacaoZerada() {
        when(patrimonioRepository.save(any(Patrimonio.class))).thenAnswer(
                (Answer<Patrimonio>) inv -> inv.getArgument(0));

        CadastrarPatrimonioDTO dto = new CadastrarPatrimonioDTO(
                "TOM-0012", "Cadeira", "MOBILIARIO", "NOVO", "Sala 3",
                LocalDate.now(), new BigDecimal("300.00"),
                "Maria", null, null, null, null);

        var response = patrimonioService.cadastrar(dto, tenantId);

        assertThat(response.taxaDepreciacaoMensal()).isEqualByComparingTo("0");
        assertThat(response.valorDepreciado()).isEqualByComparingTo("0");
    }

    @Test
    void atualizarDeveMesclarCampos() {
        when(patrimonioRepository.findByIdAndTenantId(patrimonioId, tenantId))
                .thenReturn(java.util.Optional.of(bemDefault()));
        when(patrimonioRepository.save(any(Patrimonio.class))).thenAnswer(
                (Answer<Patrimonio>) inv -> inv.getArgument(0));

        CadastrarPatrimonioDTO dto = new CadastrarPatrimonioDTO(
                "TOM-0010", null, null, "REGULAR", "Almoxarifado Central",
                null, null, "Novo Responsável", null, null, null, null);

        var response = patrimonioService.atualizar(patrimonioId, dto, tenantId);

        assertThat(response.localizacao()).isEqualTo("Almoxarifado Central");
        assertThat(response.estado()).isEqualTo("REGULAR");
        assertThat(response.responsavelNome()).isEqualTo("Novo Responsável");
        assertThat(response.descricao()).isEqualTo("Notebook Dell");
    }

    @Test
    void desativarDeveMarcarBemInativo() {
        when(patrimonioRepository.findByIdAndTenantId(patrimonioId, tenantId))
                .thenReturn(java.util.Optional.of(bemDefault()));
        when(patrimonioRepository.save(any(Patrimonio.class))).thenAnswer(
                (Answer<Patrimonio>) inv -> inv.getArgument(0));

        var response = patrimonioService.desativar(patrimonioId, tenantId);

        assertThat(response.ativo()).isFalse();
    }

    @Test
    void buscarPorTombamentoDeveRetornarBem() {
        when(patrimonioRepository.findByTombamentoAndTenantId("TOM-0010", tenantId))
                .thenReturn(java.util.Optional.of(bemDefault()));

        var response = patrimonioService.buscarPorTombamento("TOM-0010", tenantId);

        assertThat(response.tombamento()).isEqualTo("TOM-0010");
    }

    @Test
    void buscarPorQrCodeComIdDeveFuncionarComoFallback() {
        when(patrimonioRepository.findByTombamentoAndTenantId(patrimonioId.toString(), tenantId))
                .thenReturn(java.util.Optional.empty());
        when(patrimonioRepository.findByIdAndTenantId(patrimonioId, tenantId))
                .thenReturn(java.util.Optional.of(bemDefault()));

        var response = patrimonioService.buscarPorQrCode(patrimonioId.toString(), tenantId);

        assertThat(response.descricao()).isEqualTo("Notebook Dell");
    }

    @Test
    void buscarPorQrCodeInexistenteDeveLancarErro() {
        when(patrimonioRepository.findByTombamentoAndTenantId(any(), any()))
                .thenReturn(java.util.Optional.empty());

        assertThatThrownBy(() -> patrimonioService.buscarPorQrCode("NAO-EXISTE", tenantId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Patrimônio não encontrado para o código informado");
    }
}