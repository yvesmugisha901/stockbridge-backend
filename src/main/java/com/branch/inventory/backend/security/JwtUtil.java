package com.branch.inventory.backend.security;

import com.branch.inventory.backend.model.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    /**
     * Access token lifetime — default 15 minutes.
     * Override in application.properties: jwt.expiration=900000
     */
    @Value("${jwt.expiration:900000}")
    private long expiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    // ── Token generation ──────────────────────────────────────────────────────

    /** Used by Spring Security internals (no extra claims). */
    public String generateToken(UserDetails userDetails) {
        return createToken(new HashMap<>(), userDetails.getUsername());
    }

    /** Used by AuthService at login — embeds role, fullName, branchId. */
    public String generateToken(UserDetails userDetails, User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", userDetails.getAuthorities().stream()
                .findFirst()
                .map(Object::toString)
                .orElse(""));
        claims.put("fullName", user.getFullName());
        claims.put("branchId", user.getBranch() != null ? user.getBranch().getId() : null);
        claims.put("branchName", user.getBranch() != null ? user.getBranch().getName() : null);
        return createToken(claims, userDetails.getUsername());
    }

    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }

    // ── Claim extraction ──────────────────────────────────────────────────────

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    public String extractFullName(String token) {
        return extractClaim(token, claims -> claims.get("fullName", String.class));
    }

    public Long extractBranchId(String token) {
        return extractClaim(token, claims -> claims.get("branchId", Long.class));
    }

    public String extractBranchName(String token) {
        return extractClaim(token, claims -> claims.get("branchName", String.class));
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        return claimsResolver.apply(extractAllClaims(token));
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // ── Validation ────────────────────────────────────────────────────────────

    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }
}
