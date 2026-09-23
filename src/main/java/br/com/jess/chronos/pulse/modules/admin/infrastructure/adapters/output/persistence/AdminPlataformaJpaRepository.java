package br.com.jess.chronos.pulse.modules.admin.infrastructure.adapters.output.persistence;

import br.com.jess.chronos.pulse.modules.admin.domain.model.AdminPlataforma;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface AdminPlataformaJpaRepository extends JpaRepository<AdminPlataformaJpaEntity, UUID> {

    @Query("SELECT a FROM AdminPlataformaJpaEntity a WHERE a.username = :username")
    Optional<AdminPlataformaJpaEntity> findByUsername(@Param("username") String username);

    @Query("SELECT a FROM AdminPlataformaJpaEntity a WHERE a.email = :email")
    Optional<AdminPlataformaJpaEntity> findByEmail(@Param("email") String email);

    boolean existsByUsername(String username);
}