package com.projeto.emprestimos.dto;

import jakarta.validation.constraints.NotBlank;

/** Cleber e Thiago: dados mínimos recebidos pela rota pública de autenticação. */
public record LoginRequest(
        @NotBlank(message = "é obrigatório") String usuario,
        @NotBlank(message = "é obrigatório") String senha
) { }
