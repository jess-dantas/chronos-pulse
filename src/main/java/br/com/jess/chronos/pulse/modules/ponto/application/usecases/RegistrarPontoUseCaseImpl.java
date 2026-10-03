package br.com.jess.chronos.pulse.modules.ponto.application.usecases;

import br.com.jess.chronos.pulse.modules.ponto.domain.model.RegistroPonto;
import br.com.jess.chronos.pulse.modules.ponto.domain.model.TipoRegistro;
import br.com.jess.chronos.pulse.modules.ponto.domain.ports.input.RegistrarPontoUseCase;
import br.com.jess.chronos.pulse.modules.ponto.domain.ports.output.RegistroPontoRepositoryPort;
import br.com.jess.chronos.pulse.modules.ponto.domain.service.GeradorHashService;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class RegistrarPontoUseCaseImpl implements RegistrarPontoUseCase {

    /**
     * Sequência POSICIONAL da jornada (regra 2026-10-03): 4 batidas
     * principais + 2 batidas de HE. O tipo depende da POSIÇÃO da batida na
     * jornada, não do tipo anterior — por isso a 5ª volta a ser ENTRADA e a
     * 6ª é SAIDA (entrada/saída de hora extra) e qualquer regime (6h, 8h,
     * 12h, 12x36, noturno) serve com as mesmas 6 posições.
     */
    private static final TipoRegistro[] SEQUENCIA = {
        TipoRegistro.ENTRADA, TipoRegistro.INTERVALO, TipoRegistro.RETORNO, TipoRegistro.SAIDA,
        TipoRegistro.ENTRADA, TipoRegistro.SAIDA
    };

    /**
     * Intervalo máximo entre duas batidas da MESMA jornada: um turno que
     * atravessa a virada (22h → 06h, inclusive sem batida na madrugada)
     * continua a mesma jornada, enquanto o abismo entre um dia de trabalho e
     * o próximo (normalmente ≥ 12h) abre uma jornada nova. A batida de
     * 00:00–05:00 não tem regra própria: o próprio intervalo decide.
     */
    private static final Duration LIMITE_JORNADA = Duration.ofHours(10);

    /** A jornada fecha nas 6 batidas; a 7ª reinicia a sequência em ENTRADA. */
    private static final int TAMANHO_JORNADA = 6;

    /**
     * Janela de busca da cadeia: a jornada mais longa possível tem 6
     * intervalos de até 10h (60h) — 72h cobre com margem.
     */
    private static final Duration JANELA_CADEIA = Duration.ofHours(72);

    /**
     * Trilhas de lock em memória por colaborador: serializa a derivação de
     * tipo/nsr quando o sync do heartbeat e o sync por-batida disputam o
     * mesmo colaborador. A cadeia da jornada atravessa dias (até 72h), então
     * o lock por dia deixava duas batidas de madrugada correrem juntas.
     * Assunção: instância única (Render/docker com uma réplica) — o lock
     * protege o read-then-insert sem lock no banco.
     */
    private static final Object[] TRILHAS = new Object[64];

    static {
        for (int i = 0; i < TRILHAS.length; i++) {
            TRILHAS[i] = new Object();
        }
    }

    private final RegistroPontoRepositoryPort repositoryPort;

    public RegistrarPontoUseCaseImpl(RegistroPontoRepositoryPort repositoryPort) {
        this.repositoryPort = repositoryPort;
    }

    @Override
    public RegistroPonto executar(RegistroPonto registro, String cpfColaborador, UUID tenantId) {
        synchronized (trilhaDe(registro)) {
            if (registro.getId() != null) {
                Optional<RegistroPonto> existente = repositoryPort.buscarPorId(registro.getId());
                if (existente.isPresent()) {
                    // Reenvio idempotente: o registro já foi persistido — devolve
                    // como está, sem rederivar tipo nem gerar novo nsr (o
                    // reprocessamento flipava o próprio tipo do dia).
                    return existente.get();
                }
            }

            TipoRegistro proximoTipo = determinarProximoTipo(registro, tenantId);
            registro.atribuirTipo(proximoTipo);

            Long nsrLogico = repositoryPort.obterProximoNsrLogico(registro.getColaboradorId(), tenantId);
            registro.atribuirNsrLogico(nsrLogico);

            Long nsr = repositoryPort.obterProximoNsr();
            registro.atribuirNsr(nsr);

            String hash = GeradorHashService.gerarHashRegistro(registro, cpfColaborador);
            registro.atribuirHash(hash);

            return repositoryPort.salvar(registro);
        }
    }

    private Object trilhaDe(RegistroPonto registro) {
        int indice = Math.floorMod(Objects.hash(registro.getColaboradorId()), TRILHAS.length);
        return TRILHAS[indice];
    }

    /**
     * Deriva o tipo pela posição da batida na jornada em andamento.
     *
     * A cadeia é percorrida de trás para frente a partir da batida nova:
     * enquanto o intervalo com a batida vizinha couber em {@link
     * #LIMITE_JORNADA} (turno atravessando a virada continua) e a jornada
     * não fechar em {@link #TAMANHO_JORNADA} batidas, a posição avança.
     * Fora disso é uma jornada nova — primeira batida, ENTRADA.
     */
    private TipoRegistro determinarProximoTipo(RegistroPonto registro, UUID tenantId) {
        Instant agora = registro.getDataHora();
        Instant inicio = agora.minus(JANELA_CADEIA);

        List<RegistroPonto> anteriores = repositoryPort.listarPorColaboradorEPeriodo(
                registro.getColaboradorId(), tenantId, inicio, agora);

        int posicao = 0;
        Instant anterior = agora;
        for (int i = anteriores.size() - 1; i >= 0 && posicao < TAMANHO_JORNADA; i--) {
            Instant instante = anteriores.get(i).getDataHora();
            if (instante == null) continue;
            if (Duration.between(instante, anterior).compareTo(LIMITE_JORNADA) > 0) {
                break;
            }
            posicao++;
            anterior = instante;
        }
        return SEQUENCIA[posicao % SEQUENCIA.length];
    }
}
