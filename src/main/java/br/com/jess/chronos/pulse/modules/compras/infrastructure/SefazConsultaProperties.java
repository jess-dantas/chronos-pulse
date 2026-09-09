package br.com.jess.chronos.pulse.modules.compras.infrastructure;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuração opcional da consulta à SEFAZ (NFeDistDFeInt - Distribuição de DF-e).
 * Quando {@code consulta-enabled} for false (padrão), o recebimento mantém a
 * validação estrutural local e a importação manual do XML.
 */
@ConfigurationProperties(prefix = "app.compras.sefaz")
public record SefazConsultaProperties(
        boolean consultaEnabled,
        String endpoint,
        String cnpjDestinatario,
        boolean ambienteProducao,
        String certificadoCaminho,
        String certificadoSenha
) {
    public SefazConsultaProperties {
        if (endpoint == null) endpoint = "";
        if (cnpjDestinatario == null) cnpjDestinatario = "";
        if (certificadoCaminho == null) certificadoCaminho = "";
        if (certificadoSenha == null) certificadoSenha = "";
    }
}