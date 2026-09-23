package br.com.jess.chronos.pulse.modules.admin.infrastructure.adapters.output.persistence;

import br.com.jess.chronos.pulse.modules.admin.domain.model.AdminRecoveryCode;
import br.com.jess.chronos.pulse.modules.admin.domain.ports.output.AdminRecoveryCodeRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class AdminRecoveryCodeRepositoryAdapter implements AdminRecoveryCodeRepositoryPort {

    private final AdminRecoveryCodeJpaRepository jpaRepository;

    public AdminRecoveryCodeRepositoryAdapter(AdminRecoveryCodeJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public AdminRecoveryCode salvar(AdminRecoveryCode codigo) {
        return jpaRepository.save(codigo);
    }

    @Override
    public void salvarTodos(List<AdminRecoveryCode> codigos) {
        jpaRepository.saveAll(codigos);
    }

    @Override
    public List<AdminRecoveryCode> listarPorAdmin(UUID adminId) {
        return jpaRepository.findByAdminIdOrderByCriadoEmAsc(adminId);
    }

    @Override
    public void removerPorAdmin(UUID adminId) {
        jpaRepository.deleteByAdminId(adminId);
    }
}
