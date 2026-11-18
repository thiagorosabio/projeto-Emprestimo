package com.projeto.emprestimos.dto;

/** Cleber e Thiago: token JWT e dados simples do usuário para a interface exibir o perfil. */
public record TokenResposta(String token, String perfil, String usuario) { }
