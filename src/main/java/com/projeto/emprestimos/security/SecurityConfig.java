package com.projeto.emprestimos.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Cleber e Thiago: login em memória, senha com BCrypt, autorização por perfil e filtro JWT.
 * Os usuários são fixos na configuração, como especificado no material do projeto.
 */
@Configuration
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(
            PasswordEncoder encoder,
            @Value("${usuarios.admin.usuario:admin}") String usuarioAdmin,
            @Value("${usuarios.admin.senha:admin123}") String senhaAdmin,
            @Value("${usuarios.colaborador.usuario:colaborador}") String usuarioColaborador,
            @Value("${usuarios.colaborador.senha:colaborador123}") String senhaColaborador) {
        return new InMemoryUserDetailsManager(
                User.withUsername(usuarioAdmin).password(encoder.encode(senhaAdmin)).roles("ADMIN").build(),
                User.withUsername(usuarioColaborador).password(encoder.encode(senhaColaborador)).roles("COLABORADOR").build()
        );
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, TokenService tokenService) throws Exception {
        FiltroJwt filtroJwt = new FiltroJwt(tokenService);

        http
            .csrf(csrf -> csrf.disable())
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Recursos da interface e ferramentas de documentação não exigem JWT.
                .requestMatchers("/", "/index.html", "/style.css", "/app.js", "/favicon.ico",
                        "/auth/login", "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", "/h2-console/**").permitAll()
                // A consulta de empréstimos do colaborador é liberada para qualquer usuário autenticado.
                .requestMatchers(HttpMethod.GET, "/colaboradores/*/emprestimos").authenticated()
                .requestMatchers("/colaboradores/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/equipamentos/**").authenticated()
                .requestMatchers("/equipamentos/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/emprestimos/**").authenticated()
                .requestMatchers("/emprestimos/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, erro) -> escreverErro(
                        response, HttpServletResponse.SC_UNAUTHORIZED, "Autenticação necessária"))
                .accessDeniedHandler((request, response, erro) -> escreverErro(
                        response, HttpServletResponse.SC_FORBIDDEN, "Você não tem permissão para esta operação"))
            )
            .addFilterBefore(filtroJwt, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    private void escreverErro(HttpServletResponse response, int status, String mensagem)
            throws java.io.IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        String mensagemSegura = mensagem.replace("\\", "\\\\").replace("\"", "\\\"");
        response.getWriter().write("{\"message\":\"" + mensagemSegura + "\"}");
    }
}
