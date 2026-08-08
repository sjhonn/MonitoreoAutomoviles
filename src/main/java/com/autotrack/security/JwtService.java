package com.autotrack.security;

import com.autotrack.entity.AppUser;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
    private final JwtEncoder jwtEncoder;
    private final Duration ttl;

    public JwtService(JwtEncoder jwtEncoder, @Value("${app.security.jwt.ttl-minutes:120}") long ttlMinutes) {
        this.jwtEncoder = jwtEncoder;
        this.ttl = Duration.ofMinutes(ttlMinutes);
    }

    public String issueToken(AppUser user) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("autotrack")
                .issuedAt(now)
                .expiresAt(now.plus(ttl))
                .subject(user.getEmail())
                .claim("name", user.getName())
                .claim("roles", List.of(user.getRole().name()))
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).type("JWT").build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    public long expiresInSeconds() {
        return ttl.toSeconds();
    }
}
