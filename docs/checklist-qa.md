# Relatório de Garantia de Qualidade (QA) - Sistema de Empréstimos

* **Projeto:** Além das Barreiras (Sistema de Empréstimos de Equipamentos)
* **Responsável de QA:** Lara Karolinne Vieira do Nascimento
* **Data da Validação:** Novembro de 2026
* **Status Geral do Projeto:** ✅ **HOMOLOGADO E APROVADO**

---

## 📊 1. Resumo Executivo da Execução

A suíte de testes cobriu o ciclo completo de validação do sistema, contemplando testes funcionais via API (Postman), validações de integração externa (API ViaCEP) e testes automatizados de integração (JUnit 5 + Spring Boot + Banco H2 em memória).

* **Total de Casos de Teste Mapeados:** 20 cenários
* **Testes Executados com Sucesso:** 20 (100% de cobertura funcional)
* **Falhas Bloqueantes em Produção:** 0

---

## 🟢 2. Matriz de Cobertura de Testes

| ID | Cenário | Categoria | Resultado Esperado | Status |
| :--- | :--- | :--- | :--- | :--- |
| **CT01** | Login Válido | Segurança / Auth | Retorna Token JWT (200 OK) | ✅ **Aprovado** |
| **CT02** | Senha Errada | Segurança / Auth | Bloqueia acesso (401 Unauthorized) | ✅ **Aprovado** |
| **CT03** | Acesso sem Login | Segurança / Auth | Bloqueia rota protegida (401) | ✅ **Aprovado** |
| **CT04** | Acesso sem Permissão | Controle de Perfil | Colaborador tenta rota Admin (403) | ✅ **Aprovado** |
| **CT05** | Cadastrar Equipamento | Estoque / CRUD | Item criado com sucesso (201 Created) | ✅ **Aprovado** |
| **CT06** | Validação de Campo Vazio | Validação de Dados | Barra cadastro sem nome (400 Bad Request) | ✅ **Aprovado** |
| **CT07** | Patrimônio Duplicado | Regra de Negócio | Impede duplicidade de tombo (409 Conflict) | ✅ **Aprovado** |
| **CT08** | Colaborador com CEP | Integração Externa | Busca endereço via ViaCEP (201 Created) | ✅ **Aprovado** |
| **CT09** | CEP Inexistente | Tratamento de Erro | Trata falha da API externa (400 Bad Request) | ✅ **Aprovado** |
| **CT10** | CEP Incompleto | Validação de Formato| Exige formato exato de 8 dígitos (400) | ✅ **Aprovado** |
| **CT11** | E-mail Duplicado | Regra de Negócio | Impede dois cadastros com mesmo e-mail (409) | ✅ **Aprovado** |
| **CT12** | Realizar Empréstimo | Core do Negócio | Empresta equipamento disponível (201 Created) | ✅ **Aprovado** |
| **CT13** | Empréstimo Duplicado | Regra de Negócio | Impede emprestar item já ocupado (409 Conflict) | ✅ **Aprovado** |
| **CT14** | Equipamento Inexistente| Tratamento de Erro | Retorna não encontrado para ID inválido (404) | ✅ **Aprovado** |
| **CT15** | Remover Equip. Emprestado| Integridade de Dados| Impede exclusão de item em uso (409 Conflict)| ✅ **Aprovado** |
| **CT16** | Devolver Equipamento | Core do Negócio | Registra devolução e libera item (200 OK) | ✅ **Aprovado** |
| **CT17** | Devolver Equip. Já Devolvido| Regra de Negócio | Impede devolução duplicada (409 Conflict) | ✅ **Aprovado** |
| **CT18** | Listagem de Atrasados | Relatórios / Filtros| Filtra corretamente empréstimos em atraso | ✅ **Aprovado** |
| **CT19** | Relatório de Empréstimos | Relatórios | Retorna contagem e lista de pendências | ✅ **Aprovado** |
| **CT20** | Persistência de Dados | Banco de Dados | Valida integridade e retenção das informações | ✅ **Aprovado** |

---

## 🔒 3. Destaques da Qualidade e Blindagem do Sistema

1. **Segurança por Perfil (RBAC):** Os testes **CT01 a CT04** comprovaram que as rotas críticas estão protegidas, impedindo que usuários comuns realizem ações administrativas e garantindo a autenticação via JWT.
2. **Resiliência com APIs Externas:** Os testes de **CT08 a CT10** validaram que a integração com o ViaCEP lida tanto com o caminho feliz (preenchimento automático de logradouro, bairro e cidade) quanto com exceções (CEP inválido ou inexistente).
3. **Integridade de Estoque:** As regras de conflito (**CT07, CT13, CT15**) garantem que o sistema não permita inconsistências como itens duplicados ou empréstimos de equipamentos já ocupados.
4. **Automação de Testes de Integração:** Além da validação manual no Postman, a suíte automatizada (`mvn test`) roda o ciclo completo de testes com banco H2 em memória, garantindo **BUILD SUCCESS** sem regressões.