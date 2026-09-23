package br.com.jess.chronos.pulse.modules.admin.domain.ports.input;

public interface BootstrapAdminUseCase {

    record Comando(String username, String senha, String nomeCompleto, String email) {}

    record Resultado(String tempToken) {}

    boolean disponivel();

    Resultado executar(Comando comando);
}
