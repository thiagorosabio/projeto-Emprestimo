package com.projeto.emprestimos;

import com.projeto.emprestimos.dto.EmprestimoRequest;
import com.projeto.emprestimos.exception.ConflitoException;
import com.projeto.emprestimos.model.Colaborador;
import com.projeto.emprestimos.model.Emprestimo;
import com.projeto.emprestimos.model.Equipamento;
import com.projeto.emprestimos.model.TipoEquipamento;
import com.projeto.emprestimos.repository.ColaboradorRepository;
import com.projeto.emprestimos.repository.EmprestimoRepository;
import com.projeto.emprestimos.repository.EquipamentoRepository;
import com.projeto.emprestimos.service.EmprestimoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import static org.junit.jupiter.api.Assertions.*;

/** Lara e Lucas: teste de integração garante a regra R1 no Service com banco H2 isolado. */
@SpringBootTest
@ActiveProfiles("test")
class EmprestimoServiceTest {
    @Autowired private EmprestimoService emprestimoService;
    @Autowired private EquipamentoRepository equipamentoRepository;
    @Autowired private ColaboradorRepository colaboradorRepository;
    @Autowired private EmprestimoRepository emprestimoRepository;

    private Long equipamentoId;
    private Long colaboradorId;

    @BeforeEach
    void prepararDados() {
        emprestimoRepository.deleteAll();
        equipamentoRepository.deleteAll();
        colaboradorRepository.deleteAll();

        Equipamento equipamento = equipamentoRepository.save(
                new Equipamento("NB-TESTE-01", "Notebook de teste", TipoEquipamento.NOTEBOOK));
        Colaborador colaborador = new Colaborador();
        colaborador.setNome("Pessoa de Teste");
        colaborador.setEmail("teste@empresa.com");
        colaborador.setCep("01310100");
        colaborador.setNumero("100");
        colaboradorRepository.save(colaborador);
        equipamentoId = equipamento.getId();
        colaboradorId = colaborador.getId();
    }

    @Test
    void naoPermiteEmprestarEquipamentoQueJaEstaEmprestado() {
        EmprestimoRequest pedido = new EmprestimoRequest(equipamentoId, colaboradorId);
        emprestimoService.emprestar(pedido);
        ConflitoException erro = assertThrows(ConflitoException.class,
                () -> emprestimoService.emprestar(pedido));
        assertEquals("Equipamento indisponível", erro.getMessage());
    }

    @Test
    void filtroDeAtrasadosSeparaTrueFalseESemParametro() {
        Emprestimo atrasado = salvarEmprestimo(LocalDate.now().minusDays(1), null);
        Emprestimo devolvido = salvarEmprestimo(LocalDate.now().minusDays(1), LocalDate.now());
        Emprestimo noPrazo = salvarEmprestimo(LocalDate.now(), null);

        assertEquals(Set.of(atrasado.getId()), ids(emprestimoService.listar(true)));
        assertEquals(Set.of(devolvido.getId(), noPrazo.getId()), ids(emprestimoService.listar(false)));
        assertEquals(Set.of(atrasado.getId(), devolvido.getId(), noPrazo.getId()), ids(emprestimoService.listar(null)));
    }

    private Emprestimo salvarEmprestimo(LocalDate prevista, LocalDate devolucao) {
        Emprestimo emprestimo = new Emprestimo();
        emprestimo.setEquipamento(equipamentoRepository.findById(equipamentoId).orElseThrow());
        emprestimo.setColaborador(colaboradorRepository.findById(colaboradorId).orElseThrow());
        emprestimo.setDataEmprestimo(prevista.minusDays(14));
        emprestimo.setDataPrevistaDevolucao(prevista);
        emprestimo.setDataDevolucao(devolucao);
        return emprestimoRepository.save(emprestimo);
    }

    private Set<Long> ids(List<Emprestimo> lista) {
        return lista.stream().map(Emprestimo::getId).collect(Collectors.toSet());
    }
}