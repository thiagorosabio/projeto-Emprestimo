package com.projeto.emprestimos.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

/** Cleber e Thiago: assina e valida JWT; o segredo vem de variável de ambiente em produção. */
@Service
public class TokenService {
    private final SecretKey chave;
    private final long validadeMs;

    public TokenService(@Value("${jwt.segredo}") String segredo,
                        @Value("${jwt.expiracao-ms:28800000}") long validadeMs) {
        if (segredo == null || segredo.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalArgumentException("JWT_SEGREDO precisa ter pelo menos 32 bytes.");
        }
        this.chave = Keys.hmacShaKeyFor(segredo.getBytes(StandardCharsets.UTF_8));
        this.validadeMs = validadeMs;
    }

    public String gerar(String usuario, String perfil) {
        Instant agora = Instant.now();
        return Jwts.builder()
                .subject(usuario)
                .claim("perfil", perfil)
                .claim("nome", usuario)
                .issuedAt(Date.from(agora))
                .expiration(Date.from(agora.plusMillis(validadeMs)))
                .signWith(chave, Jwts.SIG.HS256)
                .compact();
    }

    public Claims ler(String token) {
        return Jwts.parser()
                .verifyWith(chave)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
