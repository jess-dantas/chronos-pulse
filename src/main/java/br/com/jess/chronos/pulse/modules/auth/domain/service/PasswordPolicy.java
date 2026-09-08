package br.com.jess.chronos.pulse.modules.auth.domain.service;

import br.com.jess.chronos.pulse.modules.auth.domain.model.Role;

import java.util.Optional;

public final class PasswordPolicy {

    public static final int MIN_COLABORADOR = 6;
    public static final int MIN_GESTOR = 8;

    private static final String REGEX_MAIUSCULA = ".*[A-Z].*";
    private static final String REGEX_MINUSCULA = ".*[a-z].*";
    private static final String REGEX_DIGITO = ".*\\d.*";
    private static final String REGEX_SIMBOLO = ".*[^A-Za-z0-9].*";

    private PasswordPolicy() {
    }

    public static Optional<String> validar(String senha, Role role) {
        if (senha == null || senha.isBlank()) {
            return Optional.of("A senha é obrigatória.");
        }

        boolean gestor = role != null && role != Role.COLABORADOR;

        if (senha.length() < (gestor ? MIN_GESTOR : MIN_COLABORADOR)) {
            return Optional.of(gestor
                    ? "A senha do gestor deve ter no mínimo " + MIN_GESTOR + " caracteres."
                    : "A senha deve ter no mínimo " + MIN_COLABORADOR + " caracteres.");
        }

        if (gestor && !atendeComplexidade(senha)) {
            return Optional.of("A senha do gestor deve conter letras maiúsculas, "
                    + "minúsculas, números e símbolos.");
        }

        return Optional.empty();
    }

    private static boolean atendeComplexidade(String senha) {
        return senha.matches(REGEX_MAIUSCULA)
                && senha.matches(REGEX_MINUSCULA)
                && senha.matches(REGEX_DIGITO)
                && senha.matches(REGEX_SIMBOLO);
    }
}