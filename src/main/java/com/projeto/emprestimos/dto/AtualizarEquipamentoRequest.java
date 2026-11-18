package com.projeto.emprestimos.dto;

import com.projeto.emprestimos.model.TipoEquipamento;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Lucas: o contrato PUT permite alterar somente nome e tipo, como definido no projeto. */
public record AtualizarEquipamentoRequest(
        @NotBlank(message = "é obrigatório") String nome,
        @NotNull(message = "é obrigatório") TipoEquipamento tipo
) { }
