package com.projeto.emprestimos.service;

import com.projeto.emprestimos.dto.ColaboradorRequest;
import com.projeto.emprestimos.dto.ViaCepResposta;
import com.projeto.emprestimos.exception.ConflitoException;
import com.projeto.emprestimos.exception.NaoEncontradoException;
import com.projeto.emprestimos.model.Colaborador;
import com.projeto.emprestimos.repository.ColaboradorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/** Lucas e Thiago: cadastra pessoas e utiliza a integração ViaCEP antes de salvar. */
@Service
public class ColaboradorService {
    private final ColaboradorRepository colaboradorRepository;
    private final ViaCepService viaCepService;

    public ColaboradorService(ColaboradorRepository colaboradorRepository, ViaCepService viaCepService) {
        this.colaboradorRepository = colaboradorRepository;
        this.viaCepService = viaCepService;
    }

    @Transactional(readOnly = true)
    public List<Colaborador> listar() {
        return colaboradorRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Colaborador buscar(Long id) {
        return colaboradorRepository.findById(id)
                .orElseThrow(() -> new NaoEncontradoException("Colaborador não encontrado"));
    }

    @Transactional
    public Colaborador cadastrar(ColaboradorRequest dados) {
        if (colaboradorRepository.existsByEmail(dados.email().trim().toLowerCase())) {
            throw new ConflitoException("E-mail já cadastrado");
        }

        // Thiago: os dados de endereço são copiados da resposta ViaCEP, não do corpo enviado pelo cliente.
        ViaCepResposta endereco = viaCepService.buscar(dados.cep());
        Colaborador colaborador = new Colaborador();
        colaborador.setNome(dados.nome().trim());
        colaborador.setEmail(dados.email().trim().toLowerCase());
        colaborador.setCep(dados.cep());
        colaborador.setNumero(dados.numero().trim());
        colaborador.setLogradouro(endereco.logradouro());
        colaborador.setBairro(endereco.bairro());
        colaborador.setCidade(endereco.localidade());
        colaborador.setUf(endereco.uf());
        return colaboradorRepository.save(colaborador);
    }
}
