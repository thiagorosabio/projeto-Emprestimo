package com.projeto.emprestimos.exception;

/** Lucas: representa uma operação incompatível com o estado atual dos dados (HTTP 409). */
public class ConflitoException extends RuntimeException {
    public ConflitoException(String mensagem) { super(mensagem); }
}
