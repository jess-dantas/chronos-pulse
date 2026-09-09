package br.com.jess.chronos.pulse.modules.licitacoes.repository;

import br.com.jess.chronos.pulse.modules.licitacoes.domain.entity.LicitacaoTr;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface LicitacaoTrRepository extends JpaRepository<LicitacaoTr, UUID> {

    Optional<LicitacaoTr> findByLicitacaoId(UUID licitacaoId);
}