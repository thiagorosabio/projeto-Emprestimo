package com.projeto.emprestimos.dto;

import com.projeto.emprestimos.model.TipoEquipamento;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Lucas: impede que o cliente defina disponibilidade; um equipamento novo nasce disponível. */
public record EquipamentoRequest(
        @NotBlank(message = "é obrigatório") String patrimonio,
        @NotBlank(message = "é obrigatório") String nome,
        @NotNull(message = "é obrigatório") TipoEquipamento tipo
) { }
