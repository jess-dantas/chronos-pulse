package br.com.jess.chronos.pulse.modules.auth.infrastructure.adapters.output.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeviceTokenJpaRepository extends JpaRepository<DeviceTokenJpaEntity, UUID> {
    Optional<DeviceTokenJpaEntity> findByTokenHash(String tokenHash);
    List<DeviceTokenJpaEntity> findByUsuarioIdAndRevogadoEmIsNull(UUID usuarioId);
}
