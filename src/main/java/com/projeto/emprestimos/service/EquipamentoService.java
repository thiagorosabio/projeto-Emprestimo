package com.projeto.emprestimos.service;

import com.projeto.emprestimos.dto.AtualizarEquipamentoRequest;
import com.projeto.emprestimos.dto.EquipamentoRequest;
import com.projeto.emprestimos.exception.ConflitoException;
import com.projeto.emprestimos.exception.NaoEncontradoException;
import com.projeto.emprestimos.model.Equipamento;
import com.projeto.emprestimos.model.TipoEquipamento;
import com.projeto.emprestimos.repository.EquipamentoRepository;
import com.projeto.emprestimos.repository.EmprestimoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/** Lucas (tech lead): concentra as operações e regras relacionadas aos equipamentos. */
@Service
public class EquipamentoService {
    private final EquipamentoRepository equipamentoRepository;
    private final EmprestimoRepository emprestimoRepository;

    public EquipamentoService(EquipamentoRepository equipamentoRepository,
                              EmprestimoRepository emprestimoRepository) {
        this.equipamentoRepository = equipamentoRepository;
        this.emprestimoRepository = emprestimoRepository;
    }

    @Transactional(readOnly = true)
    public List<Equipamento> listar(TipoEquipamento tipo, Boolean disponivel) {
        if (tipo != null && disponivel != null) {
            return equipamentoRepository.findByTipoAndDisponivel(tipo, disponivel);
        }
        if (tipo != null) return equipamentoRepository.findByTipo(tipo);
        if (disponivel != null) return equipamentoRepository.findByDisponivel(disponivel);
        return equipamentoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Equipamento buscar(Long id) {
        return equipamentoRepository.findById(id)
                .orElseThrow(() -> new NaoEncontradoException("Equipamento não encontrado"));
    }

    @Transactional
    public Equipamento cadastrar(EquipamentoRequest dados) {
        if (equipamentoRepository.existsByPatrimonio(dados.patrimonio())) {
            throw new ConflitoException("Patrimônio já cadastrado");
        }
        // Lucas: disponibilidade inicial é definida no servidor, não pelo navegador.
        Equipamento equipamento = new Equipamento(dados.patrimonio().trim(), dados.nome().trim(), dados.tipo());
        equipamento.setDisponivel(true);
        return equipamentoRepository.save(equipamento);
    }

    @Transactional
    public Equipamento atualizar(Long id, AtualizarEquipamentoRequest dados) {
        Equipamento equipamento = buscar(id);
        equipamento.setNome(dados.nome().trim());
        equipamento.setTipo(dados.tipo());
        return equipamentoRepository.save(equipamento);
    }

    @Transactional
    public void remover(Long id) {
        Equipamento equipamento = buscar(id);
        // Lucas: preserva o histórico; o banco não deve apagar item referenciado por empréstimo.
        if (emprestimoRepository.existsByEquipamentoId(id)) {
            throw new ConflitoException("Equipamento tem empréstimos registrados");
        }
        equipamentoRepository.delete(equipamento);
    }
}
