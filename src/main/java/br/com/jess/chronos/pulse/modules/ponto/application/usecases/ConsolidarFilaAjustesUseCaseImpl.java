package br.com.jess.chronos.pulse.modules.ponto.application.usecases;

import br.com.jess.chronos.pulse.modules.auth.domain.model.CpcUsuario;
import br.com.jess.chronos.pulse.modules.auth.domain.ports.output.CpcUsuarioRepositoryPort;
import br.com.jess.chronos.pulse.modules.ponto.domain.model.FilaAjusteItem;
import br.com.jess.chronos.pulse.modules.ponto.domain.model.FilaAjusteItem.MarcacaoDoDia;
import br.com.jess.chronos.pulse.modules.ponto.domain.model.RegistroPonto;
import br.com.jess.chronos.pulse.modules.ponto.domain.ports.input.ConsolidarFilaAjustesUseCase;
import br.com.jess.chronos.pulse.modules.ponto.domain.ports.output.RegistroPontoRepositoryPort;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.List;

public class ConsolidarFilaAjustesUseCaseImpl implements ConsolidarFilaAjustesUseCase {

    private static final ZoneId FUSO_PONTO = ZoneId.of("America/Sao_Paulo");

    private final RegistroPontoRepositoryPort repositoryPort;
    private final CpcUsuarioRepositoryPort usuarioRepository;

    public ConsolidarFilaAjustesUseCaseImpl(RegistroPontoRepositoryPort repositoryPort,
                                            CpcUsuarioRepositoryPort usuarioRepository) {
        this.repositoryPort = repositoryPort;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public List<FilaAjusteItem> executar(Comando comando) {
        return repositoryPort.listarAjustesPendentesPorTenant(comando.tenantId()).stream()
                .sorted(Comparator.comparing(RegistroPonto::getDataHoraDispositivo))
                .map(ajuste -> new FilaAjusteItem(
                        ajuste,
                        nomeDoColaborador(ajuste),
                        marcacoesDoDia(ajuste)))
                .toList();
    }

    private String nomeDoColaborador(RegistroPonto ajuste) {
        return usuarioRepository.buscarPorId(ajuste.getColaboradorId())
                .map(CpcUsuario::getNome)
                .orElse(null);
    }

    private List<MarcacaoDoDia> marcacoesDoDia(RegistroPonto ajuste) {
        ZonedDateTime dia = ajuste.getDataHoraDispositivo().atZone(FUSO_PONTO);
        var inicio = dia.toLocalDate().atStartOfDay(FUSO_PONTO).toInstant();
        var fim = dia.toLocalDate().plusDays(1).atStartOfDay(FUSO_PONTO).toInstant();

        return repositoryPort.listarPorColaboradorEPeriodo(
                        ajuste.getColaboradorId(), ajuste.getTenantId(), inicio, fim).stream()
                .sorted(Comparator.comparing(RegistroPonto::getDataHoraDispositivo))
                .map(r -> new MarcacaoDoDia(
                        r.getDataHoraDispositivo(),
                        r.getTipoRegistro(),
                        Boolean.TRUE.equals(r.getAjusteManual())))
                .toList();
    }
}
