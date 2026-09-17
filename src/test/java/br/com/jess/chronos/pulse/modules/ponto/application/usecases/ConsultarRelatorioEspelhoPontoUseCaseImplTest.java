package br.com.jess.chronos.pulse.modules.ponto.application.usecases;

import br.com.jess.chronos.pulse.modules.auth.domain.model.CpcUsuario;
import br.com.jess.chronos.pulse.modules.auth.domain.model.Role;
import br.com.jess.chronos.pulse.modules.auth.domain.ports.output.CpcUsuarioRepositoryPort;
import br.com.jess.chronos.pulse.modules.colaborador.domain.model.Colaborador;
import br.com.jess.chronos.pulse.modules.colaborador.domain.ports.output.ColaboradorRepositoryPort;
import br.com.jess.chronos.pulse.modules.empresa.domain.model.ConfiguracaoJornada;
import br.com.jess.chronos.pulse.modules.empresa.domain.model.Empresa;
import br.com.jess.chronos.pulse.modules.empresa.domain.ports.output.ConfiguracaoJornadaRepositoryPort;
import br.com.jess.chronos.pulse.modules.empresa.domain.ports.output.EmpresaRepositoryPort;
import br.com.jess.chronos.pulse.modules.ponto.domain.model.RegistroPonto;
import br.com.jess.chronos.pulse.modules.ponto.domain.model.TipoRegistro;
import br.com.jess.chronos.pulse.modules.ponto.domain.ports.output.RegistroPontoRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsultarRelatorioEspelhoPontoUseCaseImplTest {

    @Mock
    private RegistroPontoRepositoryPort registroPontoRepository;
    @Mock
    private ColaboradorRepositoryPort colaboradorRepository;
    @Mock
    private CpcUsuarioRepositoryPort usuarioRepository;
    @Mock
    private EmpresaRepositoryPort empresaRepository;
    @Mock
    private ConfiguracaoJornadaRepositoryPort jornadaRepository;

    private ConsultarRelatorioEspelhoPontoUseCaseImpl useCase;
    private UUID colaboradorId;
    private UUID tenantId;

    @BeforeEach
    void setUp() {
        useCase = new ConsultarRelatorioEspelhoPontoUseCaseImpl(registroPontoRepository,
                colaboradorRepository, usuarioRepository, empresaRepository, jornadaRepository);
        colaboradorId = UUID.randomUUID();
        tenantId = UUID.randomUUID();
    }

    private void mocarDados(Optional<ConfiguracaoJornada> jornada) {
        Empresa empresa = new Empresa(UUID.randomUUID(), "12345678000199", "Empresa Teste LTDA");
        CpcUsuario usuario = new CpcUsuario(UUID.randomUUID(), colaboradorId, "12345678901",
                "João da Silva", "joao@empresa.com", "hash", Role.COLABORADOR, tenantId);
        Colaborador colaborador = new Colaborador(UUID.randomUUID(), colaboradorId, tenantId, "000123",
                "Analista de Sistemas", "TI", LocalDate.of(1990, 1, 1), LocalDate.of(2020, 3, 2),
                jornada.map(ConfiguracaoJornada::getId).orElse(null));

        when(empresaRepository.buscarPorId(tenantId)).thenReturn(Optional.of(empresa));
        when(colaboradorRepository.buscarPorCpcUsuarioId(colaboradorId)).thenReturn(Optional.of(colaborador));
        when(usuarioRepository.buscarPorId(colaboradorId)).thenReturn(Optional.of(usuario));
        if (jornada.isPresent()) {
            when(jornadaRepository.buscarPorIdETenant(jornada.get().getId(), tenantId)).thenReturn(jornada);
        }
    }

    @Test
    void deveMontarRelatorioComEmpregadorTrabalhadorEJornada() {
        mocarDados(Optional.of(new ConfiguracaoJornada(UUID.randomUUID(), tenantId, "Jornada Administrativa 44h",
                440, 5, 10, 660)));
        RegistroPonto r = new RegistroPonto(UUID.randomUUID(), colaboradorId, tenantId,
                Instant.parse("2026-09-01T08:00:00Z"), Instant.parse("2026-09-01T08:00:00Z"),
                TipoRegistro.ENTRADA, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                null, false, 1L);
        when(registroPontoRepository.listarPorColaboradorEPeriodo(eq(colaboradorId), eq(tenantId), any(), any()))
                .thenReturn(List.of(r));

        var relatorio = useCase.executar(colaboradorId, tenantId, 9, 2026);

        assertThat(relatorio.dataInicio()).isEqualTo(LocalDate.of(2026, 9, 1));
        assertThat(relatorio.dataFim()).isEqualTo(LocalDate.of(2026, 9, 30));
        assertThat(relatorio.dataEmissao()).isNotNull();
        assertThat(relatorio.empregadorNome()).isEqualTo("Empresa Teste LTDA");
        assertThat(relatorio.empregadorCnpj()).isEqualTo("12345678000199");
        assertThat(relatorio.trabalhadorNome()).isEqualTo("João da Silva");
        assertThat(relatorio.trabalhadorCpf()).isEqualTo("12345678901");
        assertThat(relatorio.dataAdmissao()).isEqualTo(LocalDate.of(2020, 3, 2));
        assertThat(relatorio.cargo()).isEqualTo("Analista de Sistemas");
        assertThat(relatorio.matricula()).isEqualTo("000123");
        assertThat(relatorio.departamento()).isEqualTo("TI");
        assertThat(relatorio.jornadaNome()).isEqualTo("Jornada Administrativa 44h");
        assertThat(relatorio.cargaHorariaDiariaMinutos()).isEqualTo(440);
        assertThat(relatorio.intervaloMinimoMinutos()).isEqualTo(60);
        assertThat(relatorio.marcacoes()).hasSize(1);
        assertThat(relatorio.codigoVerificacao()).hasSize(64).matches("[a-f0-9]{64}");
    }

    @Test
    void deveLancarQuandoColaboradorNaoExistir() {
        when(colaboradorRepository.buscarPorCpcUsuarioId(colaboradorId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.executar(colaboradorId, tenantId, 9, 2026))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Colaborador não encontrado");
    }

    @Test
    void deveFuncionarSemJornadaQuandoColaboradorNaoTiverConfiguracao() {
        mocarDados(Optional.empty());
        when(registroPontoRepository.listarPorColaboradorEPeriodo(eq(colaboradorId), eq(tenantId), any(), any()))
                .thenReturn(List.of());

        var relatorio = useCase.executar(colaboradorId, tenantId, 9, 2026);

        assertThat(relatorio.jornadaNome()).isNull();
        assertThat(relatorio.cargaHorariaDiariaMinutos()).isNull();
        assertThat(relatorio.marcacoes()).isEmpty();
        assertThat(relatorio.codigoVerificacao()).hasSize(64);
    }
}