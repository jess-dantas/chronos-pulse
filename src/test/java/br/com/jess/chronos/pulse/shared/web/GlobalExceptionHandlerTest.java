package br.com.jess.chronos.pulse.shared.web;

import org.junit.jupiter.api.Test;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void deveRetornar400ParaIllegalArgumentComMensagem() {
        var resposta = handler.handleIllegalArgument(
                new IllegalArgumentException("Colaborador não encontrado no seu tenant"));

        assertThat(resposta.getStatusCode().value()).isEqualTo(400);
        assertThat(resposta.getBody().status()).isEqualTo(400);
        assertThat(resposta.getBody().mensagem()).contains("Colaborador não encontrado");
    }

    @Test
    void deveRetornar500ParaErroGenerico() {
        var resposta = handler.handleGeneric(new IllegalStateException("boom"),
                new org.springframework.mock.web.MockHttpServletRequest("GET", "/api/v1/teste"));

        assertThat(resposta.getStatusCode().value()).isEqualTo(500);
        assertThat(resposta.getBody().status()).isEqualTo(500);
        assertThat(resposta.getBody().mensagem()).contains("interno");
    }

    @Test
    void deveRetornar409ParaViolacaoDeIntegridade() {
        var resposta = handler.handleDataIntegrity(
                new org.springframework.dao.DataIntegrityViolationException("duplicidade"));

        assertThat(resposta.getStatusCode().value()).isEqualTo(409);
        assertThat(resposta.getBody().status()).isEqualTo(409);
    }

    @Test
    void deveRetornar400ComCamposParaValidacao() {
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, new org.springframework.validation.BeanPropertyBindingResult(new Object(), "alvo"));
        var resposta = handler.handleValidation(ex);

        assertThat(resposta.getStatusCode().value()).isEqualTo(400);
        assertThat(resposta.getBody().campos()).isEmpty();
    }

    @Test
    void deveRetornar500ParaFalhaDeAcessoADados() {
        var resposta = handler.handleDataAccess(
                new org.springframework.dao.DataAccessResourceFailureException("falha",
                        new java.sql.SQLException("conexão recusada", "08001")));

        assertThat(resposta.getStatusCode().value()).isEqualTo(500);
        assertThat(resposta.getBody().status()).isEqualTo(500);
    }
}