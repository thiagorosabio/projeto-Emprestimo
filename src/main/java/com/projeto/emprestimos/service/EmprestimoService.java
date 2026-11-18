package com.projeto.emprestimos.service;

import com.projeto.emprestimos.dto.EmprestimoRequest;
import com.projeto.emprestimos.exception.ConflitoException;
import com.projeto.emprestimos.exception.NaoEncontradoException;
import com.projeto.emprestimos.model.Colaborador;
import com.projeto.emprestimos.model.Emprestimo;
import com.projeto.emprestimos.model.Equipamento;
import com.projeto.emprestimos.repository.EmprestimoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;

/** Lucas: regras de empréstimo e devolução ficam no Service, nunca na interface web. */
@Service
public class EmprestimoService {
    private final EmprestimoRepository emprestimoRepository;
    private final EquipamentoService equipamentoService;
    private final ColaboradorService colaboradorService;

    public EmprestimoService(EmprestimoRepository emprestimoRepository,
                            EquipamentoService equipamentoService,
                            ColaboradorService colaboradorService) {
        this.emprestimoRepository = emprestimoRepository;
        this.equipamentoService = equipamentoService;
        this.colaboradorService = colaboradorService;
    }

    @Transactional(readOnly = true)
    public List<Emprestimo> listar(Boolean atrasados) {
        // Sem parâmetro: histórico completo. true/false: filtra pela mesma regra de Emprestimo.isAtrasado().
        if (atrasados == null) {
            return emprestimoRepository.findAll();
        }
        LocalDate hoje = LocalDate.now();
        return atrasados
                ? emprestimoRepository.findByDataDevolucaoIsNullAndDataPrevistaDevolucaoBefore(hoje)
                : emprestimoRepository.findByDataDevolucaoIsNotNullOrDataPrevistaDevolucaoGreaterThanEqual(hoje);
    }

    @Transactional(readOnly = true)
    public Emprestimo buscar(Long id) {
        return emprestimoRepository.findById(id)
                .orElseThrow(() -> new NaoEncontradoException("Empréstimo não encontrado"));
    }

    @Transactional(readOnly = true)
    public List<Emprestimo> listarDoColaborador(Long colaboradorId) {
        colaboradorService.buscar(colaboradorId);
        return emprestimoRepository.findByColaboradorId(colaboradorId);
    }

    @Transactional
    public Emprestimo emprestar(EmprestimoRequest dados) {
        // Reaproveita os métodos buscar para manter as respostas 404 no mesmo padrão.
        Equipamento equipamento = equipamentoService.buscar(dados.equipamentoId());
        Colaborador colaborador = colaboradorService.buscar(dados.colaboradorId());

        // Lucas: R1 — a API é a autoridade final sobre a disponibilidade do equipamento.
        if (!equipamento.isDisponivel()) {
            throw new ConflitoException("Equipamento indisponível");
        }

        equipamento.setDisponivel(false);
        Emprestimo emprestimo = new Emprestimo();
        emprestimo.setEquipamento(equipamento);
        emprestimo.setColaborador(colaborador);
        emprestimo.setDataEmprestimo(LocalDate.now());
        emprestimo.setDataPrevistaDevolucao(LocalDate.now().plusDays(14));
        emprestimo.setDataDevolucao(null);
        return emprestimoRepository.save(emprestimo);
    }

    @Transactional
    public Emprestimo devolver(Long id) {
        Emprestimo emprestimo = buscar(id);
        // Lucas: R2 — impede registrar uma segunda devolução para o mesmo empréstimo.
        if (emprestimo.getDataDevolucao() != null) {
            throw new ConflitoException("Empréstimo já devolvido");
        }
        emprestimo.setDataDevolucao(LocalDate.now());
        emprestimo.getEquipamento().setDisponivel(true);
        return emprestimoRepository.save(emprestimo);
    }
}