package com.carebridge.backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtService {

    public static final long TOKEN_EXPIRATION_MS =
        ChronoUnit.HOURS.getDuration().toMillis() * 24;

    private static final String SECRET_STRING =
        "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";

    private final SecretKey SECRET =
        io.jsonwebtoken.security.Keys.hmacShaKeyFor(SECRET_STRING.getBytes());

    public String generateToken(String username,
                                Collection<? extends GrantedAuthority> authorities) {
        String primaryRole = authorities.stream()
            .map(GrantedAuthority::getAuthority)
            .filter(a -> a.startsWith("ROLE_") || !a.contains("_"))
            .findFirst()
            .map(a -> a.replace("ROLE_", ""))
            .orElse("PATIENT");

        List<String> permissions = authorities.stream()
            .map(GrantedAuthority::getAuthority)
            .filter(a -> !a.startsWith("ROLE_"))
            .collect(Collectors.toList());

        Date now = new Date();
        return Jwts.builder()
            .subject(username)
            .claim("role", primaryRole)
            .claim("perms", permissions)
            .issuedAt(now)
            .expiration(new Date(now.getTime() + TOKEN_EXPIRATION_MS))
            .signWith(SECRET)
            .compact();
    }

    public boolean validate(String token) {
        try {
            return !isTokenExpired(token);
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String getEmailFromToken(String token) {
        return extractClaims(token).getSubject();
    }

    public String getRoleFromToken(String token) {
        return extractClaims(token).get("role", String.class);
    }

    @SuppressWarnings("unchecked")
    public List<String> getPermissionsFromToken(String token) {
        Object raw = extractClaims(token).get("perms");
        if (raw instanceof List<?> list) {
            return list.stream().map(Object::toString).collect(Collectors.toList());
        }
        return List.of();
    }

    private Claims extractClaims(String token) throws JwtException {
        return Jwts.parser()
            .verifyWith(SECRET)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    private boolean isTokenExpired(String token) {
        return extractClaims(token).getExpiration().before(new Date());
    }
}
