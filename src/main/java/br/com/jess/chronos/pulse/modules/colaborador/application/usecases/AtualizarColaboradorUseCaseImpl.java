package br.com.jess.chronos.pulse.modules.colaborador.application.usecases;

import br.com.jess.chronos.pulse.modules.auth.domain.model.CpcUsuario;
import br.com.jess.chronos.pulse.modules.auth.domain.ports.output.CpcUsuarioRepositoryPort;
import br.com.jess.chronos.pulse.modules.colaborador.domain.model.Colaborador;
import br.com.jess.chronos.pulse.modules.colaborador.domain.ports.input.AtualizarColaboradorUseCase;
import br.com.jess.chronos.pulse.modules.colaborador.domain.ports.output.ColaboradorRepositoryPort;

public class AtualizarColaboradorUseCaseImpl implements AtualizarColaboradorUseCase {

    private final ColaboradorRepositoryPort colaboradorRepository;
    private final CpcUsuarioRepositoryPort usuarioRepository;

    public AtualizarColaboradorUseCaseImpl(ColaboradorRepositoryPort colaboradorRepository,
                                           CpcUsuarioRepositoryPort usuarioRepository) {
        this.colaboradorRepository = colaboradorRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public void executar(Comando comando) {
        Colaborador colaborador = colaboradorRepository.buscarPorId(comando.colaboradorId())
                .orElseThrow(() -> new IllegalArgumentException("Colaborador não encontrado"));

        CpcUsuario usuario = usuarioRepository.buscarPorId(colaborador.getCpcUsuarioId())
                .orElseThrow(() -> new IllegalArgumentException("Usuário do colaborador não encontrado"));

        CpcUsuario usuarioAtualizado = new CpcUsuario(
                usuario.getId(), usuario.getCpcId(), usuario.getCpf(),
                comando.nome(), comando.emailCorporativo(),
                usuario.getSenhaHash(), usuario.getRole(), usuario.getTenantId(),
                comando.acessoEstoque());
        usuarioRepository.atualizar(usuarioAtualizado);

        Colaborador colaboradorAtualizado = new Colaborador(
                colaborador.getId(), colaborador.getCpcUsuarioId(), colaborador.getTenantId(),
                comando.matricula(), comando.cargo(), comando.departamento(),
                comando.dataNascimento(), comando.dataAdmissao(),
                colaborador.getConfiguracaoJornadaId());
        colaboradorRepository.atualizar(colaboradorAtualizado);
    }
}
