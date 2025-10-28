package com.avatar.avatar_online.config;

import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import jakarta.servlet.ServletRegistration;

@Configuration
public class WebConfig {

    @Bean
    public WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> enableLoadOnStartup() {
        return factory -> {
            factory.addInitializers(servletContext -> {

                // 1. Obtém o registro do DispatcherServlet
                ServletRegistration registration = servletContext.getServletRegistration("dispatcherServlet");

                if (registration instanceof ServletRegistration.Dynamic dynamicRegistration) {
                    // 2. Realiza o cast seguro para acessar o método setLoadOnStartup

                    // 3. Aplica o Load-on-Startup
                    dynamicRegistration.setLoadOnStartup(1);

                    System.out.println("✅ Warm-up forçado: setLoadOnStartup(1) aplicado ao DispatcherServlet.");
                }
            });
        };
    }
}