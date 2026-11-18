package com.projeto.emprestimos.exception;

/** Thiago: CEP com formato correto, mas não localizado pelo serviço ViaCEP. */
public class CepNaoEncontradoException extends RuntimeException {
    public CepNaoEncontradoException(String mensagem) { super(mensagem); }
}
