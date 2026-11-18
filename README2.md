# Além das Barreiras — Sistema de Empréstimos de Equipamentos

Projeto do **Squad 2** para controlar equipamentos da empresa, colaboradores, empréstimos, devoluções e atrasos. A interface utiliza HTML, CSS e JavaScript puros; o backend utiliza Java, Spring Boot, Spring Security, JWT, Spring Data JPA, H2 e ViaCEP.

## 1. Requisitos para executar

- Java 25 (JDK 25).
- Maven 3.9 ou superior.
- Python 3.10 ou superior para o relatório.
- Acesso à internet para o backend consultar o ViaCEP no cadastro de colaboradores.

## 2. Estrutura do projeto

```text
alem-das-barreiras/
├── pom.xml
├── Dockerfile
├── README.md
├── relatorio/
│   └── relatorio.py
├── docs/
│   ├── checklist-qa.md
│   ├── divisao-equipe.md
│   └── roteiro-apresentacao.md
└── src/
    ├── main/
    │   ├── java/com/projeto/emprestimos/
    │   │   ├── EmprestimosApplication.java
    │   │   ├── model/
    │   │   ├── dto/
    │   │   ├── repository/
    │   │   ├── service/
    │   │   ├── controller/
    │   │   ├── exception/
    │   │   └── security/
    │   └── resources/
    │       ├── application.properties
    │       └── static/
    │           ├── index.html
    │           ├── style.css
    │           └── app.js
    └── test/
        ├── java/com/projeto/emprestimos/
        └── resources/application-test.properties
```

## 3. Como executar localmente

1. Extraia o ZIP do projeto.
2. Configure o JDK 25 no IntelliJ IDEA e abra a pasta `alem-das-barreiras` como projeto Maven.
3. Em **File > Project Structure > Project**, selecione **SDK 25** e, em **Settings > Build Tools > Maven > Runner**, use o JDK 25. Aguarde o Maven baixar as dependências.
4. Confira `src/main/resources/application.properties`.
5. Execute a classe `EmprestimosApplication` ou, no terminal, rode:

   ```bash
   mvn spring-boot:run
   ```

6. Confirme que `java -version` e `mvn -version` apontam para Java 25; depois abra `http://localhost:8080` para acessar a interface.
7. Abra `http://localhost:8080/swagger-ui.html` para testar as rotas no Swagger UI.
8. O JSON OpenAPI fica em `http://localhost:8080/v3/api-docs`.

O banco H2 é salvo na pasta `dados/` como arquivo, para preservar os registros após reiniciar. A pasta é criada quando o projeto iniciar.

## 4. Usuários de demonstração

| Perfil | Usuário | Senha | Permissão |
|---|---|---|---|
| ADMIN | `admin` | `admin123` | Cadastros, edição, empréstimo e devolução |
| COLABORADOR | `colaborador` | `colaborador123` | Consulta de equipamentos e empréstimos |

As contas ficam em memória na configuração do Spring Security, conforme o enunciado. As senhas padrão são apenas para demonstração acadêmica. Troque-as e configure um segredo JWT forte antes de publicar em ambiente real.

## 5. Rotas da API

Todas as rotas, exceto `POST /auth/login`, exigem `Authorization: Bearer <token>`.

| Método | Rota | Acesso | Descrição |
|---|---|---|---|
| POST | `/auth/login` | Público | Autentica e devolve JWT |
| GET | `/equipamentos` | Logado | Lista; filtros `tipo` e `disponivel` |
| GET | `/equipamentos/{id}` | Logado | Consulta equipamento |
| POST | `/equipamentos` | ADMIN | Cadastra equipamento |
| PUT | `/equipamentos/{id}` | ADMIN | Altera nome e tipo |
| DELETE | `/equipamentos/{id}` | ADMIN | Remove somente se não houver histórico |
| GET | `/colaboradores` | ADMIN | Lista colaboradores |
| GET | `/colaboradores/{id}` | ADMIN | Consulta colaborador |
| POST | `/colaboradores` | ADMIN | Cadastra colaborador e consulta ViaCEP |
| GET | `/colaboradores/{id}/emprestimos` | Logado | Lista empréstimos de um colaborador |
| GET | `/emprestimos` | Logado | Lista empréstimos |
| GET | `/emprestimos?atrasados=true` | Logado | Lista apenas atrasados |
| POST | `/emprestimos` | ADMIN | Registra empréstimo |
| PUT | `/emprestimos/{id}/devolucao` | ADMIN | Registra devolução |

### Exemplos de corpo JSON

Login:

```json
{"usuario":"admin","senha":"admin123"}
```

Cadastro de equipamento:

```json
{"patrimonio":"NB-0001","nome":"Notebook Dell","tipo":"NOTEBOOK"}
```

Cadastro de colaborador:

```json
{"nome":"Ana Souza","email":"ana@empresa.com","cep":"01310100","numero":"1000"}
```

Empréstimo:

```json
{"equipamentoId":1,"colaboradorId":1}
```

### Resposta de erro

A aplicação devolve status HTTP e corpo padronizado:

```json
{"message":"Equipamento indisponível"}
```

## 6. Regras de negócio implementadas no Service

- Equipamento cadastrado começa disponível.
- Patrimônio não pode se repetir.
- E-mail do colaborador não pode se repetir.
- Endereço é consultado no ViaCEP a partir do CEP.
- Empréstimo muda o equipamento para indisponível.
- Data do empréstimo é o dia atual; previsão é 14 dias depois.
- Não é permitido emprestar equipamento indisponível.
- Devolução grava a data atual e libera o equipamento.
- Não é permitido devolver o mesmo empréstimo duas vezes.
- Empréstimo atrasado é aquele ainda não devolvido cuja data prevista já passou.
- Equipamento com qualquer empréstimo no histórico não pode ser removido.
- Só ADMIN pode realizar operações de cadastro e alteração.

## 7. Relatório Python

Instale as dependências na raiz:

```bash
python -m pip install -r requirements.txt
```

Execute com a API ligada:

```bash
python relatorio/relatorio.py
```

O resultado será `relatorio_emprestimos.xlsx`, com as abas `Atrasados` e `Emprestados por tipo`.

Para apontar o script para outra instância, configure variáveis de ambiente:

```bash
# Linux/macOS
export API_URL="http://localhost:8080"
export API_USUARIO="admin"
export API_SENHA="admin123"
python relatorio/relatorio.py
```

No Windows PowerShell:

```powershell
$env:API_URL="http://localhost:8080"
$env:API_USUARIO="admin"
$env:API_SENHA="admin123"
python relatorio/relatorio.py
```

## 8. Testes automatizados e QA

Execute:

```bash
mvn test
```

Os testes automatizados cobrem o cálculo do atraso e a regra de não emprestar novamente um equipamento já emprestado. Os casos de aceitação do trabalho estão em `docs/checklist-qa.md` e devem ser executados pela Lara contra a aplicação integrada.

## 9. H2 Console

Em desenvolvimento, abra `http://localhost:8080/h2-console` e utilize:

- JDBC URL: `jdbc:h2:file:./dados/emprestimos`
- User Name: `sa`
- Password: deixe vazio

A console deve ficar desativada em produção com `SPRING_H2_CONSOLE_ENABLED=false`.

## 10. Deploy com Docker

Na raiz do projeto:

```bash
docker build -t alem-das-barreiras .
docker run -p 8080:8080 \
  -e JWT_SEGREDO="substitua-por-um-segredo-forte-com-mais-de-32-bytes" \
  -e SPRING_H2_CONSOLE_ENABLED=false \
  alem-das-barreiras
```

Para manter os dados do H2 fora do contêiner, monte um volume na pasta `/app/dados` ao executar o container.

## 11. Variáveis de ambiente principais

| Variável | Uso | Padrão local |
|---|---|---|
| `PORT` | Porta HTTP | `8080` |
| `JWT_SEGREDO` | Chave de assinatura JWT (mínimo 32 bytes) | Chave local de desenvolvimento |
| `SPRING_H2_CONSOLE_ENABLED` | Ativa console H2 | `true` |
| `USUARIO_ADMIN` / `SENHA_ADMIN` | Login administrador | `admin` / `admin123` |
| `USUARIO_COLABORADOR` / `SENHA_COLABORADOR` | Login de consulta | `colaborador` / `colaborador123` |
| `VIACEP_URL` | Base ViaCEP | `https://viacep.com.br/ws` |
| `API_URL` | URL consumida pelo relatório Python | `http://localhost:8080` |

## 12. Squad 2

- Lucas — Tech lead, arquitetura, backend principal e deploy.
- Cleber — Organização das tarefas, segurança JWT em conjunto com Thiago e relatório Python.
- Thiago — Segurança JWT em conjunto com Cleber e integração ViaCEP.
- Lara — QA, testes de aceitação e aprovação.
- Filipe — Documentação README e roteiro de apresentação.

Consulte `docs/divisao-equipe.md` e `docs/roteiro-apresentacao.md` para mais detalhes.

## 13. Limites desta entrega

O projeto inclui a implementação das classes e arquivos descritos no mapa e no projeto final. A validação final depende de executar `mvn test`, iniciar a API no ambiente da equipe, conferir as dependências no Maven e testar a integração externa do ViaCEP. O deploy em uma conta de nuvem e as credenciais reais são ações que dependem da equipe.
