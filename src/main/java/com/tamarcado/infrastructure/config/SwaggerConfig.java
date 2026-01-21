package com.tamarcado.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Tá Marcado! API")
                        .version("1.0.0")
                        .description("API Backend para o aplicativo Tá Marcado! - Plataforma de agendamento de serviços")
                        .contact(new Contact()
                                .name("Equipe Tá Marcado!")
                                .email("contato@tamarcado.com.br"))
                        .license(new License()
                                .name("Proprietary")
                                .url("https://tamarcado.com.br")))
                .servers(List.of(
                        new Server().url("http://localhost:8080/api/v1").description("Servidor de Desenvolvimento"),
                        new Server().url("https://api.tamarcado.com.br/v1").description("Servidor de Produção")
                ));
    }
}