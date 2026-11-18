package com.projeto.emprestimos.controller;

import com.projeto.emprestimos.dto.ColaboradorRequest;
import com.projeto.emprestimos.model.Colaborador;
import com.projeto.emprestimos.model.Emprestimo;
import com.projeto.emprestimos.service.ColaboradorService;
import com.projeto.emprestimos.service.EmprestimoService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/** Lucas: operações administrativas de colaboradores e consulta de empréstimos associados. */
@RestController
@RequestMapping("/colaboradores")
@SecurityRequirement(name = "BearerAuth")
public class ColaboradorController {
    private final ColaboradorService colaboradorService;
    private final EmprestimoService emprestimoService;

    public ColaboradorController(ColaboradorService colaboradorService, EmprestimoService emprestimoService) {
        this.colaboradorService = colaboradorService;
        this.emprestimoService = emprestimoService;
    }

    @GetMapping
    public List<Colaborador> listar() {
        return colaboradorService.listar();
    }

    @GetMapping("/{id}/emprestimos")
    public List<Emprestimo> listarEmprestimos(@PathVariable Long id) {
        return emprestimoService.listarDoColaborador(id);
    }

    @GetMapping("/{id}")
    public Colaborador buscar(@PathVariable Long id) {
        return colaboradorService.buscar(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Colaborador cadastrar(@Valid @RequestBody ColaboradorRequest dados) {
        return colaboradorService.cadastrar(dados);
    }
}
