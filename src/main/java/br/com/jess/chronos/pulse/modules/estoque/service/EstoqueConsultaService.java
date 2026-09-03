package br.com.jess.chronos.pulse.modules.estoque.service;

import br.com.jess.chronos.pulse.modules.estoque.domain.entity.EstoqueSaldo;
import br.com.jess.chronos.pulse.modules.estoque.repository.EstoqueSaldoRepository;
import br.com.jess.chronos.pulse.modules.estoque.web.dto.EstoqueSaldoResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EstoqueConsultaService {

    private final EstoqueSaldoRepository saldoRepository;

    @Transactional(readOnly = true)
    public Page<EstoqueSaldoResponseDTO> listarSaldos(
            UUID tenantId,
            UUID almoxarifadoId,
            UUID grupoId,
            Boolean abaixoMinimo,
            Pageable pageable) {

        Page<EstoqueSaldo> paginaSaldos = saldoRepository.consultarSaldosComFiltro(
                tenantId, almoxarifadoId, grupoId, abaixoMinimo, pageable);

        return paginaSaldos.map(saldo -> {
            BigDecimal valorTotal = saldo.getQuantidadeAtual().multiply(saldo.getCustoMedioUnitario());
            boolean estaAbaixoMinimo = saldo.getQuantidadeAtual().compareTo(saldo.getMaterial().getEstoqueMinimo()) <= 0;

            return new EstoqueSaldoResponseDTO(
                    saldo.getId(),
                    saldo.getAlmoxarifado().getId(),
                    saldo.getAlmoxarifado().getNome(),
                    saldo.getMaterial().getId(),
                    saldo.getMaterial().getDescricao(),
                    saldo.getMaterial().getUnidadeMedida(),
                    saldo.getLote(),
                    saldo.getDataValidade(),
                    saldo.getQuantidadeAtual(),
                    saldo.getMaterial().getEstoqueMinimo(),
                    saldo.getCustoMedioUnitario(),
                    valorTotal,
                    estaAbaixoMinimo
            );
        });
    }
}