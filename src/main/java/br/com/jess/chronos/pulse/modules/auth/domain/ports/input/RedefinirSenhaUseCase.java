package br.com.jess.chronos.pulse.modules.auth.domain.ports.input;

public interface RedefinirSenhaUseCase {

    record Comando(String cpf, String codigo, String novaSenha) {}

    void executar(Comando comando);
}