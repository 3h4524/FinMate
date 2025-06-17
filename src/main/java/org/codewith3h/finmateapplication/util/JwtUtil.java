package org.codewith3h.finmateapplication.util;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Component
@Slf4j
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    private final Set<String> invalidatedTokens = new HashSet<>();

    public String generateToken(String email, String role, Integer userId) throws JOSEException {
        if (secret == null || secret.isEmpty()) {
            throw new IllegalStateException("JWT secret key is not configured");
        }

        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

        JWTClaimsSet claimSet = new JWTClaimsSet.Builder()
                .subject(email)
                .claim("scope", role)
                .claim("userId", userId)
                .issueTime(new Date())
                .expirationTime(new Date(System.currentTimeMillis() + expiration * 1000))
                .build();

        SignedJWT signedJWT = new SignedJWT(header, claimSet);
        JWSSigner signer = new MACSigner(secret.getBytes());
        signedJWT.sign(signer);

        return signedJWT.serialize();
    }

    public String generateToken(String email, String role) throws JOSEException {
        return generateToken(email, role, null);
    }

    public String extractEmail(String token) throws Exception {
        return parseToken(token).getJWTClaimsSet().getSubject();
    }

    public String extractRole(String token) throws Exception {
        return parseToken(token).getJWTClaimsSet().getClaim("scope").toString();
    }

    public Integer extractId(String token) throws Exception {
        Object userIdClaim = parseToken(token).getJWTClaimsSet().getClaim("userId");
        if (userIdClaim == null) {
            return null;
        }
        if (userIdClaim instanceof Integer) {
            return (Integer) userIdClaim;
        }
        if (userIdClaim instanceof Long) {
            return ((Long) userIdClaim).intValue();
        }
        if (userIdClaim instanceof String) {
            return Integer.parseInt((String) userIdClaim);
        }
        return null;
    }

    public boolean validateToken(String token) throws Exception {
        if (token == null || token.isEmpty()) {
            return false;
        }

        if (invalidatedTokens.contains(token)) {
            return false;
        }

        if (secret == null || secret.isEmpty()) {
            throw new IllegalStateException("JWT secret key is not configured");
        }

        SignedJWT signedJWT = SignedJWT.parse(token);
        boolean signatureValid = signedJWT.verify(new MACVerifier(secret.getBytes()));
        boolean notExpired = signedJWT.getJWTClaimsSet().getExpirationTime().after(new Date());

        return signatureValid && notExpired;
    }

    private SignedJWT parseToken(String token) throws Exception {
        if (token == null || token.isEmpty()) {
            throw new IllegalArgumentException("Token cannot be null or empty");
        }

        if (secret == null || secret.isEmpty()) {
            throw new IllegalStateException("JWT secret key is not configured");
        }

        SignedJWT signedJWT = SignedJWT.parse(token);
        if (!signedJWT.verify(new MACVerifier(secret.getBytes()))) {
            throw new Exception("Invalid JWT token");
        }

        return signedJWT;
    }

    public void invalidateToken(String token) {
        if (token != null && !token.isEmpty()) {
            invalidatedTokens.add(token);
        }
    }
}
