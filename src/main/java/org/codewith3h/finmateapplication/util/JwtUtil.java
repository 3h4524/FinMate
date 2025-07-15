package org.codewith3h.finmateapplication.util;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codewith3h.finmateapplication.entity.User;
import org.codewith3h.finmateapplication.exception.AppException;
import org.codewith3h.finmateapplication.exception.ErrorCode;
import org.codewith3h.finmateapplication.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    private byte[] secretKey;
    private final Set<String> invalidatedTokens = new HashSet<>();

    private final UserRepository userRepository;


    public String generateToken(User user) throws JOSEException {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS256);

        JWTClaimsSet claimSet = new JWTClaimsSet.Builder()
                .subject(user.getId().toString())
                .claim("username", user.getName())
                .claim("scope", user.getRole())
                .issueTime(new Date())
                .expirationTime(new Date(System.currentTimeMillis() + expiration))
                .build();

        SignedJWT signedJWT = new SignedJWT(header, claimSet);

        JWSSigner signer = new MACSigner(secret.getBytes());

        signedJWT.sign(signer);

        return signedJWT.serialize();
    }

    public String generateTokenForExternalSystem() throws JOSEException {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS256);

        User admin = userRepository.findFirstByRole("ADMIN");

        JWTClaimsSet claimSet = new JWTClaimsSet.Builder()
                .claim("scope", admin.getRole())
                .issueTime(new Date())
                .expirationTime(new Date(System.currentTimeMillis() + expiration))
                .build();

        SignedJWT signedJWT = new SignedJWT(header, claimSet);
        JWSSigner signer = new MACSigner(secret.getBytes());

        signedJWT.sign(signer);

        return signedJWT.serialize();
    }


    public Integer extractId(String token) throws Exception {
        return Integer.parseInt(parseToken(token).getJWTClaimsSet().getSubject());
    }

    public String extractRole(String token) throws Exception {
        return parseToken(token).getJWTClaimsSet().getClaim("scope").toString();
    }

    public boolean validateToken(String token) throws Exception {
        SignedJWT signedJWT = SignedJWT.parse(token);
        boolean validSignature = signedJWT.verify(new MACVerifier(secret.getBytes()));
        boolean validExpirationTime = signedJWT.getJWTClaimsSet().getExpirationTime().after(new Date());

        return validSignature && validExpirationTime;
    }

    private SignedJWT parseToken(String token) throws Exception {
        SignedJWT signedJWT = SignedJWT.parse(token);
        if (!signedJWT.verify(new MACVerifier(secret.getBytes()))) {
            throw new AppException(ErrorCode.INVALID_TOKEN);
        }
        ;
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
