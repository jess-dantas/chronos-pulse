package br.com.jess.chronos.pulse.modules.auth.domain.service;

import br.com.jess.chronos.pulse.modules.auth.domain.model.Role;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PasswordPolicyTest {

    @Test
    void deveAceitarSenhaValidaDeColaborador() {
        assertThat(PasswordPolicy.validar("senha123", Role.COLABORADOR)).isEmpty();
    }

    @Test
    void deveRejeitarSenhaCurtaDeColaborador() {
        assertThat(PasswordPolicy.validar("abc12", Role.COLABORADOR))
                .hasValueSatisfying(m -> assertThat(m).contains("mínimo 6 caracteres"));
    }

    @Test
    void deveRejeitarSenhaNulaOuEmBranco() {
        assertThat(PasswordPolicy.validar(null, Role.COLABORADOR)).isPresent();
        assertThat(PasswordPolicy.validar("   ", Role.GESTOR_RH)).isPresent();
    }

    @Test
    void deveExigirComplexidadeParaGestor() {
        assertThat(PasswordPolicy.validar("Senha@123", Role.ADMIN_EMPRESA)).isEmpty();
    }

    @Test
    void deveRejeitarSenhaDeGestorSemMaiusculas() {
        assertThat(PasswordPolicy.validar("ab@12cdef", Role.GESTOR_RH))
                .hasValueSatisfying(m -> assertThat(m).contains("maiúsculas"));
    }

    @Test
    void deveRejeitarSenhaDeGestorSemNumero() {
        assertThat(PasswordPolicy.validar("Abcdefgh@", Role.ADMIN_PLATAFORMA))
                .hasValueSatisfying(m -> assertThat(m).contains("números"));
    }

    @Test
    void deveRejeitarSenhaDeGestorSemSimbolo() {
        assertThat(PasswordPolicy.validar("Abcde12345", Role.SUPORTE_N2))
                .hasValueSatisfying(m -> assertThat(m).contains("símbolos"));
    }

    @Test
    void deveRejeitarSenhaCurtaDeGestor() {
        assertThat(PasswordPolicy.validar("A1@2a", Role.ADMIN_PLATAFORMA))
                .hasValueSatisfying(m -> assertThat(m).contains("mínimo 8 caracteres"));
    }
}