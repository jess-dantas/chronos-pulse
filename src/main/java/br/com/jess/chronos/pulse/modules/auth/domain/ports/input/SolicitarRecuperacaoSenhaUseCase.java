package br.com.jess.chronos.pulse.modules.auth.domain.ports.input;

public interface SolicitarRecuperacaoSenhaUseCase {

    record Comando(String cpf) {}

    void executar(Comando comando);
}