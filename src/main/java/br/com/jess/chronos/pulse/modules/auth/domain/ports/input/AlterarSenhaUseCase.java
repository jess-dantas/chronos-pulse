package br.com.jess.chronos.pulse.modules.auth.domain.ports.input;

public interface AlterarSenhaUseCase {

    record Comando(String cpf, String novaSenha) {}

    void executar(Comando comando);
}