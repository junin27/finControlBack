package fincontrol.com.fincontrol;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

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
@EnableJpaAuditing
@EnableAspectJAutoProxy
public class FinControlApplication {
	public static void main(String[] args) {
		System.out.println(">> DB URL = " + System.getenv("SPRING_DATASOURCE_URL"));
		SpringApplication.run(FinControlApplication.class, args);
	}
}
