package com.projeto.emprestimos.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/** Thiago: recebe CEP e número; logradouro, bairro, cidade e UF são preenchidos pelo ViaCEP no backend. */
public record ColaboradorRequest(
        @NotBlank(message = "é obrigatório") String nome,
        @NotBlank(message = "é obrigatório") @Email(message = "deve ser um e-mail válido") String email,
        @NotBlank(message = "é obrigatório") @Pattern(regexp = "\\d{8}", message = "deve conter exatamente 8 dígitos") String cep,
        @NotBlank(message = "é obrigatório") String numero
) { }
