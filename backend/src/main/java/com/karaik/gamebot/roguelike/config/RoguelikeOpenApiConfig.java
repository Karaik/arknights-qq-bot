package com.karaik.gamebot.roguelike.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.security.SecuritySchemes;
import org.springframework.context.annotation.Configuration;

/**
 * 定义肉鸽 API 使用的 Header 鉴权描述，方便 Swagger/OpenAPI 文档引用。
 */
@Configuration
@SecuritySchemes(value = {
        @SecurityScheme(
                name = "roguelikeApiKey",
                description = "调用所有肉鸽接口都需要在 Header 中附带 `X-API-KEY`",
                type = SecuritySchemeType.APIKEY,
                paramName = "X-API-KEY",
                in = SecuritySchemeIn.HEADER
        ),
        @SecurityScheme(
                name = "sklandToken",
                description = "调用绑定接口时需在 Header 中提供森空岛 `X-SKLAND-TOKEN`",
                type = SecuritySchemeType.APIKEY,
                paramName = "X-SKLAND-TOKEN",
                in = SecuritySchemeIn.HEADER
        )
})
public class RoguelikeOpenApiConfig {
}
