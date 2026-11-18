package com.projeto.emprestimos.security;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Filipe: documenta o sistema e configura o botão Authorize para testar JWT no Swagger. */
@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI documentacaoApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Além das Barreiras — Sistema de Empréstimos de Equipamentos")
                        .description("API do Squad 2 para cadastro, empréstimo e devolução de equipamentos.")
                        .version("1.0.0"))
                .components(new Components().addSecuritySchemes("BearerAuth",
                        new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")));
    }
}
