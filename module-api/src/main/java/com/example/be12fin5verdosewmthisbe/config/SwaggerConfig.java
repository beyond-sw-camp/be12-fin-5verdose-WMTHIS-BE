package com.example.be12fin5verdosewmthisbe.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("WMTHIS API 문서")
                        .version("1.0")
                        .description("WMTHIS 프로젝트의 API 문서입니다.")
                        .contact(new Contact()
                                .name("WMTHIS 개발팀")
                                .email("support@wmthis.com")
                        ));
    }

}
