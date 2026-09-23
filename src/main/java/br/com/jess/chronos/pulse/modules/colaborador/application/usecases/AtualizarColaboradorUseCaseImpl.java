package br.com.jess.chronos.pulse.modules.colaborador.application.usecases;

import br.com.jess.chronos.pulse.modules.auth.domain.model.CpcUsuario;
import br.com.jess.chronos.pulse.modules.auth.domain.ports.output.CpcUsuarioRepositoryPort;
import br.com.jess.chronos.pulse.modules.colaborador.domain.model.Colaborador;
import br.com.jess.chronos.pulse.modules.colaborador.domain.ports.input.AtualizarColaboradorUseCase;
import br.com.jess.chronos.pulse.modules.colaborador.domain.ports.output.ColaboradorRepositoryPort;
import br.com.jess.chronos.pulse.modules.modulo.domain.ports.output.ModulosPort;

import java.util.ArrayList;
import java.util.List;

public class AtualizarColaboradorUseCaseImpl implements AtualizarColaboradorUseCase {

    private final ColaboradorRepositoryPort colaboradorRepository;
    private final CpcUsuarioRepositoryPort usuarioRepository;
    private final ModulosPort modulosPort;

    public AtualizarColaboradorUseCaseImpl(ColaboradorRepositoryPort colaboradorRepository,
                                           CpcUsuarioRepositoryPort usuarioRepository,
                                           ModulosPort modulosPort) {
        this.colaboradorRepository = colaboradorRepository;
        this.usuarioRepository = usuarioRepository;
        this.modulosPort = modulosPort;
    }

    @Override
    public void executar(Comando comando) {
        if (comando.tenantId() == null) {
            throw new IllegalArgumentException("Tenant ID obrigatório.");
        }
        Colaborador colaborador = colaboradorRepository.buscarPorIdETenant(comando.colaboradorId(), comando.tenantId())
                .orElseThrow(() -> new IllegalArgumentException("Colaborador não encontrado no seu tenant"));

        CpcUsuario usuario = usuarioRepository.buscarPorId(colaborador.getCpcUsuarioId())
                .orElseThrow(() -> new IllegalArgumentException("Usuário do colaborador não encontrado"));

        CpcUsuario usuarioAtualizado = new CpcUsuario(
                usuario.getId(), usuario.getCpcId(), usuario.getCpf(),
                comando.nome(), comando.emailCorporativo(),
                usuario.getSenhaHash(), usuario.getRole(), usuario.getTenantId(),
                comando.acessoEstoque(), comando.acessoPatrimonio(),
                comando.acessoFrota(), comando.acessoProtocolo(), usuario.getFoto());
        // Preserva dados pessoais já cadastrados; celular só muda se informado.
        usuarioAtualizado.atualizarDadosPessoais(
                usuario.getApelido(),
                comando.celular() != null ? comando.celular() : usuario.getCelular(),
                usuario.getEmailPessoal());
        usuarioRepository.atualizar(usuarioAtualizado);

        List<String> modulosAtuais = modulosPort.listarCodigosDoUsuario(usuario.getId(), usuario.getTenantId());
        List<String> atualizados = new ArrayList<>();
        for (String codigo : modulosAtuais) {
            boolean toggleavel = codigo.equals("ESTOQUE") || codigo.equals("PATRIMONIO")
                    || codigo.equals("FROTA") || codigo.equals("PROTOCOLO");
            if (!toggleavel) {
                atualizados.add(codigo);
            }
        }
        if (comando.acessoEstoque()) {
            atualizados.add("ESTOQUE");
        }
        if (comando.acessoPatrimonio()) {
            atualizados.add("PATRIMONIO");
        }
        if (comando.acessoFrota()) {
            atualizados.add("FROTA");
        }
        if (comando.acessoProtocolo()) {
            atualizados.add("PROTOCOLO");
        }
        modulosPort.definirModulosDoUsuario(usuario.getId(), usuario.getTenantId(), atualizados);

        Colaborador colaboradorAtualizado = new Colaborador(
                colaborador.getId(), colaborador.getCpcUsuarioId(), colaborador.getTenantId(),
                comando.matricula(), comando.cargo(), comando.departamento(),
                comando.dataNascimento(), comando.dataAdmissao(),
                colaborador.getConfiguracaoJornadaId(), comando.dataDesligamento());
        colaboradorRepository.atualizar(colaboradorAtualizado);
    }
}
