package br.com.jess.chronos.pulse.modules.admin.infrastructure.adapters.output.persistence;

import br.com.jess.chronos.pulse.modules.admin.domain.model.AdminRecoveryCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AdminRecoveryCodeJpaRepository extends JpaRepository<AdminRecoveryCode, UUID> {

    List<AdminRecoveryCode> findByAdminIdOrderByCriadoEmAsc(UUID adminId);

    void deleteByAdminId(UUID adminId);
}
