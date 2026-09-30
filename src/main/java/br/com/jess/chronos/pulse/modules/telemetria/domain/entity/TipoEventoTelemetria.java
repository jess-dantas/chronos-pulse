package br.com.jess.chronos.pulse.modules.telemetria.domain.entity;

public enum TipoEventoTelemetria {
    LOGIN_SUCESSO,
    LOGIN_FALHA,
    API_REQUEST,
    API_ERRO,
    UI_ERRO,
    CONEXAO_BD,
    // Eventos de app (contingência / modo dispositivo) — coluna STRING, sem migration.
    CONEXAO_OFFLINE,
    CONTINGENCIA,
    MODO_DISPOSITIVO
}