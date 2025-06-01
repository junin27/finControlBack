package fincontrol.com.fincontrol.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID; // IMPORTAÇÃO ADICIONADA

@Service
public class JwtTokenProvider {

    private static final String BASE64_SECRET = "c3VwZXJTZWNyZXRhU2VjcmV0YU1lbkRlU2VndXJhbmNhMTIzNDU2Nzg5MA==";
    private static final SecretKey SECRET_KEY = Keys.hmacShaKeyFor(Decoders.BASE64.decode(BASE64_SECRET));
    private static final long EXPIRATION_MS = 24 * 60 * 60 * 1000; // 1 dia

    // Modificado para aceitar userId e adicioná-lo como claim
    public String generateToken(String username, UUID userId) {
        Date now = new Date();
        return Jwts.builder()
                .setSubject(username) // Mantém o username (email) como subject
                .claim("userId", userId.toString()) // Adiciona o userId como uma claim customizada
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + EXPIRATION_MS))
                .signWith(SECRET_KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    // NOVO MÉTODO para obter o userId da claim
    public String getUserIdFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.get("userId", String.class); // Extrai a claim "userId" como String
    }

    public boolean validateToken(String token) {
        try {
            Jws<Claims> claimsJws = Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY)
                    .build()
                    .parseClaimsJws(token);
            return !claimsJws.getBody().getExpiration().before(new Date());
        } catch (JwtException | IllegalArgumentException ex) {
            // Idealmente, logar a exceção aqui para depuração
            // ex.printStackTrace(); // ou use um logger
            return false;
        }
    }
}