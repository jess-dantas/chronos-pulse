package br.com.jess.chronos.pulse.modules.licitacoes.repository;

import br.com.jess.chronos.pulse.modules.licitacoes.domain.entity.LicitacaoLance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LicitacaoLanceRepository extends JpaRepository<LicitacaoLance, UUID> {

    List<LicitacaoLance> findAllByLicitacaoIdOrderByAtualizadoEmDesc(UUID licitacaoId);

    Optional<LicitacaoLance> findByLicitacaoIdAndLicitacaoItemIdAndFornecedorId(
            UUID licitacaoId, UUID licitacaoItemId, UUID fornecedorId);
}
