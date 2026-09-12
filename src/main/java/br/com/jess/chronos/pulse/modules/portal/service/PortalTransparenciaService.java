package br.com.jess.chronos.pulse.modules.portal.service;

import br.com.jess.chronos.pulse.modules.empresa.domain.model.Empresa;
import br.com.jess.chronos.pulse.modules.empresa.domain.ports.output.EmpresaRepositoryPort;
import br.com.jess.chronos.pulse.modules.licitacoes.domain.entity.ContratoAditivo;
import br.com.jess.chronos.pulse.modules.licitacoes.domain.entity.ContratoLicitacao;
import br.com.jess.chronos.pulse.modules.licitacoes.domain.entity.ContratoSancao;
import br.com.jess.chronos.pulse.modules.licitacoes.domain.entity.Licitacao;
import br.com.jess.chronos.pulse.modules.licitacoes.domain.entity.LicitacaoStatus;
import br.com.jess.chronos.pulse.modules.licitacoes.repository.ContratoAditivoRepository;
import br.com.jess.chronos.pulse.modules.licitacoes.repository.ContratoSancaoRepository;
import br.com.jess.chronos.pulse.modules.licitacoes.repository.LicitacaoContratoRepository;
import br.com.jess.chronos.pulse.modules.licitacoes.repository.LicitacaoRepository;
import br.com.jess.chronos.pulse.modules.modulo.domain.ports.output.ModulosPort;
import br.com.jess.chronos.pulse.modules.portal.domain.exception.PortalIndisponivelException;
import br.com.jess.chronos.pulse.modules.portal.web.dto.PortalAditivoDTO;
import br.com.jess.chronos.pulse.modules.portal.web.dto.PortalContratoDetalheDTO;
import br.com.jess.chronos.pulse.modules.portal.web.dto.PortalContratoListaDTO;
import br.com.jess.chronos.pulse.modules.portal.web.dto.PortalLicitacaoDetalheDTO;
import br.com.jess.chronos.pulse.modules.portal.web.dto.PortalLicitacaoItemDTO;
import br.com.jess.chronos.pulse.modules.portal.web.dto.PortalLicitacaoListaDTO;
import br.com.jess.chronos.pulse.modules.portal.web.dto.PortalOrgaoDTO;
import br.com.jess.chronos.pulse.modules.portal.web.dto.PortalPublicacaoDTO;
import br.com.jess.chronos.pulse.modules.portal.web.dto.PortalResumoDTO;
import br.com.jess.chronos.pulse.modules.portal.web.dto.PortalSancaoDTO;
import br.com.jess.chronos.pulse.modules.transparencia.domain.entity.StatusPublicacaoTransparencia;
import br.com.jess.chronos.pulse.modules.transparencia.web.dto.DespesasMensaisDTO;
import br.com.jess.chronos.pulse.modules.transparencia.web.dto.TransparenciaResumoDTO;
import br.com.jess.chronos.pulse.modules.transparencia.service.TransparenciaService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static br.com.jess.chronos.pulse.modules.licitacoes.domain.entity.LicitacaoStatus.ABERTA;
import static br.com.jess.chronos.pulse.modules.licitacoes.domain.entity.LicitacaoStatus.ADJUDICADA;
import static br.com.jess.chronos.pulse.modules.licitacoes.domain.entity.LicitacaoStatus.HOMOLOGADA;
import static br.com.jess.chronos.pulse.modules.licitacoes.domain.entity.LicitacaoStatus.PUBLICADA;

@Service
public class PortalTransparenciaService {

    private static final Set<LicitacaoStatus> STATUS_PUBLICOS_LICITACAO =
            EnumSet.of(PUBLICADA, ABERTA, ADJUDICADA, HOMOLOGADA);

    private final EmpresaRepositoryPort empresaRepository;
    private final ModulosPort modulosPort;
    private final TransparenciaService transparenciaService;
    private final LicitacaoRepository licitacaoRepository;
    private final LicitacaoContratoRepository contratoRepository;
    private final ContratoAditivoRepository aditivoRepository;
    private final ContratoSancaoRepository sancaoRepository;

    public PortalTransparenciaService(EmpresaRepositoryPort empresaRepository,
                                      ModulosPort modulosPort,
                                      TransparenciaService transparenciaService,
                                      LicitacaoRepository licitacaoRepository,
                                      LicitacaoContratoRepository contratoRepository,
                                      ContratoAditivoRepository aditivoRepository,
                                      ContratoSancaoRepository sancaoRepository) {
        this.empresaRepository = empresaRepository;
        this.modulosPort = modulosPort;
        this.transparenciaService = transparenciaService;
        this.licitacaoRepository = licitacaoRepository;
        this.contratoRepository = contratoRepository;
        this.aditivoRepository = aditivoRepository;
        this.sancaoRepository = sancaoRepository;
    }

    @Transactional(readOnly = true)
    public PortalResumoDTO obterResumo(String slug) {
        Empresa empresa = orgaoPublico(slug);
        TransparenciaResumoDTO resumo = transparenciaService.obterResumo(empresa.getId());
        DespesasMensaisDTO despesas = transparenciaService.obterDespesasMensais(
                empresa.getId(), YearMonth.now().getYear());

        BigDecimal valorDespesasAno = despesas.meses().stream()
                .map(m -> valorOuZero(m.despesasNfe()).add(valorOuZero(m.combustivel())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long licitacoesPublicadas = licitacaoRepository
                .findAllByTenantIdOrderByCriadoEmDesc(empresa.getId()).stream()
                .filter(l -> STATUS_PUBLICOS_LICITACAO.contains(l.getStatus()))
                .filter(l -> l.getStatus() != HOMOLOGADA)
                .count();
        long licitacoesEmAndamento = resumo.licitacoes().abertas() + resumo.licitacoes().adjudicadas();
        long licitacoesHomologadas = resumo.licitacoes().homologadas();

        return new PortalResumoDTO(
                new PortalOrgaoDTO(empresa.getSlug(), empresa.getNome(), empresa.getCnpj()),
                licitacoesPublicadas,
                licitacoesEmAndamento,
                licitacoesHomologadas,
                resumo.contratos().ativos(),
                resumo.contratos().valorEmpenhado(),
                resumo.contratos().valorLiquidado(),
                valorDespesasAno,
                resumo.publicacoes().publicadas(),
                resumo.publicacoes().ultimaCompetencia());
    }

    @Transactional(readOnly = true)
    public List<PortalLicitacaoListaDTO> listarLicitacoes(String slug) {
        Empresa empresa = orgaoPublico(slug);
        return licitacaoRepository.findAllByTenantIdOrderByCriadoEmDesc(empresa.getId()).stream()
                .filter(l -> STATUS_PUBLICOS_LICITACAO.contains(l.getStatus()))
                .map(l -> new PortalLicitacaoListaDTO(
                        l.getId(), l.getNumero(), l.getModalidade().name(), l.getStatus().name(),
                        l.getObjeto(), l.getDataAbertura(), l.getValorEstimado(), l.getPncpPublicadoEm()))
                .toList();
    }

    @Transactional(readOnly = true)
    public PortalLicitacaoDetalheDTO obterLicitacao(String slug, UUID id) {
        Empresa empresa = orgaoPublico(slug);
        Licitacao licitacao = licitacaoRepository.findByIdAndTenantId(id, empresa.getId())
                .orElseThrow(() -> new PortalIndisponivelException("Licitação não encontrada no portal."));
        if (!STATUS_PUBLICOS_LICITACAO.contains(licitacao.getStatus())) {
            throw new PortalIndisponivelException("Licitação não divulgada no portal.");
        }
        List<PortalLicitacaoItemDTO> itens = licitacao.getItens().stream()
                .map(i -> new PortalLicitacaoItemDTO(
                        i.getDescricao(), i.getQuantidade(),
                        i.getValorEstimadoUnitario(), i.getValorEstimadoTotal()))
                .toList();
        return new PortalLicitacaoDetalheDTO(
                licitacao.getId(), licitacao.getNumero(), licitacao.getModalidade().name(),
                licitacao.getTipoJulgamento().name(), licitacao.getStatus().name(),
                licitacao.getObjeto(), licitacao.getDataAbertura(), licitacao.getValorEstimado(),
                licitacao.getObservacoes(), licitacao.getPncpPublicadoEm(), itens);
    }

    @Transactional(readOnly = true)
    public List<PortalContratoListaDTO> listarContratos(String slug) {
        Empresa empresa = orgaoPublico(slug);
        return contratoRepository.findAllByTenantIdOrderByCriadoEmDesc(empresa.getId()).stream()
                .map(this::toContratoLista)
                .toList();
    }

    @Transactional(readOnly = true)
    public PortalContratoDetalheDTO obterContrato(String slug, UUID id) {
        Empresa empresa = orgaoPublico(slug);
        ContratoLicitacao contrato = contratoRepository.findByIdAndTenantId(id, empresa.getId())
                .orElseThrow(() -> new PortalIndisponivelException("Contrato não encontrado no portal."));

        List<PortalAditivoDTO> aditivos = aditivoRepository
                .findAllByContratoIdAndTenantIdOrderByCriadoEmDesc(id, empresa.getId()).stream()
                .map(this::toAditivo)
                .toList();
        List<PortalSancaoDTO> sancoes = sancaoRepository
                .findAllByContratoIdAndTenantIdOrderByCriadoEmDesc(id, empresa.getId()).stream()
                .map(this::toSancao)
                .toList();

        return new PortalContratoDetalheDTO(
                contrato.getId(), contrato.getNumero(), contrato.getObjeto(),
                contrato.getDataInicio(), contrato.getDataFim(), contrato.getStatus(),
                contrato.getValorTotal(), contrato.getValorEmpenhado(), contrato.getValorLiquidado(),
                contrato.getEmpenhoNumero(), contrato.getLicitacaoId(), aditivos, sancoes);
    }

    @Transactional(readOnly = true)
    public DespesasMensaisDTO obterDespesasMensais(String slug, int ano) {
        Empresa empresa = orgaoPublico(slug);
        return transparenciaService.obterDespesasMensais(empresa.getId(), ano);
    }

    @Transactional(readOnly = true)
    public List<PortalPublicacaoDTO> listarPublicacoes(String slug) {
        Empresa empresa = orgaoPublico(slug);
        return transparenciaService.listarPublicacoes(empresa.getId()).stream()
                .filter(p -> p.status() == StatusPublicacaoTransparencia.PUBLICADO)
                .map(p -> new PortalPublicacaoDTO(
                        p.id(), p.competencia(), p.tipoPublicacao().name(),
                        p.valorTotal(), p.itensCount(), p.dataPublicacao(), p.observacoes()))
                .toList();
    }

    private Empresa orgaoPublico(String slug) {
        Empresa empresa = empresaRepository.buscarPorSlug(slug)
                .orElseThrow(() -> new PortalIndisponivelException("Portal não encontrado para a organização."));
        if (!empresa.isAtivo()) {
            throw new PortalIndisponivelException("Portal indisponível para esta organização.");
        }
        if (!modulosPort.isAtivo(empresa.getId(), "TRANSPARENCIA")) {
            throw new PortalIndisponivelException("Portal de transparência não contratado.");
        }
        return empresa;
    }

    private PortalContratoListaDTO toContratoLista(ContratoLicitacao c) {
        return new PortalContratoListaDTO(
                c.getId(), c.getNumero(), c.getObjeto(), c.getDataInicio(), c.getDataFim(),
                c.getStatus(), c.getValorTotal(), c.getValorEmpenhado(), c.getValorLiquidado(),
                c.getLicitacaoId());
    }

    private PortalAditivoDTO toAditivo(ContratoAditivo a) {
        return new PortalAditivoDTO(
                a.getTipo(), a.getDescricao(), a.getJustificativa(),
                a.getPrazoAdicionadoDias(), a.getNovoValorTotal(), a.getAprovado(), a.getCriadoEm());
    }

    private PortalSancaoDTO toSancao(ContratoSancao s) {
        return new PortalSancaoDTO(
                s.getTipo(), s.getDescricao(), s.getBaseLegal(),
                s.getPercentualMulta(), s.getValorMulta(), s.getAplicadaEm());
    }

    private static BigDecimal valorOuZero(BigDecimal valor) {
        return valor != null ? valor : BigDecimal.ZERO;
    }
}