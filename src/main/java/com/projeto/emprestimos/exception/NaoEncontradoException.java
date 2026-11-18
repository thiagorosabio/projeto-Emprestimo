package com.projeto.emprestimos.exception;

/** Lucas: representa uma busca por equipamento, colaborador ou empréstimo inexistente. */
public class NaoEncontradoException extends RuntimeException {
    public NaoEncontradoException(String mensagem) { super(mensagem); }
}
