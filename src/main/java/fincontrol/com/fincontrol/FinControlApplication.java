package fincontrol.com.fincontrol;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(
		info = @Info(
				title       = "FinControl API",
				version     = "v1",
				description = "API de Autenticação, Usuários e Categorias"
		)
)
@SecurityScheme(
		name   = "bearerAuth",
		type   = SecuritySchemeType.HTTP,
		scheme = "bearer",
		bearerFormat = "JWT"
)
public class FinControlApplication {
	public static void main(String[] args) {
		SpringApplication.run(FinControlApplication.class, args);
	}
}
