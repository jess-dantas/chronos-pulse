package br.com.jess.chronos.pulse.modules.notificacao.service;

import br.com.jess.chronos.pulse.modules.ponto.domain.model.RegistroPonto;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Service
public class EmailComprovantePontoService {

    private static final Logger log = LoggerFactory.getLogger(EmailComprovantePontoService.class);
    private static final DateTimeFormatter FORMATTER_DATA_HORA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss").withZone(ZoneId.of("America/Sao_Paulo"));

    private final ObjectProvider<JavaMailSender> mailSenderProvider;

    @Value("${chronos.mail.enabled:true}")
    private boolean mailEnabled;

    @Value("${chronos.mail.from:jess.dantas.it@gmail.com}")
    private String remetenteEmail;

    @Value("${chronos.mail.remetente-nome:Chronos Pulse}")
    private String remetenteNome;

    public EmailComprovantePontoService(ObjectProvider<JavaMailSender> mailSenderProvider) {
        this.mailSenderProvider = mailSenderProvider;
    }

    @Async
    public void enviarComprovantePontoAsync(String destinatarioEmail,
                                           String destinatarioNome,
                                           RegistroPonto registro,
                                           String cpfColaborador,
                                           String empresaNome) {
        if (!mailEnabled) {
            log.info("Envio de e-mail desabilitado por configuração. Comprovante para {} não enviado.", destinatarioEmail);
            return;
        }

        if (destinatarioEmail == null || destinatarioEmail.trim().isEmpty()) {
            log.warn("Destinatário sem e-mail cadastrado. Comprovante de ponto não enviado.");
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

            String tipoNome = registro.getTipoRegistro() != null ? registro.getTipoRegistro().name() : "PONTO";
            String dataHoraFormatada = registro.getDataHora() != null
                    ? FORMATTER_DATA_HORA.format(registro.getDataHora())
                    : "Data/Hora não informada";

            String assunto = String.format("[Chronos Pulse] Comprovante de Registro de Ponto - %s (%s)", tipoNome, dataHoraFormatada);
            helper.setSubject(assunto);

            String htmlCorpo = gerarHtmlComprovante(destinatarioNome, cpfColaborador, registro, dataHoraFormatada, empresaNome);
            String textoPlano = gerarTextoPlanoComprovante(destinatarioNome, cpfColaborador, registro, dataHoraFormatada, empresaNome);

            helper.setText(textoPlano, htmlCorpo);

            mailSender.send(message);
            log.info("Comprovante de ponto enviado com sucesso por e-mail para {}", destinatarioEmail);
        } catch (Exception e) {
            log.warn("Falha ao enviar comprovante de ponto por e-mail para {}: {}", destinatarioEmail, e.getMessage());
        }
    }

    private String formatarCpf(String cpf) {
        if (cpf == null) return "Não informado";
        String digits = cpf.replaceAll("\\D", "");
        if (digits.length() == 11) {
            return digits.substring(0, 3) + "." + digits.substring(3, 6) + "." + digits.substring(6, 9) + "-" + digits.substring(9);
        }
        return cpf;
    }

    private String getCorBadgeTipo(String tipo) {
        if (tipo == null) return "#0284C7";
        return switch (tipo.toUpperCase()) {
            case "ENTRADA" -> "#16A34A";
            case "INTERVALO" -> "#D97706";
            case "RETORNO" -> "#2563EB";
            case "SAIDA" -> "#9333EA";
            default -> "#0284C7";
        };
    }

    private String gerarHtmlComprovante(String nome, String cpf, RegistroPonto reg, String dataHoraStr, String empresa) {
        String tipo = reg.getTipoRegistro() != null ? reg.getTipoRegistro().name() : "N/A";
        String corBadge = getCorBadgeTipo(tipo);
        String cpfFormatado = formatarCpf(cpf);
        String nsrStr = reg.getNsr() != null ? String.valueOf(reg.getNsr()) : "Pendente";
        String hashStr = reg.getHash() != null ? reg.getHash() : "N/A";
        String modoRegistro = Boolean.TRUE.equals(reg.getAjusteManual())
                ? "Ajuste Manual (" + (reg.getJustificativa() != null ? reg.getJustificativa() : "Sem justificativa") + ")"
                : (Boolean.TRUE.equals(reg.getSincronizadoOffline()) ? "Sincronizado Offline" : "Online (Em tempo real)");

        return """
            <!DOCTYPE html>
            <html lang="pt-BR">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Comprovante de Ponto - Chronos Pulse</title>
                <style>
                    body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; background-color: #F8FAFC; margin: 0; padding: 20px; color: #1E293B; }
                    .card { max-width: 600px; margin: 0 auto; background: #FFFFFF; border-radius: 12px; border: 1px solid #E2E8F0; overflow: hidden; box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05); }
                    .header { background: linear-gradient(135deg, #0F172A 0%%, #1E293B 100%%); padding: 24px; text-align: center; color: #FFFFFF; }
                    .header h1 { margin: 0; font-size: 20px; font-weight: 700; letter-spacing: 0.5px; }
                    .header p { margin: 4px 0 0 0; font-size: 13px; color: #94A3B8; }
                    .content { padding: 28px 24px; }
                    .badge-container { text-align: center; margin-bottom: 24px; }
                    .badge { display: inline-block; background-color: %s; color: #FFFFFF; font-weight: 700; font-size: 14px; padding: 6px 18px; border-radius: 20px; text-transform: uppercase; letter-spacing: 1px; }
                    .info-table { width: 100%%; border-collapse: collapse; margin-bottom: 24px; }
                    .info-table tr { border-bottom: 1px solid #F1F5F9; }
                    .info-table td { padding: 12px 6px; font-size: 14px; }
                    .info-table td.label { font-weight: 600; color: #64748B; width: 35%%; }
                    .info-table td.value { font-weight: 500; color: #0F172A; text-align: right; }
                    .hash-box { background: #F1F5F9; border-radius: 8px; padding: 12px; font-family: monospace; font-size: 11px; word-break: break-all; color: #475569; margin-bottom: 24px; border: 1px dashed #CBD5E1; }
                    .footer { background-color: #F8FAFC; padding: 16px 24px; text-align: center; font-size: 11px; color: #64748B; border-top: 1px solid #E2E8F0; }
                </style>
            </head>
            <body>
                <div class="card">
                    <div class="header">
                        <h1>CHRONOS PULSE</h1>
                        <p>Gestão Eletrônica de Ponto &middot; Portaria MTP nº 671/2021</p>
                    </div>
                    <div class="content">
                        <div class="badge-container">
                            <span class="badge">%s</span>
                        </div>
                        <h2 style="font-size: 16px; margin: 0 0 16px 0; text-align: center; color: #0F172A;">Comprovante de Registro de Ponto do Trabalhador</h2>
                        <table class="info-table">
                            <tr>
                                <td class="label">Colaborador</td>
                                <td class="value">%s</td>
                            </tr>
                            <tr>
                                <td class="label">CPF</td>
                                <td class="value">%s</td>
                            </tr>
                            <tr>
                                <td class="label">Data e Hora</td>
                                <td class="value"><strong>%s</strong></td>
                            </tr>
                            <tr>
                                <td class="label">NSR (Sequencial)</td>
                                <td class="value">#%s</td>
                            </tr>
                            <tr>
                                <td class="label">Modalidade</td>
                                <td class="value">%s</td>
                            </tr>
                            <tr>
                                <td class="label">Identificador</td>
                                <td class="value" style="font-family: monospace; font-size: 12px;">%s</td>
                            </tr>
                        </table>
                        <div style="font-size: 12px; font-weight: 600; color: #64748B; margin-bottom: 6px;">Código Hash de Autenticidade (SHA-256):</div>
                        <div class="hash-box">%s</div>
                    </div>
                    <div class="footer">
                        Este é um comprovante digital gerado automaticamente pelo Chronos Pulse.<br>
                        Válido para fins de auditoria e conformidade com a legislação trabalhista brasileira.
                    </div>
                </div>
            </body>
            </html>
            """.formatted(
                corBadge,
                tipo,
                nome != null ? nome : "Colaborador",
                cpfFormatado,
                dataHoraStr,
                nsrStr,
                modoRegistro,
                reg.getId() != null ? reg.getId().toString() : "N/A",
                hashStr
        );
    }

    private String gerarTextoPlanoComprovante(String nome, String cpf, RegistroPonto reg, String dataHoraStr, String empresa) {
        String tipo = reg.getTipoRegistro() != null ? reg.getTipoRegistro().name() : "N/A";
        String cpfFormatado = formatarCpf(cpf);
        String nsrStr = reg.getNsr() != null ? String.valueOf(reg.getNsr()) : "Pendente";
        String hashStr = reg.getHash() != null ? reg.getHash() : "N/A";

        return String.format("""
            ====================================================
            CHRONOS PULSE - COMPROVANTE DE REGISTRO DE PONTO
            Portaria MTP nº 671/2021
            ====================================================
            
            Tipo de Marcação: %s
            Colaborador: %s
            CPF: %s
            Data e Hora: %s
            NSR: %s
            ID do Registro: %s
            Hash SHA-256: %s
            
            ----------------------------------------------------
            Este é um comprovante digital emitido eletronicamente.
            ====================================================
            """,
                tipo,
                nome != null ? nome : "Colaborador",
                cpfFormatado,
                dataHoraStr,
                nsrStr,
                reg.getId() != null ? reg.getId().toString() : "N/A",
                hashStr
        );
    }
}
