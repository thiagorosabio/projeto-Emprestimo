# Relatório de Garantia de Qualidade (QA) - Testes de Integração

* **Módulo:** Sistema de Empréstimos (`EmprestimoServiceTest`)
* **Framework:** JUnit 5 / Spring Boot / Banco H2 (Perfil `test`)
* **Status da Suíte:** Aprovado (BUILD SUCCESS)

## Cenários Validados
1. **Regra R1 (Indisponibilidade):** Valida que o sistema impede o empréstimo de um equipamento que já se encontra emprestado, disparando a exceção `ConflitoException`.
2. **Filtros de Listagem:** Valida a separação correta de empréstimos atrasados, devolvidos e sem parâmetros.

## Evidência de Execução
- Todos os **5 testes automatizados** executados com sucesso (0 falhas, 0 erros).

[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------