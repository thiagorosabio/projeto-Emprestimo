package com.projeto.emprestimos.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Thiago: representa somente os campos necessários da resposta externa do ViaCEP. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ViaCepResposta(
        String logradouro,
        String bairro,
        String localidade,
        String uf,
        Boolean erro
) { }
