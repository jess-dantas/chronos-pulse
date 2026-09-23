package br.com.jess.chronos.pulse.modules.admin.infrastructure.config;

import br.com.jess.chronos.pulse.modules.admin.application.usecases.*;
import br.com.jess.chronos.pulse.modules.admin.domain.ports.input.*;
import br.com.jess.chronos.pulse.modules.admin.domain.ports.output.*;
import br.com.jess.chronos.pulse.modules.admin.domain.ports.output.ContratoRepositoryPort;
import br.com.jess.chronos.pulse.modules.admin.infrastructure.adapters.output.persistence.AdminPlataformaRepositoryAdapter;
import br.com.jess.chronos.pulse.modules.admin.infrastructure.adapters.output.persistence.AdminPlataformaMapper;
import br.com.jess.chronos.pulse.modules.empresa.domain.ports.output.EmpresaRepositoryPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;

@Configuration
public class AdminModuleConfig {

    @Bean
    public CadastrarContratoUseCase cadastrarContratoUseCase(
            ContratoRepositoryPort contratoRepositoryPort,
            EmpresaRepositoryPort empresaRepositoryPort) {
        return new CadastrarContratoUseCaseImpl(contratoRepositoryPort, empresaRepositoryPort);
    }

    @Bean
    public ListarContratosUseCase listarContratosUseCase(ContratoRepositoryPort repositoryPort) {
        return new ListarContratosUseCaseImpl(repositoryPort);
    }

    @Bean
    public AdicionarEventoContratoUseCase adicionarEventoContratoUseCase(ContratoRepositoryPort repositoryPort) {
        return new AdicionarEventoContratoUseCaseImpl(repositoryPort);
    }

    @Bean
    public ListarEventosContratoUseCase listarEventosContratoUseCase(ContratoRepositoryPort repositoryPort) {
        return new ListarEventosContratoUseCaseImpl(repositoryPort);
    }

    @Bean
    public AtualizarSaldoContratoUseCase atualizarSaldoContratoUseCase(ContratoRepositoryPort repositoryPort) {
        return new AtualizarSaldoContratoUseCaseImpl(repositoryPort);
    }

    @Bean
    public DashboardMetricsUseCase dashboardMetricsUseCase(ContratoRepositoryPort repositoryPort) {
        return new DashboardMetricsUseCaseImpl(repositoryPort);
    }

    @Bean
    @ConditionalOnMissingBean
    public AdminPlataformaRepositoryPort adminPlataformaRepositoryPort(
            AdminPlataformaRepositoryAdapter repositoryAdapter) {
        return repositoryAdapter;
    }

    @Bean
    @ConditionalOnMissingBean
    public AdminRecoveryCodeRepositoryPort adminRecoveryCodeRepositoryPort(
            br.com.jess.chronos.pulse.modules.admin.infrastructure.adapters.output.persistence.AdminRecoveryCodeRepositoryAdapter repositoryAdapter) {
        return repositoryAdapter;
    }

    @Bean
    @Primary
    public AutenticarAdminPlataformaUseCase autenticarAdminPlataformaUseCase(
            AdminPlataformaRepositoryPort repositoryPort,
            org.springframework.security.crypto.password.PasswordEncoder passwordEncoder,
            br.com.jess.chronos.pulse.modules.auth.infrastructure.security.JwtService jwtService,
            @org.springframework.beans.factory.annotation.Value("${chronos.admin.two-factor-required:true}")
            boolean twoFactorRequired) {
        return new AutenticarAdminPlataformaUseCaseImpl(repositoryPort, passwordEncoder, jwtService, twoFactorRequired);
    }

    @Bean
    @Primary
    public VerificarTwoFactorAdminUseCase verificarTwoFactorAdminUseCase(
            AdminPlataformaRepositoryPort repositoryPort,
            br.com.jess.chronos.pulse.modules.auth.infrastructure.security.JwtService jwtService,
            br.com.jess.chronos.pulse.modules.admin.infrastructure.security.TotpService totpService) {
        return new VerificarTwoFactorAdminUseCaseImpl(repositoryPort, jwtService, totpService);
    }

    @Bean
    @Primary
    public GerenciarTwoFactorAdminUseCase gerenciarTwoFactorAdminUseCase(
            AdminPlataformaRepositoryPort repositoryPort,
            AdminRecoveryCodeRepositoryPort recoveryCodeRepositoryPort,
            br.com.jess.chronos.pulse.modules.admin.infrastructure.security.TotpService totpService,
            br.com.jess.chronos.pulse.modules.admin.infrastructure.security.AdminRecoveryCodeService recoveryCodeService) {
        return new GerenciarTwoFactorAdminUseCaseImpl(repositoryPort, recoveryCodeRepositoryPort, totpService, recoveryCodeService);
    }

    @Bean
    @Primary
    public AlterarSenhaAdminUseCase alterarSenhaAdminUseCase(
            AdminPlataformaRepositoryPort repositoryPort,
            org.springframework.security.crypto.password.PasswordEncoder passwordEncoder) {
        return new AlterarSenhaAdminUseCaseImpl(repositoryPort, passwordEncoder);
    }

    @Bean
    @Primary
    public BootstrapAdminUseCase bootstrapAdminUseCase(
            AdminPlataformaRepositoryPort repositoryPort,
            org.springframework.security.crypto.password.PasswordEncoder passwordEncoder,
            br.com.jess.chronos.pulse.modules.auth.infrastructure.security.JwtService jwtService) {
        return new BootstrapAdminUseCaseImpl(repositoryPort, passwordEncoder, jwtService);
    }

    @Bean
    @Primary
    public RecuperarAcessoAdminUseCase recuperarAcessoAdminUseCase(
            AdminPlataformaRepositoryPort repositoryPort,
            AdminRecoveryCodeRepositoryPort recoveryCodeRepositoryPort,
            org.springframework.security.crypto.password.PasswordEncoder passwordEncoder,
            br.com.jess.chronos.pulse.modules.auth.infrastructure.security.JwtService jwtService,
            br.com.jess.chronos.pulse.modules.admin.infrastructure.security.AdminRecoveryCodeService recoveryCodeService) {
        return new RecuperarAcessoAdminUseCaseImpl(repositoryPort, recoveryCodeRepositoryPort,
                passwordEncoder, jwtService, recoveryCodeService);
    }
}
