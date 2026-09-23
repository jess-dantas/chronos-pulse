package br.com.jess.chronos.pulse.modules.notificacao.service;

import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailCodigoTitularidadeService {

    private static final Logger log =
            LoggerFactory.getLogger(EmailCodigoTitularidadeService.class);

    private final ObjectProvider<JavaMailSender> mailSenderProvider;

    @Value("${chronos.mail.enabled:true}")
    private boolean mailEnabled;

    @Value("${chronos.mail.from:jess.dantas.it@gmail.com}")
    private String remetenteEmail;

    @Value("${chronos.mail.remetente-nome:Chronos Pulse}")
    private String remetenteNome;

    public EmailCodigoTitularidadeService(ObjectProvider<JavaMailSender> mailSenderProvider) {
        this.mailSenderProvider = mailSenderProvider;
    }

    /**
     * Envia o código OTP da transferência de titularidade. Com o envio de
     * e-mail desabilitado (dev/teste), o código é registrado em log para
     * permitir o smoke test do wizard sem SMTP configurado.
     */
    @Async
    public void enviarCodigoAsync(String destinatarioEmail, String codigo) {
        if (!mailEnabled) {
            log.info("[titularidade] Envio de e-mail desabilitado. "
                    + "Código de transferência para {}: {}", destinatarioEmail, codigo);
            return;
        }

        if (destinatarioEmail == null || destinatarioEmail.trim().isEmpty()) {
            log.warn("Destinatário sem e-mail cadastrado. Código de titularidade não enviado.");
            return;
        }

        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            log.warn("JavaMailSender não está disponível. E-mail para {} ignorado.", destinatarioEmail);
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(remetenteEmail, remetenteNome);
            helper.setTo(destinatarioEmail);
            helper.setSubject("Transferencia de titularidade - Chronos Pulse");
            helper.setText(gerarTextoPlano(codigo), gerarHtml(codigo));

            mailSender.send(message);
            log.info("Código de transferência de titularidade enviado para {}", destinatarioEmail);
        } catch (Exception e) {
            log.warn("Falha ao enviar código de titularidade para {}: {}",
                    destinatarioEmail, e.getMessage());
        }
    }

    private String gerarTextoPlano(String codigo) {
        return """
            ====================================================
            CHRONOS PULSE - TRANSFERENCIA DE TITULARIDADE
            ====================================================

            Seu codigo de confirmacao e: %s

            O codigo expira em 15 minutos.
            Se voce nao solicitou a transferencia de titularidade, ignore este e-mail.

            ====================================================
            """.formatted(codigo);
    }

    private String gerarHtml(String codigo) {
        return """
            <!DOCTYPE html>
            <html lang="pt-BR">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Transferencia de Titularidade - Chronos Pulse</title>
                <style>
                    body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; background-color: #F8FAFC; margin: 0; padding: 20px; color: #1E293B; }
                    .card { max-width: 600px; margin: 0 auto; background: #FFFFFF; border-radius: 12px; border: 1px solid #E2E8F0; overflow: hidden; box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05); }
                    .header { background: linear-gradient(135deg, #0F172A 0%%, #1E293B 100%%); padding: 24px; text-align: center; color: #FFFFFF; }
                    .header h1 { margin: 0; font-size: 20px; font-weight: 700; letter-spacing: 0.5px; }
                    .header p { margin: 4px 0 0 0; font-size: 13px; color: #94A3B8; }
                    .content { padding: 28px 24px; text-align: center; }
                    .codigo { display: inline-block; background: #F1F5F9; border: 2px dashed #CBD5E1; border-radius: 8px; padding: 16px 32px; font-family: monospace; font-size: 32px; font-weight: 700; letter-spacing: 6px; color: #0F172A; margin: 16px 0; }
                    .aviso { font-size: 13px; color: #64748B; }
                    .footer { background-color: #F8FAFC; padding: 16px 24px; text-align: center; font-size: 11px; color: #64748B; border-top: 1px solid #E2E8F0; }
                </style>
            </head>
            <body>
                <div class="card">
                    <div class="header">
                        <h1>CHRONOS PULSE</h1>
                        <p>Transferencia de Titularidade</p>
                    </div>
                    <div class="content">
                        <h2 style="font-size: 16px; margin: 0 0 8px 0; color: #0F172A;">Seu codigo de confirmacao</h2>
                        <div class="codigo">%s</div>
                        <p class="aviso">O codigo expira em 15 minutos.<br>Se voce nao solicitou a transferencia de titularidade, ignore este e-mail.</p>
                    </div>
                    <div class="footer">
                        Este e um e-mail automatico gerado pelo Chronos Pulse.
                    </div>
                </div>
            </body>
            </html>
            """.formatted(codigo);
    }
}
