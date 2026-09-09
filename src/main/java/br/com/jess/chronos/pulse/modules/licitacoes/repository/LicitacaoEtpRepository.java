package br.com.jess.chronos.pulse.modules.licitacoes.repository;

import br.com.jess.chronos.pulse.modules.licitacoes.domain.entity.LicitacaoEtp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface LicitacaoEtpRepository extends JpaRepository<LicitacaoEtp, UUID> {

    Optional<LicitacaoEtp> findByLicitacaoId(UUID licitacaoId);
}