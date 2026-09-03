package br.com.jess.chronos.pulse.modules.colaborador.application.usecases;

import br.com.jess.chronos.pulse.modules.auth.domain.ports.output.CpcUsuarioRepositoryPort;
import br.com.jess.chronos.pulse.modules.colaborador.domain.model.Colaborador;
import br.com.jess.chronos.pulse.modules.colaborador.domain.ports.input.ListarColaboradoresUseCase;
import br.com.jess.chronos.pulse.modules.colaborador.domain.ports.output.ColaboradorRepositoryPort;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ListarColaboradoresUseCaseImpl implements ListarColaboradoresUseCase {

    private final ColaboradorRepositoryPort colaboradorRepository;
    private final CpcUsuarioRepositoryPort usuarioRepository;

    public ListarColaboradoresUseCaseImpl(ColaboradorRepositoryPort colaboradorRepository,
                                          CpcUsuarioRepositoryPort usuarioRepository) {
        this.colaboradorRepository = colaboradorRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public List<ColaboradorItem> executar(UUID tenantId) {
        List<Colaborador> colaboradores = (tenantId != null)
                ? colaboradorRepository.listarPorTenant(tenantId)
                : colaboradorRepository.listarTodos();

        List<ColaboradorItem> resultado = new ArrayList<>();

        for (Colaborador colab : colaboradores) {
            usuarioRepository.buscarPorId(colab.getCpcUsuarioId()).ifPresent(usuario -> {
                resultado.add(new ColaboradorItem(
                        colab.getId(),
                        colab.getCpcUsuarioId(),
                        colab.getTenantId(),
                        usuario.getCpf(),
                        usuario.getNome(),
                        usuario.getEmailCorporativo() != null ? usuario.getEmailCorporativo() : usuario.getEmailPessoal(),
                        colab.getMatricula(),
                        colab.getCargo(),
                        colab.getDepartamento(),
                        colab.getDataAdmissao(),
                        colab.getDataNascimento(),
                        usuario.isAcessoEstoque(),
                        colab.isAtivo()
                ));
            });
        }

        return resultado;
    }
}
