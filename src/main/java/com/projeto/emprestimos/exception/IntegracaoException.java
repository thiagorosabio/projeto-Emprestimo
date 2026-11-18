package com.projeto.emprestimos.exception;

/** Thiago: falha de comunicação com ViaCEP, distinta de um CEP inexistente. */
public class IntegracaoException extends RuntimeException {
    public IntegracaoException(String mensagem, Throwable causa) { super(mensagem, causa); }
}
