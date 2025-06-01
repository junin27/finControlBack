package fincontrol.com.fincontrol.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger; // Import SLF4J Logger
import org.slf4j.LoggerFactory; // Import SLF4J LoggerFactory
import org.springframework.lang.NonNull; // Adicionando @NonNull se estiver usando Spring Framework 5+
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JWTAuthenticationFilter extends OncePerRequestFilter {

    // Adicione uma instância do Logger
    private static final Logger log = LoggerFactory.getLogger(JWTAuthenticationFilter.class);

    private final JwtTokenProvider tokenProvider;

    public JWTAuthenticationFilter(JwtTokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        final String requestURI = request.getRequestURI();
        log.info("JWTAuthFilter: Iniciando filtro para URI: {}", requestURI);

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            log.info("JWTAuthFilter: Token recebido: '{}' (URI: {})", token, requestURI); // Cuidado ao logar tokens em produção

            if (tokenProvider.validateToken(token)) {
                log.info("JWTAuthFilter: Token VALIDADO com sucesso. (URI: {})", requestURI);
                String userIdString = tokenProvider.getUserIdFromToken(token);
                log.info("JWTAuthFilter: UserIDString extraído do token: '{}' (URI: {})", userIdString, requestURI);

                if (userIdString != null && !userIdString.isEmpty()) {
                    // Usar o userIdString (String do UUID) como o principal.
                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(
                                    userIdString, // O principal agora é a String do UUID do usuário
                                    null, // Credentials (não necessárias aqui, pois o token já foi validado)
                                    Collections.emptyList()); // Authorities (papéis) - ajuste se necessário

                    SecurityContextHolder.getContext().setAuthentication(auth);
                    log.info("JWTAuthFilter: Usuário com ID '{}' autenticado e SecurityContext atualizado. (URI: {})", userIdString, requestURI);
                } else {
                    log.warn("JWTAuthFilter: UserIDString é nulo ou vazio após extração do token validado. (URI: {})", requestURI);
                }
            } else {
                log.warn("JWTAuthFilter: FALHA na VALIDAÇÃO do token. (URI: {})", requestURI);
                // O SecurityContext não será modificado, e a requisição pode prosseguir como anônima
                // ou ser barrada por outra configuração de segurança mais adiante se a rota for protegida.
            }
        } else {
            log.warn("JWTAuthFilter: Cabeçalho Authorization ausente ou não começa com 'Bearer '. (URI: {})", requestURI);
        }

        filterChain.doFilter(request, response);
        log.debug("JWTAuthFilter: Saindo do filtro para URI: {}", requestURI);
    }
}