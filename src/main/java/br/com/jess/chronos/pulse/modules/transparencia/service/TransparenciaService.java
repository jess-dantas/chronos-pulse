package br.com.jess.chronos.pulse.modules.transparencia.service;

import br.com.jess.chronos.pulse.modules.admin.domain.model.Contrato;
import br.com.jess.chronos.pulse.modules.admin.domain.ports.output.ContratoRepositoryPort;
import br.com.jess.chronos.pulse.modules.colaborador.infrastructure.adapters.output.persistence.ColaboradorJpaEntity;
import br.com.jess.chronos.pulse.modules.colaborador.infrastructure.adapters.output.persistence.ColaboradorJpaRepository;
import br.com.jess.chronos.pulse.modules.compras.domain.entity.EntradaNfe;
import br.com.jess.chronos.pulse.modules.compras.domain.entity.Fornecedor;
import br.com.jess.chronos.pulse.modules.compras.domain.entity.PedidoCompra;
import br.com.jess.chronos.pulse.modules.compras.repository.EntradaNfeRepository;
import br.com.jess.chronos.pulse.modules.compras.repository.FornecedorRepository;
import br.com.jess.chronos.pulse.modules.compras.repository.PedidoCompraRepository;
import br.com.jess.chronos.pulse.modules.estoque.domain.entity.EstoqueSaldo;
import br.com.jess.chronos.pulse.modules.estoque.domain.entity.Material;
import br.com.jess.chronos.pulse.modules.estoque.repository.EstoqueSaldoRepository;
import br.com.jess.chronos.pulse.modules.frota.domain.entity.FrotaAbastecimento;
import br.com.jess.chronos.pulse.modules.frota.domain.entity.FrotaVeiculo;
import br.com.jess.chronos.pulse.modules.frota.repository.FrotaAbastecimentoRepository;
import br.com.jess.chronos.pulse.modules.frota.repository.FrotaVeiculoRepository;
import br.com.jess.chronos.pulse.modules.licitacoes.domain.entity.Licitacao;
import br.com.jess.chronos.pulse.modules.licitacoes.domain.entity.LicitacaoStatus;
import br.com.jess.chronos.pulse.modules.licitacoes.repository.LicitacaoRepository;
import br.com.jess.chronos.pulse.modules.patrimonio.domain.entity.Patrimonio;
import br.com.jess.chronos.pulse.modules.patrimonio.repository.PatrimonioRepository;
import br.com.jess.chronos.pulse.modules.ponto.infrastructure.adapters.output.persistence.RegistroPontoJpaRepository;
import br.com.jess.chronos.pulse.modules.transparencia.domain.entity.StatusPublicacaoTransparencia;
import br.com.jess.chronos.pulse.modules.transparencia.domain.entity.TransparenciaPublicacao;
import br.com.jess.chronos.pulse.modules.transparencia.repository.TransparenciaPublicacaoRepository;
import br.com.jess.chronos.pulse.modules.transparencia.web.dto.CriarPublicacaoDTO;
import br.com.jess.chronos.pulse.modules.transparencia.web.dto.DespesasMensaisDTO;
import br.com.jess.chronos.pulse.modules.transparencia.web.dto.PublicacaoResponseDTO;
import br.com.jess.chronos.pulse.modules.transparencia.web.dto.TransparenciaResumoDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransparenciaService {

    private final TransparenciaPublicacaoRepository publicacaoRepository;
    private final ContratoRepositoryPort contratoRepositoryPort;
    private final PedidoCompraRepository pedidoRepository;
    private final EntradaNfeRepository entradaNfeRepository;
    private final FornecedorRepository fornecedorRepository;
    private final LicitacaoRepository licitacaoRepository;
    private final EstoqueSaldoRepository estoqueSaldoRepository;
    private final PatrimonioRepository patrimonioRepository;
    private final FrotaVeiculoRepository frotaVeiculoRepository;
    private final FrotaAbastecimentoRepository abastecimentoRepository;
    private final ColaboradorJpaRepository colaboradorRepository;
    private final RegistroPontoJpaRepository registroPontoRepository;

    public TransparenciaResumoDTO obterResumo(UUID tenantId) {
        TransparenciaResumoDTO.ContratosResumoDTO contratos = resumoContratos(tenantId);
        TransparenciaResumoDTO.ComprasResumoDTO compras = resumoCompras(tenantId);
        TransparenciaResumoDTO.LicitacoesResumoDTO licitacoes = resumoLicitacoes(tenantId);
        TransparenciaResumoDTO.EstoqueResumoDTO estoque = resumoEstoque(tenantId);
        TransparenciaResumoDTO.PatrimonioResumoDTO patrimonio = resumoPatrimonio(tenantId);
        TransparenciaResumoDTO.FrotaResumoDTO frota = resumoFrota(tenantId);
        TransparenciaResumoDTO.ColaboradoresResumoDTO colaboradores = resumoColaboradores(tenantId);
        TransparenciaResumoDTO.PontoResumoDTO ponto = resumoPonto(tenantId);
        TransparenciaResumoDTO.PublicacoesResumoDTO publicacoes = resumoPublicacoes(tenantId);

        return new TransparenciaResumoDTO(contratos, compras, licitacoes, estoque,
                patrimonio, frota, colaboradores, ponto, publicacoes);
    }

    public DespesasMensaisDTO obterDespesasMensais(UUID tenantId, int ano) {
        List<EntradaNfe> nfes = entradaNfeRepository.findAllByTenantIdOrderByCriadoEmDesc(tenantId);
        List<FrotaAbastecimento> abastecimentos =
                abastecimentoRepository.findAllByTenantIdOrderByDataHoraDesc(tenantId);
        List<PedidoCompra> pedidos = pedidoRepository.findAllByTenantIdOrderByDataEmissaoDesc(tenantId);

        List<DespesasMensaisDTO.DespesaMensalDTO> meses = new ArrayList<>();
        for (int mes = 1; mes <= 12; mes++) {
            int finalMes = mes;
            List<EntradaNfe> nfesMes = nfes.stream()
                    .filter(n -> n.getDataEmissao() != null
                            && n.getDataEmissao().getYear() == ano
                            && n.getDataEmissao().getMonthValue() == finalMes)
                    .toList();
            List<FrotaAbastecimento> abastMes = abastecimentos.stream()
                    .filter(a -> a.getDataHora() != null
                            && a.getDataHora().getYear() == ano
                            && a.getDataHora().getMonthValue() == finalMes)
                    .toList();
            List<PedidoCompra> pedidosMes = pedidos.stream()
                    .filter(p -> p.getDataEmissao() != null
                            && p.getDataEmissao().getYear() == ano
                            && p.getDataEmissao().getMonthValue() == finalMes)
                    .toList();

            meses.add(new DespesasMensaisDTO.DespesaMensalDTO(
                    finalMes,
                    somarValorNota(nfesMes),
                    nfesMes.size(),
                    somarAbastecimentos(abastMes),
                    abastMes.size(),
                    somarPedidos(pedidosMes),
                    pedidosMes.size()
            ));
        }
        return new DespesasMensaisDTO(ano, meses);
    }

    public List<PublicacaoResponseDTO> listarPublicacoes(UUID tenantId) {
        return publicacaoRepository.findAllByTenantIdOrderByCompetenciaDesc(tenantId).stream()
                .map(TransparenciaService::toDTO)
                .toList();
    }

    @Transactional
    public PublicacaoResponseDTO criarPublicacao(CriarPublicacaoDTO dto, UUID tenantId) {
        boolean jaExiste = publicacaoRepository
                .findAllByTenantIdOrderByCompetenciaDesc(tenantId).stream()
                .anyMatch(p -> p.getCompetencia().equals(dto.competencia())
                        && p.getTipoPublicacao() == dto.tipoPublicacao());
        if (jaExiste) {
            throw new IllegalArgumentException("Já existe publicação para a competência "
                    + dto.competencia() + " do tipo " + dto.tipoPublicacao());
        }

        TransparenciaPublicacao publicacao = TransparenciaPublicacao.builder()
                .tenantId(tenantId)
                .competencia(dto.competencia())
                .tipoPublicacao(dto.tipoPublicacao())
                .valorTotal(dto.valorTotal() != null ? dto.valorTotal() : BigDecimal.ZERO)
                .itensCount(dto.itensCount() != null ? dto.itensCount() : 0)
                .observacoes(dto.observacoes())
                .build();
        return toDTO(publicacaoRepository.save(publicacao));
    }

    @Transactional
    public PublicacaoResponseDTO publicar(UUID id, UUID tenantId) {
        TransparenciaPublicacao publicacao = buscar(id, tenantId);
        if (publicacao.getStatus() == StatusPublicacaoTransparencia.PUBLICADO) {
            throw new IllegalArgumentException("Publicação já está publicada");
        }
        publicacao.setStatus(StatusPublicacaoTransparencia.PUBLICADO);
        publicacao.setDataPublicacao(LocalDate.now());
        return toDTO(publicacaoRepository.save(publicacao));
    }

    @Transactional
    public void remover(UUID id, UUID tenantId) {
        TransparenciaPublicacao publicacao = buscar(id, tenantId);
        if (publicacao.getStatus() == StatusPublicacaoTransparencia.PUBLICADO) {
            throw new IllegalArgumentException(
                    "Publicação já divulgada não pode ser removida — registre novo lançamento corretivo");
        }
        publicacaoRepository.delete(publicacao);
    }

    public TransparenciaPublicacao buscar(UUID id, UUID tenantId) {
        return publicacaoRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Publicação não encontrada"));
    }

    // ---- agregações BI ----

    private TransparenciaResumoDTO.ContratosResumoDTO resumoContratos(UUID tenantId) {
        List<Contrato> contratos = contratoRepositoryPort.listarPorTenant(tenantId);
        long ativos = contratos.stream().filter(c -> "ATIVO".equals(c.getStatus())).count();
        BigDecimal empenhado = BigDecimal.ZERO;
        BigDecimal liquidado = BigDecimal.ZERO;
        BigDecimal saldo = BigDecimal.ZERO;
        for (Contrato c : contratos) {
            if ("ATIVO".equals(c.getStatus())) {
                empenhado = empenhado.add(valorOuZero(c.getValorEmpenhado()));
                liquidado = liquidado.add(valorOuZero(c.getValorLiquidado()));
                saldo = saldo.add(valorOuZero(c.getSaldo()));
            }
        }
        long vencendo30 = 0;
        long vencendo60 = 0;
        long vencendo90 = 0;
        long vencidos = 0;
        for (Contrato c : contratos) {
            long dias = c.getDiasParaVencimento();
            if (dias < 0) {
                vencidos++;
            } else if (dias <= 30) {
                vencendo30++;
            } else if (dias <= 60) {
                vencendo60++;
            } else if (dias <= 90) {
                vencendo90++;
            }
        }
        return new TransparenciaResumoDTO.ContratosResumoDTO(ativos, empenhado,
                liquidado, saldo, vencendo30, vencendo60, vencendo90, vencidos);
    }

    private TransparenciaResumoDTO.ComprasResumoDTO resumoCompras(UUID tenantId) {
        List<Fornecedor> fornecedores = fornecedorRepository.findAllByTenantIdOrderByRazaoSocial(tenantId);
        long fornecedoresAtivos = fornecedores.stream()
                .filter(f -> Boolean.TRUE.equals(f.getAtivo())).count();
        List<PedidoCompra> pedidos = pedidoRepository.findAllByTenantIdOrderByDataEmissaoDesc(tenantId);
        List<EntradaNfe> nfes = entradaNfeRepository.findAllByTenantIdOrderByCriadoEmDesc(tenantId);
        return new TransparenciaResumoDTO.ComprasResumoDTO(
                fornecedoresAtivos,
                pedidos.size(),
                somarPedidos(pedidos),
                nfes.size(),
                somarValorNota(nfes));
    }

    private TransparenciaResumoDTO.LicitacoesResumoDTO resumoLicitacoes(UUID tenantId) {
        List<Licitacao> licitacoes = licitacaoRepository.findAllByTenantIdOrderByCriadoEmDesc(tenantId);
        long total = licitacoes.size();
        long emElaboracao = contarPorStatus(licitacoes, LicitacaoStatus.EM_ELABORACAO);
        long publicadas = contarPorStatus(licitacoes, LicitacaoStatus.PUBLICADA);
        long abertas = contarPorStatus(licitacoes, LicitacaoStatus.ABERTA);
        long adjudicadas = contarPorStatus(licitacoes, LicitacaoStatus.ADJUDICADA);
        long homologadas = contarPorStatus(licitacoes, LicitacaoStatus.HOMOLOGADA);
        long canceladas = contarPorStatus(licitacoes, LicitacaoStatus.CANCELADA);
        BigDecimal valorEstimado = licitacoes.stream()
                .map(l -> valorOuZero(l.getValorEstimado()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new TransparenciaResumoDTO.LicitacoesResumoDTO(total, emElaboracao,
                publicadas, abertas, adjudicadas, homologadas, canceladas, valorEstimado);
    }

    private TransparenciaResumoDTO.EstoqueResumoDTO resumoEstoque(UUID tenantId) {
        List<EstoqueSaldo> saldos = estoqueSaldoRepository.findAllByTenantId(tenantId);
        BigDecimal valorTotal = BigDecimal.ZERO;
        long acima = 0;
        long abaixo = 0;
        for (EstoqueSaldo s : saldos) {
            BigDecimal qtd = valorOuZero(s.getQuantidadeAtual());
            BigDecimal custo = valorOuZero(s.getCustoMedioUnitario());
            valorTotal = valorTotal.add(qtd.multiply(custo));
            Material material = s.getMaterial();
            BigDecimal minimo = material != null && material.getEstoqueMinimo() != null
                    ? material.getEstoqueMinimo() : BigDecimal.ZERO;
            if (qtd.compareTo(minimo) > 0) {
                acima++;
            } else {
                abaixo++;
            }
        }
        return new TransparenciaResumoDTO.EstoqueResumoDTO(
                saldos.size(), valorTotal, acima, abaixo);
    }

    private TransparenciaResumoDTO.PatrimonioResumoDTO resumoPatrimonio(UUID tenantId) {
        List<Patrimonio> bens = patrimonioRepository.findAllByTenantId(tenantId);
        long ativos = bens.stream().filter(b -> !Boolean.FALSE.equals(b.getAtivo())).count();
        BigDecimal valorAquisicao = bens.stream()
                .map(b -> valorOuZero(b.getValorAquisicao()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal depreciado = bens.stream()
                .map(b -> valorOuZero(b.getValorDepreciado()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new TransparenciaResumoDTO.PatrimonioResumoDTO(
                bens.size(), ativos, valorAquisicao, valorAquisicao.subtract(depreciado));
    }

    private TransparenciaResumoDTO.FrotaResumoDTO resumoFrota(UUID tenantId) {
        List<FrotaVeiculo> veiculos = frotaVeiculoRepository.findAllByTenantId(tenantId);
        long ativos = veiculos.stream().filter(v -> !Boolean.FALSE.equals(v.getAtivo())).count();
        YearMonth mesAtual = YearMonth.now();
        List<FrotaAbastecimento> abastecimentos =
                abastecimentoRepository.findAllByTenantIdOrderByDataHoraDesc(tenantId).stream()
                        .filter(a -> a.getDataHora() != null
                                && a.getDataHora().getYear() == mesAtual.getYear()
                                && a.getDataHora().getMonthValue() == mesAtual.getMonthValue())
                        .toList();
        return new TransparenciaResumoDTO.FrotaResumoDTO(
                veiculos.size(), ativos, abastecimentos.size(), somarAbastecimentos(abastecimentos));
    }

    private TransparenciaResumoDTO.ColaboradoresResumoDTO resumoColaboradores(UUID tenantId) {
        List<ColaboradorJpaEntity> colaboradores = colaboradorRepository.findByTenantId(tenantId);
        long ativos = colaboradores.stream().filter(ColaboradorJpaEntity::isAtivo).count();
        return new TransparenciaResumoDTO.ColaboradoresResumoDTO(colaboradores.size(), ativos);
    }

    private TransparenciaResumoDTO.PontoResumoDTO resumoPonto(UUID tenantId) {
        YearMonth mesAtual = YearMonth.now();
        long registrosMes = registroPontoRepository
                .findByTenantIdOrderByDataHoraDispositivoAsc(tenantId).stream()
                .filter(r -> r.getDataHoraDispositivo() != null)
                .map(r -> r.getDataHoraDispositivo().atZone(java.time.ZoneOffset.UTC).toLocalDate())
                .filter(d -> d.getYear() == mesAtual.getYear()
                        && d.getMonthValue() == mesAtual.getMonthValue())
                .count();
        return new TransparenciaResumoDTO.PontoResumoDTO(registrosMes);
    }

    private TransparenciaResumoDTO.PublicacoesResumoDTO resumoPublicacoes(UUID tenantId) {
        long publicadas = publicacaoRepository
                .countByTenantIdAndStatus(tenantId, StatusPublicacaoTransparencia.PUBLICADO);
        String ultimaCompetencia = publicacaoRepository
                .findAllByTenantIdOrderByCompetenciaDesc(tenantId).stream()
                .filter(p -> p.getStatus() == StatusPublicacaoTransparencia.PUBLICADO)
                .map(TransparenciaPublicacao::getCompetencia)
                .findFirst().orElse("-");
        return new TransparenciaResumoDTO.PublicacoesResumoDTO(publicadas, ultimaCompetencia);
    }

    // ---- helpers ----

    private static long contarPorStatus(List<Licitacao> licitacoes, LicitacaoStatus status) {
        return licitacoes.stream().filter(l -> l.getStatus() == status).count();
    }

    private static BigDecimal somarValorNota(List<EntradaNfe> nfes) {
        return nfes.stream().map(n -> valorOuZero(n.getValorNota()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static BigDecimal somarAbastecimentos(List<FrotaAbastecimento> abastecimentos) {
        return abastecimentos.stream().map(a -> valorOuZero(a.getValorTotal()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static BigDecimal somarPedidos(List<PedidoCompra> pedidos) {
        return pedidos.stream().map(p -> valorOuZero(p.getValorTotal()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static BigDecimal valorOuZero(BigDecimal valor) {
        return valor != null ? valor : BigDecimal.ZERO;
    }

    private static PublicacaoResponseDTO toDTO(TransparenciaPublicacao p) {
        return new PublicacaoResponseDTO(
                p.getId(),
                p.getTenantId(),
                p.getCompetencia(),
                p.getTipoPublicacao(),
                p.getValorTotal(),
                p.getItensCount(),
                p.getStatus(),
                p.getDataPublicacao(),
                p.getObservacoes(),
                p.getCriadoEm(),
                p.getAtualizadoEm());
    }
}