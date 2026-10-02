package br.com.VixLegen.ProjetoVixLegen10.Config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;

@Configuration
public class SecurityConfig {

    private static final String ISSUER = "vixlegen-api";

    @Bean
    public SecretKey jwtSecretKey(
            @Value("${jwt.secret}") String secret) {

        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);

        if (bytes.length < 32) {
            throw new IllegalStateException(
                    "JWT_SECRET deve possuir pelo menos 32 caracteres"
            );
        }

        return new SecretKeySpec(bytes, "HmacSHA256");
    }

    @Bean
    public JwtEncoder jwtEncoder(SecretKey secretKey) {
        return NimbusJwtEncoder
                .withSecretKey(secretKey)
                .algorithm(MacAlgorithm.HS256)
                .build();
    }

    @Bean
    public JwtDecoder jwtDecoder(SecretKey secretKey) {

        NimbusJwtDecoder decoder = NimbusJwtDecoder
                .withSecretKey(secretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();

        decoder.setJwtValidator(
                JwtValidators.createDefaultWithIssuer(ISSUER)
        );

        return decoder;
    }

    @Bean
    public Converter<Jwt, ? extends AbstractAuthenticationToken>
    jwtAuthenticationConverter() {

        JwtGrantedAuthoritiesConverter scopeConverter =
                new JwtGrantedAuthoritiesConverter();

        JwtAuthenticationConverter converter =
                new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(jwt -> {

            Collection<GrantedAuthority> authorities =
                    new ArrayList<>(
                            scopeConverter.convert(jwt)
                    );

            String role = jwt.getClaimAsString("role");

            if (role != null && !role.isBlank()) {
                authorities.add(
                        new SimpleGrantedAuthority(
                                "ROLE_" + role
                        )
                );
            }

            return authorities;
        });

        return converter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            Converter<Jwt, ? extends AbstractAuthenticationToken>
                    jwtAuthenticationConverter) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )
                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(
                                HttpMethod.OPTIONS,
                                "/**"
                        ).permitAll()

                        .requestMatchers(
                                HttpMethod.POST,
                                "/auth/login"
                        ).permitAll()

                        .requestMatchers(
                                "/error"
                        ).permitAll()

                        // Administração de categorias e permissões:
                        // somente Administrador Geral.
                        .requestMatchers(
                                "/categorias/**"
                        ).hasRole("ADMIN")

                        // Usuários:
                        // Administrador Geral gerencia;
                        // Advogado Sênior apenas consulta.
                        .requestMatchers(
                                HttpMethod.GET,
                                "/usuarios/**"
                        ).hasAnyRole(
                                "ADMIN",
                                "ADVOGADO_SENIOR"
                        )
                        .requestMatchers(
                                "/usuarios/**"
                        ).hasRole("ADMIN")

                        // Recursos jurídicos comuns seguem
                        // as permissões booleanas da Categoria.
                        .requestMatchers(
                                HttpMethod.GET,
                                "/**"
                        ).hasAuthority("SCOPE_visualizar")

                        .requestMatchers(
                                HttpMethod.POST,
                                "/**"
                        ).hasAuthority("SCOPE_editar")

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/**"
                        ).hasAuthority("SCOPE_editar")

                        .requestMatchers(
                                HttpMethod.PATCH,
                                "/**"
                        ).hasAuthority("SCOPE_editar")

                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/**"
                        ).hasAuthority("SCOPE_excluir")

                        .anyRequest()
                        .authenticated()
                )
                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(jwt ->
                                jwt.jwtAuthenticationConverter(
                                        jwtAuthenticationConverter
                                )
                        )
                );

        return http.build();
    }
}
