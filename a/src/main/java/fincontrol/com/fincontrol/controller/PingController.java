package fincontrol.com.fincontrol.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PingController {

    /**
     * Endpoint público para checagem rápida de saúde.
     */
    @GetMapping("/ping")
    public String ping() {
        return "pong";
    }

    /**
     * Página inicial pública.
     */
    @GetMapping("/")
    public String home() {
        return "Bem-vindo ao FinControl API!";
    }
}
