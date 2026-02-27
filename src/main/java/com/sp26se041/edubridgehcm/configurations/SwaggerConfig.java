package com.sp26se041.edubridgehcm.configurations;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;

@OpenAPIDefinition(
        info = @Info(
                title = "EduBridgeHCM",
                version = "1.0",
                description = "API for dev EduBridgeHCM"
        ),

        servers = {
                @Server(
                        description = "Local host",
                        url = "http://localhost:8080/"
                ),

                @Server(
                        description = "Server",
                        url = ""
                )
        }
)
public class SwaggerConfig {
}
