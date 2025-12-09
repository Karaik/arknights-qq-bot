package com.karaik.gamebot.common.config;

import com.karaik.gamebot.auth.config.AuthProperties;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 为 Knife4j 文档全局追加 X-API-KEY 头，并给出默认值便于调试。
 * 使用 OpenApiCustomizer 避免构造注入 OpenAPI 导致的装配问题。
 */
@Configuration
public class Knife4jGlobalHeaderConfig {

    private static final String HEADER_NAME = "X-API-KEY";

    @Bean
    public OpenApiCustomizer globalApiKeyHeader(AuthProperties authProperties) {
        return openApi -> {
            String defaultKey = authProperties.getHypergryph().getApiKey();
            Parameter apiKeyHeader = new Parameter()
                    .name(HEADER_NAME)
                    .in("header")
                    .required(true)
                    .description("全局接口访问 Key，默认值用于本地调试")
                    .schema(new StringSchema()._default(defaultKey));

            if (openApi.getPaths() == null) {
                return;
            }
            openApi.getPaths().values().forEach(pathItem ->
                    pathItem.readOperations().forEach(operation -> {
                        boolean exists = operation.getParameters() != null && operation.getParameters().stream()
                                .anyMatch(p -> HEADER_NAME.equalsIgnoreCase(p.getName()));
                        if (!exists) {
                            operation.addParametersItem(apiKeyHeader);
                        }
                    })
            );
        };
    }
}
