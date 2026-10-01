package br.com.jess.chronos.pulse.modules.admin.domain.ports.input;

public interface RedefinirSenhaAdminUseCase {

    record Comando(String username, String codigo, String novaSenha) {}

    void executar(Comando comando);
}
