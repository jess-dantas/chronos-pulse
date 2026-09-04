package br.com.jess.chronos.pulse.modules.colaborador.domain.ports.input;

import java.time.LocalDate;
import java.util.UUID;

public interface AtualizarColaboradorUseCase {

    record Comando(
            UUID colaboradorId,
            String nome,
            String emailCorporativo,
            String matricula,
            String cargo,
            String departamento,
            LocalDate dataNascimento,
            LocalDate dataAdmissao,
            boolean acessoEstoque
    ) {}

    void executar(Comando comando);
}
