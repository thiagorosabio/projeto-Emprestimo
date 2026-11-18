# Roteiro de apresentação — 18/11

## 1. Contexto e objetivo

A equipe **Além das Barreiras** apresenta o **Sistema de Empréstimos de Equipamentos**, criado para substituir o controle manual em planilhas por um sistema com cadastro, empréstimos, devoluções e acompanhamento de atrasos.

## 2. Demonstração pela interface

1. Entrar como administrador.
2. Cadastrar um equipamento de exemplo.
3. Cadastrar um colaborador com CEP válido e mostrar o endereço preenchido pela API ViaCEP.
4. Registrar um empréstimo.
5. Mostrar que o equipamento passou a aparecer como emprestado.
6. Registrar a devolução e mostrar que ficou disponível.
7. Abrir a lista de atrasados.

## 3. Demonstração de validações

- Tentar usar senha incorreta.
- Tentar emprestar equipamento já emprestado.
- Tentar cadastrar patrimônio repetido.
- Tentar cadastrar CEP inexistente.
- Entrar como COLABORADOR e tentar executar uma rota ADMIN.

## 4. Participação dos integrantes

- **Lucas:** explica arquitetura em camadas, entidades, regras no Service, banco H2 e deploy.
- **Cleber:** explica organização do Squad, login JWT em conjunto com Thiago e geração do relatório Python.
- **Thiago:** explica autenticação JWT em conjunto com Cleber e consulta do ViaCEP durante o cadastro de colaborador.
- **Lara:** apresenta o checklist de QA, os testes de aceitação e os resultados aprovados/reprovados.
- **Filipe:** apresenta o README, as rotas no Swagger, a organização da documentação e encerra o roteiro.

## 5. Fechamento

Mostrar a planilha `relatorio_emprestimos.xlsx` com as abas **Atrasados** e **Emprestados por tipo**. Finalizar explicando que a API mantém as regras centralizadas e que os dados ficam persistidos no banco.
