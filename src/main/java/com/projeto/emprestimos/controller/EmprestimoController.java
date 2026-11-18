package com.projeto.emprestimos.controller;

import com.projeto.emprestimos.dto.EmprestimoRequest;
import com.projeto.emprestimos.model.Emprestimo;
import com.projeto.emprestimos.service.EmprestimoService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/** Lucas: expõe histórico, criação e devolução sem implementar regra de negócio no controller. */
@RestController
@RequestMapping("/emprestimos")
@SecurityRequirement(name = "BearerAuth")
public class EmprestimoController {
    private final EmprestimoService emprestimoService;

    public EmprestimoController(EmprestimoService emprestimoService) {
        this.emprestimoService = emprestimoService;
    }

    @GetMapping
    public List<Emprestimo> listar(@RequestParam(required = false) Boolean atrasados) {
        return emprestimoService.listar(atrasados);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Emprestimo emprestar(@Valid @RequestBody EmprestimoRequest dados) {
        return emprestimoService.emprestar(dados);
    }

    @PutMapping("/{id}/devolucao")
    public Emprestimo devolver(@PathVariable Long id) {
        return emprestimoService.devolver(id);
    }
}
