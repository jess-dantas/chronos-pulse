package br.com.jess.chronos.pulse.modules.empresa.domain.ports.input;

import br.com.jess.chronos.pulse.modules.empresa.domain.model.Empresa;

import java.util.UUID;

public interface AtualizarEmpresaUseCase {
    record Comando(UUID id, String nome, Boolean ativo) {}
    Empresa executar(Comando comando);
}
