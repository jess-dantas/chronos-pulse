package br.com.jess.chronos.pulse.modules.patrimonio.service;

import br.com.jess.chronos.pulse.modules.patrimonio.domain.entity.Desfazimento;
import br.com.jess.chronos.pulse.modules.patrimonio.domain.entity.Patrimonio;
import br.com.jess.chronos.pulse.modules.patrimonio.repository.DesfazimentoRepository;
import br.com.jess.chronos.pulse.modules.patrimonio.repository.PatrimonioRepository;
import br.com.jess.chronos.pulse.modules.patrimonio.web.dto.CadastrarDesfazimentoDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DesfazimentoServiceTest {

    @Mock
    private DesfazimentoRepository desfazimentoRepository;

    @Mock
    private PatrimonioRepository patrimonioRepository;

    @InjectMocks
    private DesfazimentoService desfazimentoService;

    private final UUID tenantId = UUID.randomUUID();
    private final UUID patrimonioId = UUID.randomUUID();

    private Patrimonio bemDefault() {
        return Patrimonio.builder()
                .id(patrimonioId)
                .tenantId(tenantId)
                .descricao("Notebook Dell")
                .tombamento("TOM-0010")
                .build();
    }

    @Test
    void cadastrarComComissaoDevePersistirComStatusEmAnalise() {
        when(patrimonioRepository.findByIdAndTenantId(patrimonioId, tenantId))
                .thenReturn(Optional.of(bemDefault()));
        when(patrimonioRepository.findById(patrimonioId)).thenReturn(Optional.of(bemDefault()));
        when(desfazimentoRepository.save(any(Desfazimento.class))).thenAnswer(
                (Answer<Desfazimento>) inv -> {
                    Desfazimento d = inv.getArgument(0);
                    d.setId(UUID.randomUUID());
                    return d;
                });

        CadastrarDesfazimentoDTO dto = new CadastrarDesfazimentoDTO(
                patrimonioId, "ocioso", "doacao", "Bem sem uso",
                "Comissão de Patrimônio", "P-2026/123", "obs",
                List.of(new CadastrarDesfazimentoDTO.MembroDTO("Ana", "Presidente", "12345678901", true, "Favorável")));

        var response = desfazimentoService.cadastrar(dto, tenantId);

        assertThat(response.status()).isEqualTo("EM_ANALISE");
        assertThat(response.patrimonioDescricao()).isEqualTo("Notebook Dell");
        assertThat(response.tombamento()).isEqualTo("TOM-0010");
        assertThat(response.comissao()).hasSize(1);
        assertThat(response.comissao().get(0).nome()).isEqualTo("Ana");
        assertThat(response.estadoBem()).isEqualTo("OCIOSO");
        assertThat(response.tipoDesfazimento()).isEqualTo("DOACAO");
        verify(desfazimentoRepository).save(any(Desfazimento.class));
    }

    @Test
    void cadastrarComBemInexistenteDeveLancarErro() {
        when(patrimonioRepository.findByIdAndTenantId(any(), any())).thenReturn(Optional.empty());

        CadastrarDesfazimentoDTO dto = new CadastrarDesfazimentoDTO(
                patrimonioId, "OCIOSO", "VENDA", "Sem uso", "Responsável", null, null, null);

        assertThatThrownBy(() -> desfazimentoService.cadastrar(dto, tenantId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Patrimônio não encontrado");
    }

    @Test
    void aprovarDeveBaixarBemEAtualizarStatus() {
        Desfazimento d = Desfazimento.builder()
                .id(UUID.randomUUID()).tenantId(tenantId).patrimonioId(patrimonioId)
                .estadoBem("IRRECUPERAVEL").tipoDesfazimento("RECICLAGEM")
                .justificativa("Inutilizável").status("EM_ANALISE").build();
        when(desfazimentoRepository.findByIdAndTenantId(d.getId(), tenantId)).thenReturn(Optional.of(d));
        when(patrimonioRepository.findByIdAndTenantId(patrimonioId, tenantId))
                .thenReturn(Optional.of(bemDefault()));
        when(patrimonioRepository.findById(patrimonioId)).thenReturn(Optional.of(bemDefault()));

        var response = desfazimentoService.aprovar(d.getId(), tenantId, "Parecer favorável");

        assertThat(response.status()).isEqualTo("APROVADO");
        assertThat(response.aprovado()).isTrue();
        assertThat(response.dataAprovacao()).isNotNull();
        assertThat(response.dataBaixa()).isNotNull();
        verify(patrimonioRepository).save(any(Patrimonio.class));
    }

    @Test
    void aprovarJaAprovadoDeveLancarErro() {
        Desfazimento d = Desfazimento.builder()
                .id(UUID.randomUUID()).tenantId(tenantId).patrimonioId(patrimonioId)
                .estadoBem("OCIOSO").tipoDesfazimento("VENDA")
                .justificativa("Sem uso").status("APROVADO").build();
        when(desfazimentoRepository.findByIdAndTenantId(d.getId(), tenantId)).thenReturn(Optional.of(d));

        assertThatThrownBy(() -> desfazimentoService.aprovar(d.getId(), tenantId, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Desfazimento já aprovado.");
    }

    @Test
    void cancelarDeveMarcarComoCancelado() {
        Desfazimento d = Desfazimento.builder()
                .id(UUID.randomUUID()).tenantId(tenantId).patrimonioId(patrimonioId)
                .estadoBem("OCIOSO").tipoDesfazimento("VENDA")
                .justificativa("Sem uso").status("EM_ANALISE").build();
        when(desfazimentoRepository.findByIdAndTenantId(d.getId(), tenantId)).thenReturn(Optional.of(d));
        when(patrimonioRepository.findById(patrimonioId)).thenReturn(Optional.of(bemDefault()));

        var response = desfazimentoService.cancelar(d.getId(), tenantId);

        assertThat(response.status()).isEqualTo("CANCELADO");
        assertThat(response.aprovado()).isFalse();
        verify(patrimonioRepository, never()).save(any(Patrimonio.class));
    }

    @Test
    void cancelarAprovadoDeveLancarErro() {
        Desfazimento d = Desfazimento.builder()
                .id(UUID.randomUUID()).tenantId(tenantId).patrimonioId(patrimonioId)
                .estadoBem("OCIOSO").tipoDesfazimento("VENDA")
                .justificativa("Sem uso").status("APROVADO").build();
        when(desfazimentoRepository.findByIdAndTenantId(d.getId(), tenantId)).thenReturn(Optional.of(d));

        assertThatThrownBy(() -> desfazimentoService.cancelar(d.getId(), tenantId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Desfazimento baixado não pode ser cancelado.");
    }
}