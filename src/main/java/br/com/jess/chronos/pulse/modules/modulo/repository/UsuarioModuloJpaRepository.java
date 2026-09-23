package br.com.jess.chronos.pulse.modules.modulo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

public interface UsuarioModuloJpaRepository extends JpaRepository<UsuarioModuloJpaEntity, UUID> {
    List<UsuarioModuloJpaEntity> findByUsuarioIdAndTenantId(UUID usuarioId, UUID tenantId);

    @Modifying
    @Transactional
    void deleteByUsuarioIdAndTenantId(UUID usuarioId, UUID tenantId);
}
