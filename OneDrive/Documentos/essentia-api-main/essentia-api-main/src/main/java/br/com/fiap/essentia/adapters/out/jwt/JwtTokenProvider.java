package br.com.fiap.essentia.adapters.out.jwt;

import br.com.fiap.essentia.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.List;

@Component
public class JwtTokenProvider {

    private final Key key;
    private final long expirationMs;
    private final String issuer;

    public JwtTokenProvider(JwtProperties props) {
        byte[] keyBytes = Decoders.BASE64.decode(props.getSecret());
        if (keyBytes.length < 32) {
            throw new IllegalArgumentException("app.jwt.secret must decode to at least 32 bytes");
        }
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.expirationMs = props.getExpirationMs();
        this.issuer = props.getIssuer();
    }

    // Exemplo reduzido
    public String createToken(String userId) {
        var now = new Date();
        var exp = new Date(now.getTime() + expirationMs);
        return Jwts.builder()
                .setSubject(userId)
                .setIssuer(issuer)
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(key)
                .compact();
    }


    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public String getUserId(String token) {
        Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
        return claims.getSubject();
    }

    @SuppressWarnings("unchecked")
    public List<String> getRoles(String token) {
        Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
        return (List<String>) claims.get("roles");
    }
}
