package com.projeto.emprestimos.repository;

import com.projeto.emprestimos.model.Colaborador;
import org.springframework.data.jpa.repository.JpaRepository;

/** Lucas: verifica e-mails duplicados antes de inserir um novo colaborador. */
public interface ColaboradorRepository extends JpaRepository<Colaborador, Long> {
    boolean existsByEmail(String email);
}
