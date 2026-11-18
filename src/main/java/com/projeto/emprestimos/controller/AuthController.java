package com.projeto.emprestimos.controller;

import com.projeto.emprestimos.dto.LoginRequest;
import com.projeto.emprestimos.dto.TokenResposta;
import com.projeto.emprestimos.security.TokenService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

/** Cleber e Thiago: valida as credenciais no Spring Security e emite o JWT de acesso. */
@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;

    public AuthController(AuthenticationManager authenticationManager, TokenService tokenService) {
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
    }

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public TokenResposta login(@Valid @RequestBody LoginRequest dados) {
        Authentication autenticacao = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dados.usuario(), dados.senha()));

        String perfil = autenticacao.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(autoridade -> autoridade.startsWith("ROLE_"))
                .map(autoridade -> autoridade.substring("ROLE_".length()))
                .findFirst()
                .orElse("COLABORADOR");

        String token = tokenService.gerar(dados.usuario(), perfil);
        return new TokenResposta(token, perfil, dados.usuario());
    }
}
