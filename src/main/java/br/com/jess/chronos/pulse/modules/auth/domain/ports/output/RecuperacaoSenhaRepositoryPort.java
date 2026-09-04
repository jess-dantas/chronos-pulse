package br.com.jess.chronos.pulse.modules.auth.domain.ports.output;

import br.com.jess.chronos.pulse.modules.auth.domain.model.RecuperacaoSenha;

import java.util.Optional;

public interface RecuperacaoSenhaRepositoryPort {
    RecuperacaoSenha salvar(RecuperacaoSenha recuperacaoSenha);
    RecuperacaoSenha atualizar(RecuperacaoSenha recuperacaoSenha);
    Optional<RecuperacaoSenha> buscarUltimaPorCpf(String cpf);
    void marcarTodasComoUsadas(String cpf);
}