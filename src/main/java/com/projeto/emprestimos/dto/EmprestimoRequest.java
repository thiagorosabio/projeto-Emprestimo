package com.projeto.emprestimos.dto;

import jakarta.validation.constraints.NotNull;

/** Lucas: o front-end envia apenas os identificadores; o Service encontra as entidades e aplica as regras. */
public record EmprestimoRequest(
        @NotNull(message = "é obrigatório") Long equipamentoId,
        @NotNull(message = "é obrigatório") Long colaboradorId
) { }
