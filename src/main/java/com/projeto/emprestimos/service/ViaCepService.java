package com.projeto.emprestimos.service;

import com.projeto.emprestimos.dto.ViaCepResposta;
import com.projeto.emprestimos.exception.CepNaoEncontradoException;
import com.projeto.emprestimos.exception.IntegracaoException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/** Thiago: consulta o ViaCEP no servidor para preencher o endereço sem depender do navegador. */
@Service
public class ViaCepService {
    private final RestClient restClient;

    public ViaCepService(@Value("${viacep.url:https://viacep.com.br/ws}") String url) {
        // Thiago: cria o cliente diretamente para não depender de um bean RestClient.Builder.
        // Isso evita a falha de inicialização quando o starter MVC não registra esse builder.
        this.restClient = RestClient.builder()
                .baseUrl(url)
                .build();
    }

    public ViaCepResposta buscar(String cep) {
        try {
            ViaCepResposta resposta = restClient.get()
                    .uri("/{cep}/json/", cep)
                    .retrieve()
                    .body(ViaCepResposta.class);

            if (resposta == null || Boolean.TRUE.equals(resposta.erro())) {
                throw new CepNaoEncontradoException("CEP não encontrado");
            }
            return resposta;
        } catch (CepNaoEncontradoException erro) {
            throw erro;
        } catch (RestClientException erro) {
            throw new IntegracaoException("Não foi possível consultar o ViaCEP. Tente novamente.", erro);
        }
    }
}
