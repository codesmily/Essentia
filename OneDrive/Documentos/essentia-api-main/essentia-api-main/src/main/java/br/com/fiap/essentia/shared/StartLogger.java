package br.com.fiap.essentia.shared;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class StartLogger implements ApplicationListener<ApplicationReadyEvent> {

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        if (event.getApplicationContext() instanceof ServletWebServerApplicationContext ctx) {
            int port = ctx.getWebServer().getPort();

            System.out.println("\n==================================================");
            System.out.println(" 📖 Swagger UI: http://localhost:" + port + "/swagger-ui/index.html");
            System.out.println(" 📑 OpenAPI JSON: http://localhost:" + port + "/v3/api-docs");
            System.out.println("==================================================\n");
        }
    }
}
