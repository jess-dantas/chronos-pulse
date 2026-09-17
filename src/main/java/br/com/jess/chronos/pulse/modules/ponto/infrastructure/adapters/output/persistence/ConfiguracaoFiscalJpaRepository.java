package br.com.jess.chronos.pulse.modules.ponto.infrastructure.adapters.output.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ConfiguracaoFiscalJpaRepository extends JpaRepository<ConfiguracaoFiscalJpaEntity, UUID> {
}