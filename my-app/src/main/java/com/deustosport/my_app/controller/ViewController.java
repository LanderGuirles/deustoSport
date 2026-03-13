package com.deustosport.my_app.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.view.RedirectView;

@Controller
@Tag(name = "Health Check", description = "Endpoint para verificar que el API está activo")
public class ViewController {

    @GetMapping("/")
    @ResponseBody
    public String health() {
        return "{\"status\": \"API DeustoSport activa\", \"docs\": \"http://localhost:8080/docs/index.html\"}";
    }

    @GetMapping("/health")
    @ResponseBody
    public String healthCheck() {
        return "{\"status\": \"OK\", \"timestamp\": \"" + System.currentTimeMillis() + "\"}";
    }

    @GetMapping("/docs")
    public RedirectView docsRedirect() {
        return new RedirectView("/docs/index.html");
    }

    @GetMapping("/swagger-ui/index.html")
    public RedirectView legacySwaggerUiRedirect() {
        return new RedirectView("/docs/index.html");
    }
}
