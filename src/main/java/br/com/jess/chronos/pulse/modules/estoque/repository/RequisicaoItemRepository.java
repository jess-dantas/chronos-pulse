package br.com.jess.chronos.pulse.modules.estoque.repository;

import br.com.jess.chronos.pulse.modules.estoque.domain.entity.RequisicaoItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RequisicaoItemRepository extends JpaRepository<RequisicaoItem, UUID> {
    List<RequisicaoItem> findAllByRequisicaoId(UUID requisicaoId);
}
