package br.com.jess.chronos.pulse.modules.admin.infrastructure.adapters.output.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AdminDeviceTokenJpaRepository extends JpaRepository<AdminDeviceTokenJpaEntity, UUID> {
    Optional<AdminDeviceTokenJpaEntity> findByTokenHash(String tokenHash);
    List<AdminDeviceTokenJpaEntity> findByAdminIdAndRevogadoEmIsNull(UUID adminId);
}
