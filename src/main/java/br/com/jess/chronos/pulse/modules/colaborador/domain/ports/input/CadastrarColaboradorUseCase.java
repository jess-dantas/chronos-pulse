package br.com.jess.chronos.pulse.modules.colaborador.domain.ports.input;

import br.com.jess.chronos.pulse.modules.colaborador.domain.model.Colaborador;
import java.time.LocalDate;
import java.util.UUID;

public interface CadastrarColaboradorUseCase {
    record Comando(
            String cpf, String nome, String emailCorporativo, String senha,
            String matricula, String cargo, String departamento,
            LocalDate dataNascimento, LocalDate dataAdmissao, LocalDate dataDesligamento,
            UUID tenantId, UUID configuracaoJornadaId,
            boolean acessoEstoque, boolean acessoPatrimonio,
            boolean acessoFrota, boolean acessoProtocolo
    ) {
        public Comando(String cpf, String nome, String emailCorporativo, String senha,
                       String matricula, String cargo, String departamento,
                       LocalDate dataNascimento, LocalDate dataAdmissao,
                       UUID tenantId, UUID configuracaoJornadaId) {
            this(cpf, nome, emailCorporativo, senha, matricula, cargo, departamento,
                    dataNascimento, dataAdmissao, null, tenantId, configuracaoJornadaId,
                    false, false, false, false);
        }
    }
    Colaborador executar(Comando comando);
}
