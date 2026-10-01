package br.com.jess.chronos.pulse.modules.admin.domain.ports.input;

public interface SolicitarResetSenhaAdminUseCase {

    record Comando(String username) {}

    void executar(Comando comando);
}
