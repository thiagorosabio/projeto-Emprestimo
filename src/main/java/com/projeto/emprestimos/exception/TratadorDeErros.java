package com.projeto.emprestimos.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import java.util.stream.Collectors;

/**
 * Lara (QA): centraliza os erros para que cada rota responda com status correto e JSON {message}.
 * Thiago: também converte falhas do ViaCEP em mensagens compreensíveis.
 * Cleber: converte falha de autenticação do login em HTTP 401.
 */
@RestControllerAdvice
public class TratadorDeErros {

    @ExceptionHandler(NaoEncontradoException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErroResposta naoEncontrado(NaoEncontradoException erro) {
        return new ErroResposta(erro.getMessage());
    }

    @ExceptionHandler(ConflitoException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErroResposta conflito(ConflitoException erro) {
        return new ErroResposta(erro.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErroResposta validacao(MethodArgumentNotValidException erro) {
        String mensagem = erro.getBindingResult().getFieldErrors().stream()
                .map(campo -> campo.getField() + ": " + campo.getDefaultMessage())
                .distinct()
                .collect(Collectors.joining("; "));
        if (mensagem.isBlank()) mensagem = "Revise os campos enviados.";
        return new ErroResposta(mensagem);
    }

    @ExceptionHandler(CepNaoEncontradoException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErroResposta cepNaoEncontrado(CepNaoEncontradoException erro) {
        return new ErroResposta(erro.getMessage());
    }

    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErroResposta loginInvalido(AuthenticationException erro) {
        return new ErroResposta("Usuário ou senha inválidos");
    }

    @ExceptionHandler(IntegracaoException.class)
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    public ErroResposta integracao(IntegracaoException erro) {
        return new ErroResposta(erro.getMessage());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErroResposta tipoInvalido(MethodArgumentTypeMismatchException erro) {
        return new ErroResposta("Parâmetro inválido: " + erro.getName());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErroResposta jsonInvalido(HttpMessageNotReadableException erro) {
        return new ErroResposta("JSON inválido ou tipo de equipamento não reconhecido.");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErroResposta integridade(DataIntegrityViolationException erro) {
        return new ErroResposta("Já existe um registro com esses dados ou há empréstimos vinculados.");
    }
}
