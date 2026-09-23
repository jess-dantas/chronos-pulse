package br.com.jess.chronos.pulse.modules.modulo.infrastructure.security;

import br.com.jess.chronos.pulse.modules.auth.domain.model.CpcUsuario;
import br.com.jess.chronos.pulse.modules.auth.domain.model.Role;
import br.com.jess.chronos.pulse.modules.modulo.domain.ports.output.ModulosPort;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class ModuloInterceptor implements HandlerInterceptor {

    private final ModulosPort modulosPort;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        String codigo = resolverCodigoModulo(handlerMethod);
        if (codigo == null) {
            return true;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof CpcUsuario usuario)) {
            return true;
        }
        if (isPerfilPlataforma(usuario) || usuario.getRole() == Role.ADMIN_EMPRESA || usuario.getTenantId() == null) {
            return true;
        }
        if (!modulosPort.usuarioModuloAtivo(usuario.getId(), usuario.getTenantId(), codigo)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "O módulo " + codigo + " não está liberado para este usuário.");
        }
        return true;
    }

    private String resolverCodigoModulo(HandlerMethod handlerMethod) {
        RequiresModulo methodAnnotation = handlerMethod.getMethodAnnotation(RequiresModulo.class);
        if (methodAnnotation != null) {
            String codigo = extrairCodigo(methodAnnotation);
            if (codigo != null) {
                return codigo;
            }
        }
        RequiresModulo classAnnotation = AnnotationUtils.findAnnotation(handlerMethod.getBeanType(), RequiresModulo.class);
        return classAnnotation != null ? extrairCodigo(classAnnotation) : null;
    }

    private String extrairCodigo(RequiresModulo annotation) {
        if (annotation.codigo() != null && !annotation.codigo().isBlank()) {
            return annotation.codigo();
        }
        if (annotation.value() != null && !annotation.value().isBlank()) {
            return annotation.value();
        }
        return null;
    }

    private boolean isPerfilPlataforma(CpcUsuario usuario) {
        Role role = usuario.getRole();
        return role == Role.ADMIN_PLATAFORMA || role == Role.SUPORTE_N1 || role == Role.SUPORTE_N2;
    }
}