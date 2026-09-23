package br.com.jess.chronos.pulse.modules.admin.domain.ports.input;

public interface AlterarSenhaAdminUseCase {

    record Comando(String adminId, String senhaAtual, String novaSenha) {}

    void executar(Comando comando);
}
