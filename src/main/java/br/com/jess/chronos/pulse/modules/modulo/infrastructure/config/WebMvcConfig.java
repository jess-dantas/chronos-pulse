package br.com.jess.chronos.pulse.modules.modulo.infrastructure.config;

import br.com.jess.chronos.pulse.modules.modulo.infrastructure.security.ModuloInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final ModuloInterceptor moduloInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(moduloInterceptor)
                .addPathPatterns("/api/v1/**");
    }
}