package br.com.jess.chronos.pulse.modules.licitacoes.repository;

import br.com.jess.chronos.pulse.modules.licitacoes.domain.entity.LicitacaoEdital;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface LicitacaoEditalRepository extends JpaRepository<LicitacaoEdital, UUID> {

    Optional<LicitacaoEdital> findByLicitacaoId(UUID licitacaoId);
}