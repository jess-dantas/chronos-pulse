package br.com.jess.chronos.pulse.modules.auth.application.usecases;

import br.com.jess.chronos.pulse.modules.auth.domain.model.CpcUsuario;
import br.com.jess.chronos.pulse.modules.auth.domain.ports.input.BuscarPerfilUseCase;
import br.com.jess.chronos.pulse.modules.auth.domain.ports.output.CpcUsuarioRepositoryPort;

public class BuscarPerfilUseCaseImpl implements BuscarPerfilUseCase {

    private final CpcUsuarioRepositoryPort repositoryPort;

    public BuscarPerfilUseCaseImpl(CpcUsuarioRepositoryPort repositoryPort) {
        this.repositoryPort = repositoryPort;
    }

    @Override
    public Resultado executar(String cpf) {
        CpcUsuario usuario = repositoryPort.buscarPorCpf(cpf)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        String tenantId = usuario.getTenantId() != null ? usuario.getTenantId().toString() : null;

        return new Resultado(
                usuario.getCpf(),
                usuario.getNome(),
                usuario.getEmailCorporativo() != null ? usuario.getEmailCorporativo() : usuario.getEmailPessoal(),
                usuario.getRole().name(),
                tenantId,
                usuario.getCpcId().toString(),
                usuario.isAcessoEstoque(),
                usuario.getFoto());
    }
}
