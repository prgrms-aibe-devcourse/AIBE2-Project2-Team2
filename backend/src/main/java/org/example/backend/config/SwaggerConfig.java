package org.example.backend.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@SecurityScheme(
        name = "Authorization",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        Paths paths = new Paths();
        paths.addPathItem("/api/auth/login", new PathItem().post(
                new io.swagger.v3.oas.models.Operation()
                        .operationId("login")
                        .summary("로그인 (JwtLoginFilter 직접 처리)")
                        .requestBody(new RequestBody().content(new Content().addMediaType("application/json",
                                new MediaType().schema(new Schema<Map<String, String>>()
                                        .example(Map.of(
                                                "password", "Password@123",
                                                "email", "example@naver.com"
                                        ))
                                ))))
                        .responses(new io.swagger.v3.oas.models.responses.ApiResponses()
                                .addApiResponse("200", new ApiResponse().description("로그인 성공"))
                                .addApiResponse("401", new ApiResponse().description("이메일 또는 비밀번호 오류")))

        ));

        return new OpenAPI()
                .info(new Info()
                        .title("team2 몰디브 API 문서")
                        .description("Spring Boot 기반 REST API 명세서")
                        .version("v1.0.0"))
                .addSecurityItem(new SecurityRequirement().addList("Authorization")) // 🔐 인증 필요하다고 선언
                .paths(paths); // 로그인 경로 등록
    }
}