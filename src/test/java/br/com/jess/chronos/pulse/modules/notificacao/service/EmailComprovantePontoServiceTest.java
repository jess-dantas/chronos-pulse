package br.com.jess.chronos.pulse.modules.notificacao.service;

import br.com.jess.chronos.pulse.modules.ponto.domain.model.RegistroPonto;
import br.com.jess.chronos.pulse.modules.ponto.domain.model.TipoRegistro;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailComprovantePontoServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private ObjectProvider<JavaMailSender> mailSenderProvider;

    private EmailComprovantePontoService service;

    @BeforeEach
    void setUp() {
        lenient().when(mailSenderProvider.getIfAvailable()).thenReturn(mailSender);
        service = new EmailComprovantePontoService(mailSenderProvider);
        ReflectionTestUtils.setField(service, "mailEnabled", true);
        ReflectionTestUtils.setField(service, "remetenteEmail", "jess.dantas.it@gmail.com");
        ReflectionTestUtils.setField(service, "remetenteNome", "Chronos Pulse");
    }

    private RegistroPonto criarRegistroPonto() {
        RegistroPonto reg = new RegistroPonto(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                Instant.now(),
                Instant.now(),
                TipoRegistro.ENTRADA,
                new BigDecimal("-23.550520"),
                new BigDecimal("-46.633308"),
                new BigDecimal("5.0"),
                null,
                false,
                1001L,
                false,
                null,
                null
        );
        reg.atribuirHash("a1b2c3d4e5f67890123456789abcdef0123456789abcdef0123456789abcdef0");
        return reg;
    }

    @Test
    void deveEnviarComprovantePorEmailComSucesso() {
        MimeMessage mimeMessage = new MimeMessage(Session.getInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        RegistroPonto reg = criarRegistroPonto();

        assertDoesNotThrow(() -> service.enviarComprovantePontoAsync(
                "colaborador@empresa.com.br",
                "Colaborador Teste",
                reg,
                "12345678901",
                "Chronos Pulse Tech LTDA"
        ));

        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void naoDeveEnviarEmailQuandoDesabilitado() {
        ReflectionTestUtils.setField(service, "mailEnabled", false);
        RegistroPonto reg = criarRegistroPonto();

        service.enviarComprovantePontoAsync(
                "colaborador@empresa.com.br",
                "Colaborador Teste",
                reg,
                "12345678901",
                "Chronos Pulse Tech LTDA"
        );

        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void naoDeveEnviarEmailQuandoDestinatarioNuloOuVazio() {
        RegistroPonto reg = criarRegistroPonto();

        service.enviarComprovantePontoAsync(
                "",
                "Colaborador Teste",
                reg,
                "12345678901",
                "Chronos Pulse Tech LTDA"
        );

        service.enviarComprovantePontoAsync(
                null,
                "Colaborador Teste",
                reg,
                "12345678901",
                "Chronos Pulse Tech LTDA"
        );

        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void deveCapturarExcecaoTratandoSilenciosamenteSemFalharAplicacao() {
        MimeMessage mimeMessage = new MimeMessage(Session.getInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doThrow(new RuntimeException("SMTP Timeout")).when(mailSender).send(any(MimeMessage.class));

        RegistroPonto reg = criarRegistroPonto();

        assertDoesNotThrow(() -> service.enviarComprovantePontoAsync(
                "colaborador@empresa.com.br",
                "Colaborador Teste",
                reg,
                "12345678901",
                "Chronos Pulse Tech LTDA"
        ));
    }
}
