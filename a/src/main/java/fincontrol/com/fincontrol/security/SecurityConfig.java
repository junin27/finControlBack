package fincontrol.com.fincontrol.security;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final JWTAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtTokenProvider jwtTokenProvider,
                          JWTAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
          // 1) Ativa CORS com a configuração abaixo
          .cors().and()
          // 2) Desabilita CSRF
          .csrf(AbstractHttpConfigurer::disable)
          // 3) Configura permissões de endpoint
          .authorizeHttpRequests(auth -> auth
              .requestMatchers(
                  "/v3/api-docs/**",
                  "/swagger-ui.html",
                  "/swagger-ui/**",
                  "/swagger-ui/index.html"
              ).permitAll()
              .requestMatchers("/auth/**").permitAll()
              .requestMatchers("/actuator/health").permitAll()
              .requestMatchers("/", "/ping").permitAll()
              .anyRequest().authenticated()
          )
          // 4) Injeta o filtro JWT
          .addFilterBefore(jwtAuthenticationFilter,
              UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // Bean que libera CORS para todas as origens, métodos e headers
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        // em vez de allowedOrigins("*"):
        cfg.setAllowedOriginPatterns(List.of("*"));
        cfg.setAllowedMethods(List.of("*"));
        cfg.setAllowedHeaders(List.of("*"));
        cfg.setAllowCredentials(true);
    
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
