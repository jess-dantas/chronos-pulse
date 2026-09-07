package br.com.jess.chronos.pulse.modules.modulo.infrastructure.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marca um controller (ou método) que somente é acessível quando a empresa
 * (tenant) do usuário autenticado tiver o módulo contratado/ativo.
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresModulo {

    String codigo();
}