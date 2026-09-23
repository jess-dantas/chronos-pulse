package br.com.jess.chronos.pulse.modules.admin.domain.ports.output;

import br.com.jess.chronos.pulse.modules.admin.domain.model.AdminRecoveryCode;

import java.util.List;
import java.util.UUID;

public interface AdminRecoveryCodeRepositoryPort {

    AdminRecoveryCode salvar(AdminRecoveryCode codigo);

    void salvarTodos(List<AdminRecoveryCode> codigos);

    List<AdminRecoveryCode> listarPorAdmin(UUID adminId);

    void removerPorAdmin(UUID adminId);
}
