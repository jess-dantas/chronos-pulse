package br.com.jess.chronos.pulse.modules.auth.domain.ports.output;

import br.com.jess.chronos.pulse.modules.auth.domain.model.CpcUsuario;
import java.util.Optional;
import java.util.UUID;

public interface CpcUsuarioRepositoryPort {
    CpcUsuario salvar(CpcUsuario usuario);
    Optional<CpcUsuario> buscarPorCpf(String cpf);
    Optional<CpcUsuario> buscarPorId(UUID id);
    boolean existePorCpf(String cpf);
    CpcUsuario atualizar(CpcUsuario usuario);
    void desativarPorId(UUID id);
}
