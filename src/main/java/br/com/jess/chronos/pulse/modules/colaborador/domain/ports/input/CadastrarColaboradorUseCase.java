package br.com.jess.chronos.pulse.modules.colaborador.domain.ports.input;

import br.com.jess.chronos.pulse.modules.colaborador.domain.model.Colaborador;
import java.time.LocalDate;
import java.util.UUID;

public interface CadastrarColaboradorUseCase {
    record Comando(
            String cpf, String nome, String emailCorporativo, String senha,
            String matricula, String cargo, String departamento,
            LocalDate dataNascimento, LocalDate dataAdmissao,
            UUID tenantId, UUID configuracaoJornadaId
    ) {}
    Colaborador executar(Comando comando);
}
