package br.com.jess.chronos.pulse.modules.auth.application.usecases;

import br.com.jess.chronos.pulse.modules.auth.domain.model.CpcUsuario;
import br.com.jess.chronos.pulse.modules.auth.domain.ports.input.BuscarPerfilUseCase;
import br.com.jess.chronos.pulse.modules.auth.domain.ports.output.CpcUsuarioRepositoryPort;
import br.com.jess.chronos.pulse.modules.empresa.domain.model.Empresa;
import br.com.jess.chronos.pulse.modules.empresa.domain.ports.output.EmpresaRepositoryPort;
import br.com.jess.chronos.pulse.modules.modulo.domain.ports.output.ModulosPort;

import java.util.Collections;
import java.util.List;

public class BuscarPerfilUseCaseImpl implements BuscarPerfilUseCase {

    private final CpcUsuarioRepositoryPort repositoryPort;
    private final ModulosPort modulosPort;
    private final EmpresaRepositoryPort empresaRepository;

    public BuscarPerfilUseCaseImpl(CpcUsuarioRepositoryPort repositoryPort,
                                   ModulosPort modulosPort,
                                   EmpresaRepositoryPort empresaRepository) {
        this.repositoryPort = repositoryPort;
        this.modulosPort = modulosPort;
        this.empresaRepository = empresaRepository;
    }

    @Override
    public Resultado executar(String cpf) {
        CpcUsuario usuario = repositoryPort.buscarPorCpf(cpf)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        String tenantId = usuario.getTenantId() != null ? usuario.getTenantId().toString() : null;
        String tenantSlug = usuario.getTenantId() != null
                ? empresaRepository.buscarPorId(usuario.getTenantId()).map(Empresa::getSlug).orElse(null)
                : null;
        List<String> modulos = usuario.getTenantId() != null
                ? modulosPort.listarCodigosAtivos(usuario.getTenantId())
                : Collections.emptyList();

        return new Resultado(
                usuario.getCpf(),
                usuario.getNome(),
                usuario.getEmailCorporativo() != null ? usuario.getEmailCorporativo() : usuario.getEmailPessoal(),
                usuario.getRole().name(),
                tenantId,
                tenantSlug,
                usuario.getCpcId().toString(),
                usuario.isAcessoEstoque(),
                usuario.isAcessoPatrimonio(),
                usuario.isAcessoFrota(),
                usuario.isAcessoProtocolo(),
                usuario.getFoto(),
                modulos);
    }
}
