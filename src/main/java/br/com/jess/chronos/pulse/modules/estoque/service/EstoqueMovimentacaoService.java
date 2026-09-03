package br.com.jess.chronos.pulse.modules.estoque.service;

import br.com.jess.chronos.pulse.modules.estoque.domain.entity.Almoxarifado;
import br.com.jess.chronos.pulse.modules.estoque.domain.entity.EstoqueMovimentacao;
import br.com.jess.chronos.pulse.modules.estoque.domain.entity.EstoqueSaldo;
import br.com.jess.chronos.pulse.modules.estoque.domain.entity.Material;
import br.com.jess.chronos.pulse.modules.estoque.domain.service.CalculadoraPmpService;
import br.com.jess.chronos.pulse.modules.estoque.repository.AlmoxarifadoRepository;
import br.com.jess.chronos.pulse.modules.estoque.repository.EstoqueMovimentacaoRepository;
import br.com.jess.chronos.pulse.modules.estoque.repository.EstoqueSaldoRepository;
import br.com.jess.chronos.pulse.modules.estoque.repository.MaterialRepository;
import br.com.jess.chronos.pulse.modules.estoque.web.dto.EntradaMaterialDTO;
import br.com.jess.chronos.pulse.modules.estoque.web.dto.SaidaMaterialDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EstoqueMovimentacaoService {

    private final EstoqueSaldoRepository saldoRepository;
    private final EstoqueMovimentacaoRepository movimentacaoRepository;
    private final AlmoxarifadoRepository almoxarifadoRepository;
    private final MaterialRepository materialRepository;
    private final CalculadoraPmpService calculadoraPmpService;

    @Transactional
    public void registrarEntrada(EntradaMaterialDTO dto, UUID tenantId, UUID usuarioCpcId) {
        Almoxarifado almoxarifado = almoxarifadoRepository.findByIdAndTenantId(dto.almoxarifadoId(), tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Almoxarifado não encontrado"));

        Material material = materialRepository.findByIdAndTenantId(dto.materialId(), tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Material não encontrado"));

        String lote = dto.lote() != null && !dto.lote().isBlank() ? dto.lote().trim() : null;

        EstoqueSaldo saldo = saldoRepository.findByTenantIdAndAlmoxarifadoIdAndMaterialIdAndLote(
                        tenantId, dto.almoxarifadoId(), dto.materialId(), lote)
                .orElseGet(() -> EstoqueSaldo.builder()
                        .tenantId(tenantId)
                        .almoxarifado(almoxarifado)
                        .material(material)
                        .lote(lote)
                        .dataValidade(dto.dataValidade())
                        .quantidadeAtual(BigDecimal.ZERO)
                        .custoMedioUnitario(BigDecimal.ZERO)
                        .build());

        // Cálculo do Custo Médio Ponderado (PMP) via domínio
        BigDecimal novoCustoMedio = calculadoraPmpService.calcularNovoCustoMedio(
                saldo.getQuantidadeAtual(),
                saldo.getCustoMedioUnitario(),
                dto.quantidade(),
                dto.valorUnitario()
        );

        BigDecimal novaQtdTotal = saldo.getQuantidadeAtual().add(dto.quantidade());
        BigDecimal valorTotalEntrada = dto.quantidade().multiply(dto.valorUnitario())
                .setScale(CalculadoraPmpService.CASAS_DECIMAIS_VALOR, java.math.RoundingMode.HALF_UP);

        saldo.setQuantidadeAtual(novaQtdTotal);
        saldo.setCustoMedioUnitario(novoCustoMedio);
        if (dto.dataValidade() != null) {
            saldo.setDataValidade(dto.dataValidade());
        }
        saldoRepository.save(saldo);

        EstoqueMovimentacao movimentacao = EstoqueMovimentacao.builder()
                .tenantId(tenantId)
                .almoxarifado(almoxarifado)
                .material(material)
                .tipoMovimento("ENTRADA_NFE")
                .quantidade(dto.quantidade())
                .valorUnitario(dto.valorUnitario())
                .valorTotal(valorTotalEntrada)
                .lote(lote)
                .dataValidade(dto.dataValidade())
                .documentoReferencia(dto.documentoReferencia())
                .usuarioCpcId(usuarioCpcId)
                .build();

        movimentacaoRepository.save(movimentacao);
    }

    @Transactional
    public void registrarSaida(SaidaMaterialDTO dto, UUID tenantId, UUID usuarioCpcId) {
        Almoxarifado almoxarifado = almoxarifadoRepository.findByIdAndTenantId(dto.almoxarifadoId(), tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Almoxarifado não encontrado"));

        Material material = materialRepository.findByIdAndTenantId(dto.materialId(), tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Material não encontrado"));

        String lote = dto.lote() != null && !dto.lote().isBlank() ? dto.lote().trim() : null;

        // Busca o saldo atual do material/lote
        EstoqueSaldo saldo = saldoRepository.findByTenantIdAndAlmoxarifadoIdAndMaterialIdAndLote(
                        tenantId, dto.almoxarifadoId(), dto.materialId(), lote)
                .orElseThrow(() -> new IllegalArgumentException("Saldo não encontrado para o material/lote informado"));

        // Validação de saldo suficiente
        if (saldo.getQuantidadeAtual().compareTo(dto.quantidade()) < 0) {
            throw new IllegalArgumentException("Saldo insuficiente em estoque. Saldo atual: "
                    + saldo.getQuantidadeAtual() + ", Solicitado: " + dto.quantidade());
        }

        // Na saída contábil (MCASP), o valor unitário é o Custo Médio Ponderado atual
        BigDecimal custoMedioAtual = saldo.getCustoMedioUnitario();
        BigDecimal valorTotalSaida = calculadoraPmpService.calcularValorTotalSaida(dto.quantidade(), custoMedioAtual);

        // Abate do saldo físico
        BigDecimal novaQuantidade = saldo.getQuantidadeAtual().subtract(dto.quantidade());
        saldo.setQuantidadeAtual(novaQuantidade);
        saldoRepository.save(saldo);

        // Registro da movimentação auditável de saída
        EstoqueMovimentacao movimentacao = EstoqueMovimentacao.builder()
                .tenantId(tenantId)
                .almoxarifado(almoxarifado)
                .material(material)
                .tipoMovimento("SAIDA_REQUISICAO")
                .quantidade(dto.quantidade())
                .valorUnitario(custoMedioAtual)
                .valorTotal(valorTotalSaida)
                .lote(lote)
                .dataValidade(saldo.getDataValidade())
                .documentoReferencia(dto.documentoReferencia())
                .usuarioCpcId(usuarioCpcId)
                .build();

        movimentacaoRepository.save(movimentacao);
    }
}
