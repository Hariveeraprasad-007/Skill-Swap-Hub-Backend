package com.skillswap.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Skill Swap Hub API",
                version = "1.0",
                description = """
                    Backend API for Skill Swap Hub — a peer-to-peer skill exchange platform.
                    
                    Features:
                    • User registration (email/password + Google OAuth2)
                    • Rich profiles with skills, certifications, experience, coding platform links
                    • Session booking with conflict detection
                    • Skill swap, free, or paid teaching modes
                    • Mutual feedback and ratings
                    • Real-time 1-to-1 chat via WebSocket
                    • Video/voice call signaling (WebRTC)
                    • Notifications (REST + WebSocket push)
                    • Admin dashboard
                    """,
                contact = @Contact(name = "Skill Swap Hub")
        ),
        security = @SecurityRequirement(name = "bearerAuth")
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class OpenApiConfig {
}
