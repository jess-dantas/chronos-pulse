package br.com.jess.chronos.pulse.modules.patrimonio.service;

import br.com.jess.chronos.pulse.modules.patrimonio.domain.entity.Patrimonio;
import br.com.jess.chronos.pulse.modules.patrimonio.domain.entity.TransferenciaPatrimonio;
import br.com.jess.chronos.pulse.modules.patrimonio.repository.PatrimonioRepository;
import br.com.jess.chronos.pulse.modules.patrimonio.repository.TransferenciaPatrimonioRepository;
import br.com.jess.chronos.pulse.modules.patrimonio.web.dto.SolicitarTransferenciaDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransferenciaServiceTest {

    @Mock
    private TransferenciaPatrimonioRepository transferenciaRepository;

    @Mock
    private PatrimonioRepository patrimonioRepository;

    @InjectMocks
    private TransferenciaService transferenciaService;

    private final UUID tenantId = UUID.randomUUID();
    private final UUID patrimonioId = UUID.randomUUID();
    private final UUID transferenciaId = UUID.randomUUID();

    private Patrimonio bemAtivo() {
        return Patrimonio.builder()
                .id(patrimonioId)
                .tenantId(tenantId)
                .descricao("Notebook Dell")
                .tombamento("TOM-0010")
                .localizacao("Gabinete do Prefeito")
                .responsavelNome("Fulano")
                .valorAquisicao(new BigDecimal("6850.00"))
                .ativo(true)
                .build();
    }

    private TransferenciaPatrimonio transferencia(String status) {
        return TransferenciaPatrimonio.builder()
                .id(transferenciaId)
                .tenantId(tenantId)
                .patrimonioId(patrimonioId)
                .localizacaoOrigem("Gabinete do Prefeito")
                .localizacaoDestino("Secretaria de Obras")
                .status(status)
                .build();
    }

    @Test
    void solicitarDeveCriarComOrigemDefaultDoBem() {
        when(patrimonioRepository.findByIdAndTenantId(patrimonioId, tenantId))
                .thenReturn(Optional.of(bemAtivo()));
        when(transferenciaRepository.save(any(TransferenciaPatrimonio.class))).thenAnswer(
                (Answer<TransferenciaPatrimonio>) inv -> {
                    TransferenciaPatrimonio t = inv.getArgument(0);
                    t.setId(transferenciaId);
                    return t;
                });

        SolicitarTransferenciaDTO dto = new SolicitarTransferenciaDTO(
                patrimonioId, "Secretaria de Obras", null, null,
                "Engº Carlos", null, "Veículo remanejado");

        var response = transferenciaService.solicitar(dto, tenantId, "Admin Demo");

        assertThat(response.status()).isEqualTo("SOLICITADA");
        assertThat(response.localizacaoOrigem()).isEqualTo("Gabinete do Prefeito");
        assertThat(response.responsavelOrigem()).isEqualTo("Fulano");
        assertThat(response.solicitadoPor()).isEqualTo("Admin Demo");
        verify(transferenciaRepository).save(any(TransferenciaPatrimonio.class));
    }

    @Test
    void solicitarBemInativoDeveLancarErro() {
        Patrimonio inativo = bemAtivo();
        inativo.setAtivo(false);
        when(patrimonioRepository.findByIdAndTenantId(patrimonioId, tenantId)).thenReturn(Optional.of(inativo));

        SolicitarTransferenciaDTO dto = new SolicitarTransferenciaDTO(
                patrimonioId, "Secretaria de Obras", null, null, null, null, null);

        assertThatThrownBy(() -> transferenciaService.solicitar(dto, tenantId, "Admin"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Bem inativo não pode ser transferido");
    }

    @Test
    void confirmarDeveAtualizarLocalizacaoDoBem() {
        when(transferenciaRepository.findByIdAndTenantId(transferenciaId, tenantId))
                .thenReturn(Optional.of(transferencia("SOLICITADA")));
        when(patrimonioRepository.findByIdAndTenantId(patrimonioId, tenantId))
                .thenReturn(Optional.of(bemAtivo()));
        when(transferenciaRepository.save(any(TransferenciaPatrimonio.class))).thenAnswer(
                (Answer<TransferenciaPatrimonio>) inv -> inv.getArgument(0));

        var response = transferenciaService.confirmar(transferenciaId, tenantId, "Admin Demo");

        assertThat(response.status()).isEqualTo("CONFIRMADA");
        assertThat(response.dataEfetivacao()).isNotNull();
        assertThat(response.aprovadoPor()).isEqualTo("Admin Demo");
        verify(patrimonioRepository).save(any(Patrimonio.class));
    }

    @Test
    void confirmarCanceladaDeveLancarErro() {
        when(transferenciaRepository.findByIdAndTenantId(transferenciaId, tenantId))
                .thenReturn(Optional.of(transferencia("CANCELADA")));

        assertThatThrownBy(() -> transferenciaService.confirmar(transferenciaId, tenantId, "Admin"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Transferência já concluída ou cancelada");
    }

    @Test
    void cancelarDeveMarcarComoCancelado() {
        when(transferenciaRepository.findByIdAndTenantId(transferenciaId, tenantId))
                .thenReturn(Optional.of(transferencia("SOLICITADA")));
        when(transferenciaRepository.save(any(TransferenciaPatrimonio.class))).thenAnswer(
                (Answer<TransferenciaPatrimonio>) inv -> inv.getArgument(0));

        var response = transferenciaService.cancelar(transferenciaId, tenantId);

        assertThat(response.status()).isEqualTo("CANCELADA");
    }

    @Test
    void cancelarConfirmadaDeveLancarErro() {
        when(transferenciaRepository.findByIdAndTenantId(transferenciaId, tenantId))
                .thenReturn(Optional.of(transferencia("CONFIRMADA")));

        assertThatThrownBy(() -> transferenciaService.cancelar(transferenciaId, tenantId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Transferência já concluída não pode ser cancelada");
    }
}