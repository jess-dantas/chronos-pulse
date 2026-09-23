package br.com.jess.chronos.pulse.modules.titularidade.domain.ports.input;

import java.util.UUID;

public interface TransferirTitularidadeUseCase {

    String ETAPA_CELULAR = "CELULAR";
    String ETAPA_EMAIL = "EMAIL";

    record IniciarComando(UUID solicitanteId, UUID tenantId, UUID novoTitularId) {}

    record BiometriaComando(UUID transferenciaId, UUID solicitanteId, UUID tenantId,
                            boolean confirmado) {}

    record EnviarCodigoComando(UUID transferenciaId, UUID solicitanteId, UUID tenantId,
                               String etapa) {}

    record VerificarCodigoComando(UUID transferenciaId, UUID solicitanteId, UUID tenantId,
                                  String etapa, String codigo, Boolean celularConfirmado) {}

    record Comando(UUID transferenciaId, UUID solicitanteId, UUID tenantId) {}

    record Iniciado(UUID transferenciaId, String novoTitularNome, String novoTitularCelular) {}

    record CodigoEnviado(String destino) {}

    Iniciado iniciar(IniciarComando comando);

    void confirmarBiometria(BiometriaComando comando);

    CodigoEnviado enviarCodigo(EnviarCodigoComando comando);

    void verificarCodigo(VerificarCodigoComando comando);

    void concluir(Comando comando);

    void cancelar(Comando comando);
}
