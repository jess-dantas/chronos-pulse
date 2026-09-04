package br.com.jess.chronos.pulse.modules.auth.domain.ports.input;

public interface AlterarFotoPerfilUseCase {

    record Comando(String cpf, byte[] bytes) {}

    String executar(Comando comando);
}