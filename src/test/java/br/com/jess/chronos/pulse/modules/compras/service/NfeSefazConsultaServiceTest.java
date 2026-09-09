package br.com.jess.chronos.pulse.modules.compras.service;

import br.com.jess.chronos.pulse.modules.compras.infrastructure.SefazConsultaProperties;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NfeSefazConsultaServiceTest {

    @Test
    void consultaDesabilitadaRetornaVazio() {
        NfeSefazConsultaService service = new NfeSefazConsultaService(
                new SefazConsultaProperties(false, null, null, false, null, null));
        Optional<?> resultado = service.consultarPorChave("35250922334455000190550010000000011000000087");
        assertTrue(resultado.isEmpty());
    }

    @Test
    void consultaHabilitadaSemEndpointLanca() {
        NfeSefazConsultaService service = new NfeSefazConsultaService(
                new SefazConsultaProperties(true, "  ", null, false, null, null));
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.consultarPorChave("35250922334455000190550010000000011000000087"));
        assertTrue(ex.getMessage().contains("endpoint"));
    }
}