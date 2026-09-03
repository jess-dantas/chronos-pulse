package br.com.jess.chronos.pulse.modules.colaborador.application.usecases;

import br.com.jess.chronos.pulse.modules.auth.domain.model.CpcUsuario;
import br.com.jess.chronos.pulse.modules.auth.domain.model.Role;
import br.com.jess.chronos.pulse.modules.auth.domain.ports.output.CpcUsuarioRepositoryPort;
import br.com.jess.chronos.pulse.modules.colaborador.domain.model.Colaborador;
import br.com.jess.chronos.pulse.modules.colaborador.domain.ports.input.CadastrarColaboradorUseCase;
import br.com.jess.chronos.pulse.modules.colaborador.domain.ports.output.ColaboradorRepositoryPort;
import org.springframework.security.crypto.password.PasswordEncoder;

public class CadastrarColaboradorUseCaseImpl implements CadastrarColaboradorUseCase {

    private final ColaboradorRepositoryPort colaboradorRepository;
    private final CpcUsuarioRepositoryPort usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public CadastrarColaboradorUseCaseImpl(ColaboradorRepositoryPort colaboradorRepository,
                                           CpcUsuarioRepositoryPort usuarioRepository,
                                           PasswordEncoder passwordEncoder) {
        this.colaboradorRepository = colaboradorRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Colaborador executar(Comando comando) {
        if (usuarioRepository.existePorCpf(comando.cpf())) {
            throw new IllegalArgumentException("CPF já cadastrado: " + comando.cpf());
        }

        CpcUsuario usuario = usuarioRepository.salvar(new CpcUsuario(
                null, null, comando.cpf(), comando.nome(), comando.emailCorporativo(),
                passwordEncoder.encode(comando.senha()), Role.COLABORADOR, comando.tenantId(), comando.acessoEstoque()));

        return colaboradorRepository.salvar(new Colaborador(
                null, usuario.getId(), comando.tenantId(), comando.matricula(),
                comando.cargo(), comando.departamento(), comando.dataNascimento(),
                comando.dataAdmissao(), comando.configuracaoJornadaId()));
    }
}
