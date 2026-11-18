package com.projeto.emprestimos.repository;

import com.projeto.emprestimos.model.Emprestimo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

/** Lucas: consultas que apoiam o histórico, a prevenção de exclusão e a lista de atrasados. */
public interface EmprestimoRepository extends JpaRepository<Emprestimo, Long> {
    boolean existsByEquipamentoId(Long equipamentoId);
    List<Emprestimo> findByColaboradorId(Long colaboradorId);
    List<Emprestimo> findByDataDevolucaoIsNullAndDataPrevistaDevolucaoBefore(LocalDate data);
    // Complemento exato da consulta acima: já devolvido OU prazo ainda não vencido.
    List<Emprestimo> findByDataDevolucaoIsNotNullOrDataPrevistaDevolucaoGreaterThanEqual(LocalDate data);
}
