package br.com.jess.chronos.pulse.modules.lead.infrastructure.adapters.output.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LeadEmpresaJpaRepository extends JpaRepository<LeadEmpresaJpaEntity, UUID> {
}