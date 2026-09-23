package br.com.jess.chronos.pulse.modules.admin.domain.ports.output;

import br.com.jess.chronos.pulse.modules.admin.domain.model.AdminPlataforma;

import java.util.Optional;
import java.util.UUID;

public interface AdminPlataformaRepositoryPort {

    AdminPlataforma salvar(AdminPlataforma admin);

    Optional<AdminPlataforma> buscarPorId(UUID id);

    Optional<AdminPlataforma> buscarPorUsername(String username);

    Optional<AdminPlataforma> buscarPorEmail(String email);

    boolean existsByUsername(String username);

    long count();
}