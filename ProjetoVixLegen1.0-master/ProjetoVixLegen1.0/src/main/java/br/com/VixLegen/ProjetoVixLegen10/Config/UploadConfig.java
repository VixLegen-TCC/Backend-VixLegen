package br.com.VixLegen.ProjetoVixLegen10.Config;

import jakarta.servlet.MultipartConfigElement;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UploadConfig {
    private static final long MB = 1024L * 1024L;

    @Bean
    public MultipartConfigElement multipartConfigElement() {
        // Substitui o limite padrão (geralmente 1 MB) sem modificar secrets do application.properties.
        return new MultipartConfigElement("", 10 * MB, 11 * MB, 0);
    }
}
