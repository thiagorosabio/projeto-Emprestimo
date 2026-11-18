package com.projeto.emprestimos.controller;

import com.projeto.emprestimos.dto.AtualizarEquipamentoRequest;
import com.projeto.emprestimos.dto.EquipamentoRequest;
import com.projeto.emprestimos.model.Equipamento;
import com.projeto.emprestimos.model.TipoEquipamento;
import com.projeto.emprestimos.service.EquipamentoService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/** Lucas: controller fino; recebe HTTP, valida o corpo e delega a regra ao Service. */
@RestController
@RequestMapping("/equipamentos")
@SecurityRequirement(name = "BearerAuth")
public class EquipamentoController {
    private final EquipamentoService equipamentoService;

    public EquipamentoController(EquipamentoService equipamentoService) {
        this.equipamentoService = equipamentoService;
    }

    @GetMapping
    public List<Equipamento> listar(@RequestParam(required = false) TipoEquipamento tipo,
                                    @RequestParam(required = false) Boolean disponivel) {
        return equipamentoService.listar(tipo, disponivel);
    }

    @GetMapping("/{id}")
    public Equipamento buscar(@PathVariable Long id) {
        return equipamentoService.buscar(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Equipamento cadastrar(@Valid @RequestBody EquipamentoRequest dados) {
        return equipamentoService.cadastrar(dados);
    }

    @PutMapping("/{id}")
    public Equipamento atualizar(@PathVariable Long id,
                                 @Valid @RequestBody AtualizarEquipamentoRequest dados) {
        return equipamentoService.atualizar(id, dados);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        equipamentoService.remover(id);
        return ResponseEntity.noContent().build();
    }
}
