package fincontrol.com.fincontrol.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger; // Import SLF4J Logger
import org.slf4j.LoggerFactory; // Import SLF4J LoggerFactory
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtTokenProvider {

    // Adicione uma instância do Logger
    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    private static final String BASE64_SECRET = "c3VwZXJTZWNyZXRhU2VjcmV0YU1lbkRlU2VndXJhbmNhMTIzNDU2Nzg5MA==";
    private static final SecretKey SECRET_KEY = Keys.hmacShaKeyFor(Decoders.BASE64.decode(BASE64_SECRET));
    private static final long EXPIRATION_MS = 24 * 60 * 60 * 1000; // 1 dia

    public String generateToken(String username, UUID userId) {
        Date now = new Date();
        String token = Jwts.builder()
                .setSubject(username)
                .claim("userId", userId.toString())
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + EXPIRATION_MS))
                .signWith(SECRET_KEY, SignatureAlgorithm.HS256)
                .compact();
        logger.info("Token gerado para userId: {}", userId); // Log da geração do token
        return token;
    }

    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    public String getUserIdFromToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            String userId = claims.get("userId", String.class);
            if (userId == null) {
                logger.warn("Claim 'userId' não encontrada ou é nula no token.");
            }
            return userId;
        } catch (JwtException e) {
            logger.error("Erro ao extrair userId do token: {} - {}", e.getClass().getSimpleName(), e.getMessage());
            return null; // Retorna null se houver erro na extração (token inválido)
        }
    }

    public boolean validateToken(String token) {
        try {
            Jws<Claims> claimsJws = Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY)
                    .build()
                    .parseClaimsJws(token);
            boolean isTokenExpired = claimsJws.getBody().getExpiration().before(new Date());
            if (isTokenExpired) {
                logger.warn("Token expirado: {}", token); // Cuidado ao logar o token inteiro em produção
            }
            return !isTokenExpired;
        } catch (io.jsonwebtoken.security.SignatureException ex) { // Específico para problemas de assinatura
            logger.warn("Validação do Token falhou: Assinatura inválida - {}", ex.getMessage());
        } catch (MalformedJwtException ex) {
            logger.warn("Validação do Token falhou: Token malformado - {}", ex.getMessage());
        } catch (ExpiredJwtException ex) {
            logger.warn("Validação do Token falhou: Token expirado - {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            logger.warn("Validação do Token falhou: Token não suportado - {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            logger.warn("Validação do Token falhou: Argumento ilegal (claims string vazia) - {}", ex.getMessage());
        } catch (JwtException ex) { // Captura genérica para outros erros JwtException
            logger.warn("Validação do Token falhou: Erro JWT Genérico - {} - {}", ex.getClass().getSimpleName(), ex.getMessage());
        }
        return false;
    }
}