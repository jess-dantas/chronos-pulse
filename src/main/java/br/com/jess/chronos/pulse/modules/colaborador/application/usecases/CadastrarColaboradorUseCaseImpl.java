package br.com.jess.chronos.pulse.modules.colaborador.application.usecases;

import br.com.jess.chronos.pulse.modules.auth.domain.model.CpcUsuario;
import br.com.jess.chronos.pulse.modules.auth.domain.model.Role;
import br.com.jess.chronos.pulse.modules.auth.domain.service.PasswordPolicy;
import br.com.jess.chronos.pulse.modules.auth.domain.ports.output.CpcUsuarioRepositoryPort;
import br.com.jess.chronos.pulse.modules.colaborador.domain.model.Colaborador;
import br.com.jess.chronos.pulse.modules.colaborador.domain.ports.input.CadastrarColaboradorUseCase;
import br.com.jess.chronos.pulse.modules.colaborador.domain.ports.output.ColaboradorRepositoryPort;
import br.com.jess.chronos.pulse.modules.modulo.domain.ports.output.ModulosPort;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

public class CadastrarColaboradorUseCaseImpl implements CadastrarColaboradorUseCase {

    private final ColaboradorRepositoryPort colaboradorRepository;
    private final CpcUsuarioRepositoryPort usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModulosPort modulosPort;

    public CadastrarColaboradorUseCaseImpl(ColaboradorRepositoryPort colaboradorRepository,
                                           CpcUsuarioRepositoryPort usuarioRepository,
                                           PasswordEncoder passwordEncoder,
                                           ModulosPort modulosPort) {
        this.colaboradorRepository = colaboradorRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.modulosPort = modulosPort;
    }

    @Override
    public Colaborador executar(Comando comando) {
        if (usuarioRepository.existePorCpf(comando.cpf())) {
            throw new IllegalArgumentException("CPF já cadastrado: " + comando.cpf());
        }

        PasswordPolicy.validar(comando.senha(), Role.COLABORADOR)
                .ifPresent(mensagem -> {
                    throw new IllegalArgumentException(mensagem);
                });

        CpcUsuario usuario = new CpcUsuario(
                null, null, comando.cpf(), comando.nome(), comando.emailCorporativo(),
                passwordEncoder.encode(comando.senha()), Role.COLABORADOR, comando.tenantId(),
                comando.acessoEstoque(), comando.acessoPatrimonio(),
                comando.acessoFrota(), comando.acessoProtocolo(), null);
        usuario.atualizarDadosPessoais(null, comando.celular(), null);
        usuario = usuarioRepository.salvar(usuario);

        List<String> modulosIniciais = new java.util.ArrayList<>();
        modulosIniciais.add("PONTO");
        if (comando.acessoEstoque()) {
            modulosIniciais.add("ESTOQUE");
        }
        if (comando.acessoPatrimonio()) {
            modulosIniciais.add("PATRIMONIO");
        }
        if (comando.acessoFrota()) {
            modulosIniciais.add("FROTA");
        }
        if (comando.acessoProtocolo()) {
            modulosIniciais.add("PROTOCOLO");
        }
        modulosPort.definirModulosDoUsuario(usuario.getId(), comando.tenantId(), modulosIniciais);

        return colaboradorRepository.salvar(new Colaborador(
                null, usuario.getId(), comando.tenantId(), comando.matricula(),
                comando.cargo(), comando.departamento(), comando.dataNascimento(),
                comando.dataAdmissao(), comando.configuracaoJornadaId(), comando.dataDesligamento()));
    }
}
