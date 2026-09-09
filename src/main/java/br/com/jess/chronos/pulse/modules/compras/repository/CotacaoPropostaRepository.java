package br.com.jess.chronos.pulse.modules.compras.repository;

import br.com.jess.chronos.pulse.modules.compras.domain.entity.CotacaoProposta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CotacaoPropostaRepository extends JpaRepository<CotacaoProposta, UUID> {
    List<CotacaoProposta> findAllByCotacaoId(UUID cotacaoId);
    Optional<CotacaoProposta> findByCotacaoIdAndFornecedorIdAndMaterialId(
            UUID cotacaoId, UUID fornecedorId, UUID materialId);
}