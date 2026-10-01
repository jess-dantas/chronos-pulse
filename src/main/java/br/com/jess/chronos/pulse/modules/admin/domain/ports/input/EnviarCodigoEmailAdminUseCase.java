package br.com.jess.chronos.pulse.modules.admin.domain.ports.input;

public interface EnviarCodigoEmailAdminUseCase {

    record Comando(String tempToken) {}

    void executar(Comando comando);
}
