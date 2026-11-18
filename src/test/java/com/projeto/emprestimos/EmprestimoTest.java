package com.projeto.emprestimos;

import com.projeto.emprestimos.model.Emprestimo;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

/** Lara (QA): testes unitários da regra calculada de atraso. */
class EmprestimoTest {

    @Test
    void emprestimoVencidoESemDevolucaoEstaAtrasado() {
        Emprestimo emprestimo = new Emprestimo();
        emprestimo.setDataPrevistaDevolucao(LocalDate.now().minusDays(1));
        emprestimo.setDataDevolucao(null);
        assertTrue(emprestimo.isAtrasado());
    }

    @Test
    void emprestimoDevolvidoNaoEstaAtrasado() {
        Emprestimo emprestimo = new Emprestimo();
        emprestimo.setDataPrevistaDevolucao(LocalDate.now().minusDays(1));
        emprestimo.setDataDevolucao(LocalDate.now());
        assertFalse(emprestimo.isAtrasado());
    }

    @Test
    void emprestimoNoPrazoNaoEstaAtrasado() {
        Emprestimo emprestimo = new Emprestimo();
        emprestimo.setDataPrevistaDevolucao(LocalDate.now().plusDays(1));
        emprestimo.setDataDevolucao(null);
        assertFalse(emprestimo.isAtrasado());
    }
}
