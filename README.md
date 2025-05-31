# finControlBack

## Autenticação

Todas as rotas da API de Bancos requerem autenticação. Certifique-se de incluir o token de autenticação JWT no cabeçalho `Authorization` de suas requisições:

`Authorization: Bearer <seu_token_jwt>`

---

# Bancos

Esta documentação descreve a API para gerenciamento de bancos. Através dela, é possível criar, listar, visualizar, atualizar e remover bancos, bem como gerenciar os valores associados a eles.

---

## Objeto Banco (`BankDto`)

Representa os dados detalhados de um banco, incluindo totais de movimentações, saldo atual e timestamps.

### Estrutura do Objeto (Exemplo `BankDto`)

```json
{
  "id": "a1b2c3d4-e5f6-7890-1234-567890abcdef",
  "name": "Banco Principal",
  "description": "Conta corrente para despesas do dia a dia",
  "totalIncome": 7500.00,
  "totalExpense": 3250.50,
  "currentBalance": 4249.50,
  "createdAt": "2025-05-28T10:15:30",
  "updatedAt": "2025-05-28T10:20:00"
}
```

### Campos do Objeto `BankDto`

| Campo            | Tipo          | Descrição                                                                       |
|------------------|---------------|---------------------------------------------------------------------------------|
| `id`             | UUID          | UUID do banco.                                                            |
| `name`           | String        | Nome do banco.                                                            |
| `description`    | String        | Descrição do banco.                                                       |
| `totalIncome`    | BigDecimal    | Soma total das receitas (entradas) associadas a este banco (calculado).       |
| `totalExpense`   | BigDecimal    | Soma total das despesas (saídas) associadas a este banco (calculado).         |
| `currentBalance` | BigDecimal    | Saldo atual do banco (armazenado e ajustado por operações).                 |
| `createdAt`      | LocalDateTime | Timestamp de criação do banco.                                              |
| `updatedAt`      | LocalDateTime | Timestamp da última atualização do banco.                                   |

---

## Endpoints

A base URL para os endpoints de bancos é: `/api/banks`

### 1. Criar Novo Banco

Cria um novo banco para o usuário autenticado.

* **Método**: `POST`
* **Endpoint**: `/api/banks`
* **Corpo da Requisição** (`BankCreateDto`):

    ```json
    {
      "name": "Banco Principal",
      "description": "Conta corrente principal para despesas",
      "initialBalance": 1500.50
    }
    ```

    * **Campos `BankCreateDto`**:
        * `name` (String, obrigatório): Nome do banco.
        * `description` (String, opcional): Descrição opcional do banco. Se não informado ou vazio, um valor padrão ("Campo não Informado pelo Usuário") pode ser definido no backend.
        * `initialBalance` (BigDecimal, opcional): Saldo inicial opcional do banco. Se não fornecido, o saldo começa em 0.

* **Retornos de Sucesso**:
    * **`201 Created`**: Banco criado com sucesso.
        * Corpo: Objeto `BankDto` do banco criado.
        * Cabeçalho `Location`: URL para o recurso do banco criado (ex: `/api/banks/{id_do_banco}`).

* **Retornos de Erro Possíveis**:
    * **`400 Bad Request`**: Dados inválidos na requisição (ex: `name` ausente ou vazio) devido à validação `@Valid` e `@NotBlank`.
        * Corpo: Objeto de erro com detalhes da validação.
    * **`401 Unauthorized`**: Token JWT ausente, inválido ou expirado (inferido pela lógica de segurança no `BankService`).
    * **`500 Internal Server Error`**: Erro inesperado no servidor (ex: "Usuário não encontrado" durante a criação se o usuário autenticado não existir no DB, conforme `BankService`).

---

### 2. Listar Todos os Bancos

Lista todos os bancos pertencentes ao usuário autenticado.

* **Método**: `GET`
* **Endpoint**: `/api/banks`

* **Retornos de Sucesso**:
    * **`200 OK`**: Lista de bancos retornada com sucesso.
        * Corpo: Array de objetos `BankDto`. Retorna um array vazio `[]` se o usuário não possuir bancos.

* **Retornos de Erro Possíveis**:
    * **`401 Unauthorized`**: Token JWT ausente, inválido ou expirado (inferido pela lógica de segurança no `BankService`).
    * **`500 Internal Server Error`**: Erro inesperado no servidor (ex: "Usuário não encontrado" conforme `BankService`).

---

### 3. Atualizar Banco Existente

Atualiza os dados de um banco existente. Somente o proprietário do banco pode atualizá-lo.

* **Método**: `PUT`
* **Endpoint**: `/api/banks/{id}`
    * `{id}` (UUID): ID do banco a ser atualizado.
* **Corpo da Requisição** (`BankUpdateDto`):

    ```json
    {
      "name": "Banco Investimentos",
      "description": "Conta para aplicações financeiras",
      "balance": 2500.75
    }
    ```
    * **Campos `BankUpdateDto`**:
        * `name` (String, opcional): Novo nome do banco.
        * `description` (String, opcional): Nova descrição opcional do banco.
        * `balance` (BigDecimal, opcional): Novo saldo para o banco. ATENÇÃO: Ajusta diretamente o saldo.

* **Retornos de Sucesso**:
    * **`200 OK`**: Banco atualizado com sucesso.
        * Corpo: Objeto `BankDto` do banco atualizado.

* **Retornos de Erro Possíveis**:
    * **`400 Bad Request`**: Dados inválidos na requisição (devido à validação `@Valid`).
    * **`401 Unauthorized`**: Token JWT ausente, inválido ou expirado (inferido pela lógica de segurança no `BankService`).
    * **`403 Forbidden`**: O usuário autenticado não é o proprietário do banco especificado (conforme lógica em `BankService`).
    * **`404 Not Found`**: Banco com o ID fornecido não encontrado.
    * **`500 Internal Server Error`**: Erro inesperado no servidor (ex: "Usuário não encontrado" conforme `BankService`).

---

### 4. Remover Banco por ID

Remove um banco específico pelo seu ID. Todas as receitas e despesas associadas a este banco também serão removidas devido à configuração de cascata (`cascade = CascadeType.ALL`). Somente o proprietário do banco pode removê-lo.

* **Método**: `DELETE`
* **Endpoint**: `/api/banks/{id}`
    * `{id}` (UUID): ID do banco a ser removido.

* **Retornos de Sucesso**:
    * **`204 No Content`**: Banco e dados associados removidos com sucesso. Nenhum corpo na resposta.

* **Retornos de Erro Possíveis**:
    * **`401 Unauthorized`**: Token JWT ausente, inválido ou expirado (inferido pela lógica de segurança no `BankService`).
    * **`403 Forbidden`**: O usuário autenticado não é o proprietário do banco especificado (conforme lógica em `BankService`).
    * **`404 Not Found`**: Banco com o ID fornecido não encontrado.
    * **`500 Internal Server Error`**: Erro inesperado no servidor (ex: "Usuário não encontrado" conforme `BankService`).

---

### 5. Remover Todas as Receitas de um Banco

Remove todas as receitas (`ExtraIncome`) associadas a um banco específico. O saldo do banco é atualizado, subtraindo o valor total das receitas removidas. Somente o proprietário do banco pode realizar esta ação.

* **Método**: `DELETE`
* **Endpoint**: `/api/banks/{id}/clear-incomes`
    * `{id}` (UUID): ID do banco para limpar as receitas.

* **Retornos de Sucesso**:
    * **`204 No Content`**: Receitas do banco removidas com sucesso. Nenhum corpo na resposta.

* **Retornos de Erro Possíveis**:
    * **`401 Unauthorized`**: Token JWT ausente, inválido ou expirado (inferido pela lógica de segurança no `BankService`).
    * **`403 Forbidden`**: O usuário autenticado não é o proprietário do banco especificado (conforme lógica em `BankService`).
    * **`404 Not Found`**: Banco com o ID fornecido não encontrado.
    * **`500 Internal Server Error`**: Erro inesperado no servidor (ex: "Usuário não encontrado" conforme `BankService`).

---

### 6. Remover Todas as Despesas de um Banco

Remove todas as despesas (`Expense`) associadas a um banco específico. O saldo do banco é atualizado, adicionando o valor total das despesas removidas (revertendo os débitos). Somente o proprietário do banco pode realizar esta ação.

* **Método**: `DELETE`
* **Endpoint**: `/api/banks/{id}/clear-expenses`
    * `{id}` (UUID): ID do banco para limpar as despesas.

* **Retornos de Sucesso**:
    * **`204 No Content`**: Despesas do banco removidas com sucesso. Nenhum corpo na resposta.

* **Retornos de Erro Possíveis**:
    * **`401 Unauthorized`**: Token JWT ausente, inválido ou expirado (inferido pela lógica de segurança no `BankService`).
    * **`403 Forbidden`**: O usuário autenticado não é o proprietário do banco especificado (conforme lógica em `BankService`).
    * **`404 Not Found`**: Banco com o ID fornecido não encontrado.
    * **`500 Internal Server Error`**: Erro inesperado no servidor (ex: "Usuário não encontrado" conforme `BankService`).

---

## Considerações Importantes

* **Propriedade de Dados**: A API impõe que os usuários só podem interagir com os bancos que lhes pertencem, conforme a lógica de verificação de usuário no `BankService`.
* **Saldo do Banco (`currentBalance`)**:
    * O campo `balance` na entidade `Bank` é o saldo autoritativo.
    * Ele pode ser definido no momento da criação através do `initialBalance` em `BankCreateDto`.
    * Pode ser diretamente modificado através do campo `balance` em `BankUpdateDto`.
    * É automaticamente ajustado quando todas as receitas (`clearIncomes`) ou todas as despesas (`clearExpenses`) de um banco são removidas.
    * A criação/remoção de receitas e despesas individuais (presumivelmente através de outros endpoints não detalhados aqui, ex: `/api/incomes`, `/api/expenses`) também deve ajustar este saldo.
* **Campos Calculados em `BankDto`**:
    * `totalIncome` e `totalExpense` são calculados no momento da consulta, somando as respectivas transações associadas ao banco, conforme o método `toDto` no `BankService`.
* **Exclusão em Cascata**: Ao remover um banco (`DELETE /api/banks/{id}`), todas as suas receitas (`ExtraIncome`) e despesas (`Expense`) associadas são automaticamente excluídas do banco de dados devido à configuração `cascade = CascadeType.ALL, orphanRemoval = true` na entidade `Bank`.

