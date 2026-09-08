package br.com.jess.chronos.pulse.modules.auditoria.repository;

import br.com.jess.chronos.pulse.modules.auditoria.domain.entity.Auditoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AuditoriaRepository extends JpaRepository<Auditoria, UUID> {

    Optional<Auditoria> findTopByOrderByDataHoraDesc();

    @Query("SELECT a FROM Auditoria a WHERE a.tenantId IS NULL OR a.tenantId = ?1 ORDER BY a.dataHora DESC")
    List<Auditoria> findRecentes(UUID tenantId);

    @Query("SELECT a FROM Auditoria a WHERE a.entidadeId = ?1 AND a.entidade = ?2 ORDER BY a.dataHora DESC")
    List<Auditoria> findByEntidade(String entidadeId, String entidade);
}