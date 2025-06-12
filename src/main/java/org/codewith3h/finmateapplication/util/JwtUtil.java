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

    private byte[] secretKey;
    private final Set<String> invalidatedTokens = new HashSet<>();


    public String generateToken(String email, String role) throws JOSEException {
        // Tao header
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS256);

        JWTClaimsSet claimSet = new JWTClaimsSet.Builder()
                .subject(email)
                .claim("scope", role)
                .issueTime(new Date())
                .expirationTime(new Date(System.currentTimeMillis() + expiration * 1000))
                .build();


        SignedJWT signedJWT = new SignedJWT(header, claimSet);

        JWSSigner signer = new MACSigner(secret.getBytes());

        signedJWT.sign(signer);

        return signedJWT.serialize();
    }

    public String extractEmail(String token) throws Exception {
        return parseToken(token).getJWTClaimsSet().getSubject();
    }

    public String extractRole(String token) throws Exception {
        return parseToken(token).getJWTClaimsSet().getClaim("scope").toString();
    }

    public boolean validateToken(String token) throws Exception{
        SignedJWT signedJWT = SignedJWT.parse(token);
        boolean signatureValid = signedJWT.verify( new MACVerifier(secret.getBytes()));
        boolean notExpired = signedJWT.getJWTClaimsSet().getExpirationTime().after(new Date());

        return signatureValid && notExpired;
    }

    private SignedJWT parseToken(String token) throws Exception {
        SignedJWT signedJWT = SignedJWT.parse(token);
        if(!signedJWT.verify(new MACVerifier(secret.getBytes()))){
            throw new  Exception("Invalid JWT token");
        };
        return signedJWT;
    }

    public void invalidateToken(String token) {
        try {
            invalidatedTokens.add(token);
            log.info("Token invalidated successfully");
        } catch (Exception e) {
            log.error("Error invalidating token: {}", e.getMessage());
        }
    }
}
