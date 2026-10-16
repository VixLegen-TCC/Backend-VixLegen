package br.com.VixLegen.ProjetoVixLegen10.Config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ObservabilidadeConfig implements WebMvcConfigurer {
    private final AuditoriaHttpInterceptor auditoria;

    public ObservabilidadeConfig(AuditoriaHttpInterceptor auditoria) {
        this.auditoria = auditoria;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(auditoria).addPathPatterns("/**");
    }
}
