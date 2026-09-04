package br.com.jess.chronos.pulse.modules.auth.application.usecases;

import br.com.jess.chronos.pulse.modules.auth.domain.model.CpcUsuario;
import br.com.jess.chronos.pulse.modules.auth.domain.model.Role;
import br.com.jess.chronos.pulse.modules.auth.domain.ports.input.CadastrarEmpresaCompletoUseCase;
import br.com.jess.chronos.pulse.modules.auth.domain.ports.output.CpcUsuarioRepositoryPort;
import br.com.jess.chronos.pulse.modules.auth.infrastructure.security.JwtService;
import br.com.jess.chronos.pulse.modules.colaborador.domain.model.Colaborador;
import br.com.jess.chronos.pulse.modules.colaborador.domain.ports.output.ColaboradorRepositoryPort;
import br.com.jess.chronos.pulse.modules.empresa.domain.model.Empresa;
import br.com.jess.chronos.pulse.modules.empresa.domain.ports.output.EmpresaRepositoryPort;
import br.com.jess.chronos.pulse.shared.util.CnpjValidator;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;

public class CadastrarEmpresaCompletoUseCaseImpl implements CadastrarEmpresaCompletoUseCase {

    private final EmpresaRepositoryPort empresaRepository;
    private final CpcUsuarioRepositoryPort usuarioRepository;
    private final ColaboradorRepositoryPort colaboradorRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public CadastrarEmpresaCompletoUseCaseImpl(
            EmpresaRepositoryPort empresaRepository,
            CpcUsuarioRepositoryPort usuarioRepository,
            ColaboradorRepositoryPort colaboradorRepository,
            JwtService jwtService,
            PasswordEncoder passwordEncoder) {
        this.empresaRepository = empresaRepository;
        this.usuarioRepository = usuarioRepository;
        this.colaboradorRepository = colaboradorRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Resultado executar(Comando comando) {
        String cnpj = CnpjValidator.normalizar(comando.cnpj());
        if (!CnpjValidator.validar(cnpj)) {
            throw new IllegalArgumentException("CNPJ inválido: " + comando.cnpj());
        }
        if (empresaRepository.existePorCnpj(cnpj)) {
            throw new IllegalArgumentException("CNPJ já cadastrado: " + cnpj);
        }
        if (usuarioRepository.existePorCpf(comando.responsavelCpf())) {
            throw new IllegalArgumentException("CPF já cadastrado: " + comando.responsavelCpf());
        }

        Empresa empresa = empresaRepository.salvar(new Empresa(
                null, cnpj, comando.nomeEmpresa(),
                comando.responsavelNome(), comando.responsavelCpf(),
                comando.responsavelEmail(), comando.responsavelCelular(),
                comando.responsavelTelefone(), comando.enderecoLogradouro(),
                comando.enderecoNumero(), comando.enderecoComplemento(),
                comando.enderecoBairro(), comando.enderecoCidade(),
                comando.enderecoUf(), comando.enderecoCep()));

        CpcUsuario usuario = usuarioRepository.salvar(new CpcUsuario(
                null, null, comando.responsavelCpf(), comando.responsavelNome(),
                comando.responsavelEmail(),
                passwordEncoder.encode(comando.responsavelSenha()),
                Role.ADMIN_EMPRESA, empresa.getId()));

        colaboradorRepository.salvar(new Colaborador(
                null, usuario.getId(), empresa.getId(),
                null, "Administrador", "Administração",
                null, LocalDate.now(), null));

        String accessToken = jwtService.gerarAccessToken(
                usuario.getCpf(), usuario.getRole().name(),
                usuario.getCpcId().toString(),
                empresa.getId().toString(), usuario.isAcessoEstoque());
        String refreshToken = jwtService.gerarRefreshToken(usuario.getCpf());

        return new Resultado(
                accessToken, refreshToken,
                usuario.getRole().name(),
                usuario.getCpcId().toString(),
                usuario.getNome(),
                usuario.getEmailCorporativo(),
                empresa.getId().toString(),
                usuario.isAcessoEstoque(),
                usuario.getFoto());
    }
}
