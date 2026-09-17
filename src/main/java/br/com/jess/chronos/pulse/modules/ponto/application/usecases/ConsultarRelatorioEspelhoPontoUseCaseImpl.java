package br.com.jess.chronos.pulse.modules.ponto.application.usecases;

import br.com.jess.chronos.pulse.modules.auth.domain.model.CpcUsuario;
import br.com.jess.chronos.pulse.modules.auth.domain.ports.output.CpcUsuarioRepositoryPort;
import br.com.jess.chronos.pulse.modules.colaborador.domain.model.Colaborador;
import br.com.jess.chronos.pulse.modules.colaborador.domain.ports.output.ColaboradorRepositoryPort;
import br.com.jess.chronos.pulse.modules.empresa.domain.model.ConfiguracaoJornada;
import br.com.jess.chronos.pulse.modules.empresa.domain.model.Empresa;
import br.com.jess.chronos.pulse.modules.empresa.domain.ports.output.ConfiguracaoJornadaRepositoryPort;
import br.com.jess.chronos.pulse.modules.empresa.domain.ports.output.EmpresaRepositoryPort;
import br.com.jess.chronos.pulse.modules.ponto.domain.model.RegistroPonto;
import br.com.jess.chronos.pulse.modules.ponto.domain.ports.input.ConsultarRelatorioEspelhoPontoUseCase;
import br.com.jess.chronos.pulse.modules.ponto.domain.ports.output.RegistroPontoRepositoryPort;
import br.com.jess.chronos.pulse.modules.ponto.domain.service.GeradorCodigoVerificacaoEspelho;

import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

public class ConsultarRelatorioEspelhoPontoUseCaseImpl implements ConsultarRelatorioEspelhoPontoUseCase {

    private final RegistroPontoRepositoryPort registroPontoRepository;
    private final ColaboradorRepositoryPort colaboradorRepository;
    private final CpcUsuarioRepositoryPort usuarioRepository;
    private final EmpresaRepositoryPort empresaRepository;
    private final ConfiguracaoJornadaRepositoryPort jornadaRepository;

    public ConsultarRelatorioEspelhoPontoUseCaseImpl(RegistroPontoRepositoryPort registroPontoRepository,
                                                     ColaboradorRepositoryPort colaboradorRepository,
                                                     CpcUsuarioRepositoryPort usuarioRepository,
                                                     EmpresaRepositoryPort empresaRepository,
                                                     ConfiguracaoJornadaRepositoryPort jornadaRepository) {
        this.registroPontoRepository = registroPontoRepository;
        this.colaboradorRepository = colaboradorRepository;
        this.usuarioRepository = usuarioRepository;
        this.empresaRepository = empresaRepository;
        this.jornadaRepository = jornadaRepository;
    }

    @Override
    public RelatorioEspelho executar(UUID colaboradorId, UUID tenantId, int mes, int ano) {
        if (colaboradorId == null || tenantId == null) {
            throw new IllegalArgumentException("ColaboradorId e TenantId são obrigatórios para o relatório de espelho de ponto.");
        }
        YearMonth ym = YearMonth.of(ano, mes);
        LocalDate dataInicio = ym.atDay(1);
        LocalDate dataFim = ym.atEndOfMonth();
        Instant inicio = dataInicio.atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant fim = dataFim.atTime(23, 59, 59, 999_999_999).toInstant(ZoneOffset.UTC);

        Empresa empresa = empresaRepository.buscarPorId(tenantId).orElse(null);
        Colaborador colaborador = colaboradorRepository.buscarPorCpcUsuarioId(colaboradorId)
                .orElseThrow(() -> new IllegalArgumentException("Colaborador não encontrado para o relatório de espelho de ponto."));
        CpcUsuario usuario = usuarioRepository.buscarPorId(colaboradorId).orElse(null);
        ConfiguracaoJornada jornada = colaborador.getConfiguracaoJornadaId() == null
                ? null
                : jornadaRepository.buscarPorIdETenant(colaborador.getConfiguracaoJornadaId(), tenantId).orElse(null);

        List<RegistroPonto> marcacoes = registroPontoRepository
                .listarPorColaboradorEPeriodo(colaboradorId, tenantId, inicio, fim);

        String cnpj = empresa != null ? empresa.getCnpj() : "";
        String cpf = usuario != null ? usuario.getCpf() : "";

        return new RelatorioEspelho(
                dataInicio,
                dataFim,
                Instant.now(),
                empresa != null ? empresa.getNome() : null,
                cnpj,
                usuario != null ? usuario.getNome() : null,
                cpf,
                colaborador.getDataAdmissao(),
                colaborador.getCargo(),
                colaborador.getMatricula(),
                colaborador.getDepartamento(),
                jornada != null ? jornada.getNome() : null,
                jornada != null ? jornada.getCargaHorariaDiariaMinutos() : null,
                jornada != null ? jornada.getIntervaloMinimoMinutos() : null,
                marcacoes,
                GeradorCodigoVerificacaoEspelho.gerar(cnpj, cpf, inicio, fim, marcacoes)
        );
    }
}