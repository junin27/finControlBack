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
* 
---

# Cofres

Esta documentação descreve a API para gerenciamento de cofres. Um cofre é um local para guardar dinheiro com um objetivo específico, podendo ou não estar vinculado a um banco para a movimentação inicial ou final dos valores.

---

## Objeto Cofre (`VaultDto`)

Representa os dados detalhados de um cofre do usuário.

### Estrutura do Objeto (Exemplo `VaultDto`)

```json
{
  "id": "c0f4e1d2-a3b4-5678-9012-abcdef123456",
  "name": "Economias para Viagem",
  "description": "Juntar dinheiro para viagem de férias",
  "amount": 500.00,
  "currency": "BRL",
  "bankId": "a1b2c3d4-e5f6-7890-1234-567890abcdef",
  "bankName": "Banco Principal",
  "userId": "u5e4r3i2-d1s0-e9r8-u7s6-er1234567890",
  "createdAt": "2025-05-30T14:00:00Z",
  "updatedAt": "2025-05-30T14:05:00Z"
}
```

### Campos do Objeto `VaultDto`

| Campo         | Tipo          | Descrição                                                                 |
|---------------|---------------|---------------------------------------------------------------------------|
| `id`          | UUID          | ID único do cofre.                                                        |
| `name`        | String        | Nome do cofre.                                                            |
| `description` | String        | Descrição do cofre.                                                       |
| `amount`      | BigDecimal    | Valor atual guardado no cofre.                                            |
| `currency`    | String        | Moeda do cofre (ex: BRL, USD).                                            |
| `bankId`      | UUID          | ID do banco ao qual o cofre pode estar vinculado (opcional).              |
| `bankName`    | String        | Nome do banco ao qual o cofre pode estar vinculado (opcional).            |
| `userId`      | UUID          | ID do usuário proprietário do cofre.                                      |
| `createdAt`   | LocalDateTime | Data e hora de criação do cofre.                                          |
| `updatedAt`   | LocalDateTime | Data e hora da última atualização do cofre.                               |

---

## Endpoints

A base URL para os endpoints de cofres é: `/api/vaults`

### 1. Criar Novo Cofre

Cria um novo cofre para o usuário autenticado. O valor inicial pode ser debitado de um banco especificado.

* **Método**: `POST`
* **Endpoint**: `/api/vaults`
* **Corpo da Requisição** (`VaultCreateDto`):

    ```json
    {
      "name": "Economias para Viagem",
      "description": "Juntar dinheiro para viagem de férias", // Opcional
      "initialAmount": 500.00,
      "currency": "BRL",
      "bankId": "a1b2c3d4-e5f6-7890-1234-567890abcdef" // Opcional
    }
    ```

    * **Campos `VaultCreateDto`**:
        * `name` (String, obrigatório, max 100): Nome do cofre.
        * `description` (String, opcional, max 255): Descrição do cofre. Se não informado ou vazio, um valor padrão ("Campo não Informado pelo Usuário") é definido.
        * `initialAmount` (BigDecimal, obrigatório, >= 0): Valor inicial a ser guardado.
        * `currency` (String, obrigatório, max 10): Moeda do cofre (ex: BRL, USD).
        * `bankId` (UUID, opcional): ID do banco de onde o `initialAmount` será retirado. Se fornecido, o banco deve pertencer ao usuário e ter saldo suficiente.

* **Retornos de Sucesso**:
    * **`201 Created`**: Cofre criado com sucesso.
        * Corpo: Objeto `VaultDto` do cofre criado.
        * Cabeçalho `Location`: URL para o recurso do cofre criado (ex: `/api/vaults/{id_do_cofre}`).

* **Retornos de Erro Possíveis**:
    * **`400 Bad Request`**: Dados inválidos na requisição (ex: `name` ausente, `initialAmount` negativo, `currency` ausente) ou saldo insuficiente no banco se `bankId` for fornecido.
        * Corpo: Mensagem de erro detalhando a falha (ex: "O valor inicial do cofre não pode ser negativo.", "O seu saldo no banco X é insuficiente...").
    * **`401 Unauthorized`**: Token JWT ausente, inválido ou expirado.
    * **`404 Not Found`**: Usuário autenticado não encontrado no sistema, ou `bankId` fornecido não corresponde a um banco existente ou que não pertence ao usuário.
        * Corpo: Mensagem de erro (ex: "Usuário autenticado não encontrado...", "Você não possui o banco com ID X cadastrado...").
    * **`500 Internal Server Error`**: Erro inesperado no servidor.

---

### 2. Listar Todos os Cofres do Usuário

Recupera uma lista de todos os cofres pertencentes ao usuário autenticado.

* **Método**: `GET`
* **Endpoint**: `/api/vaults`

* **Retornos de Sucesso**:
    * **`200 OK`**: Lista de cofres retornada com sucesso.
        * Corpo: Array de objetos `VaultDto`. Retorna um array vazio `[]` se o usuário não possuir cofres.

* **Retornos de Erro Possíveis**:
    * **`401 Unauthorized`**: Token JWT ausente, inválido ou expirado.
    * **`404 Not Found`**: Usuário autenticado não encontrado no sistema.
    * **`500 Internal Server Error`**: Erro inesperado no servidor.

---

### 3. Buscar Cofre por ID

Recupera os detalhes de um cofre específico pelo seu ID. O cofre deve pertencer ao usuário autenticado.

* **Método**: `GET`
* **Endpoint**: `/api/vaults/{vaultId}`
    * `{vaultId}` (UUID): ID do cofre a ser buscado.

* **Retornos de Sucesso**:
    * **`200 OK`**: Cofre encontrado e retornado com sucesso.
        * Corpo: Objeto `VaultDto` detalhado do cofre.

* **Retornos de Erro Possíveis**:
    * **`401 Unauthorized`**: Token JWT ausente, inválido ou expirado.
    * **`404 Not Found`**: Cofre com o ID fornecido não encontrado, não pertence ao usuário autenticado, ou usuário autenticado não encontrado.
        * Corpo: Mensagem de erro (ex: "Cofre com ID X não encontrado ou não pertence ao usuário.").
    * **`500 Internal Server Error`**: Erro inesperado no servidor.

---

### 4. Listar Cofres por Banco

Recupera uma lista de todos os cofres do usuário autenticado que estão vinculados a um banco específico.

* **Método**: `GET`
* **Endpoint**: `/api/vaults/bank/{bankId}`
    * `{bankId}` (UUID): ID do banco para filtrar os cofres.

* **Retornos de Sucesso**:
    * **`200 OK`**: Lista de cofres vinculados ao banco retornada com sucesso.
        * Corpo: Array de objetos `VaultDto`. Retorna um array vazio `[]` se não houver cofres vinculados ao banco para o usuário.

* **Retornos de Erro Possíveis**:
    * **`401 Unauthorized`**: Token JWT ausente, inválido ou expirado.
    * **`404 Not Found`**: Banco com o ID fornecido não encontrado, não pertence ao usuário autenticado, ou usuário autenticado não encontrado.
        * Corpo: Mensagem de erro (ex: "Você não possui o banco com ID X cadastrado...").
    * **`500 Internal Server Error`**: Erro inesperado no servidor.

---

### 5. Atualizar Cofre Existente

Atualiza os dados de um cofre existente (nome, descrição, moeda). O cofre deve pertencer ao usuário autenticado.
*Nota: Esta operação não permite alterar o valor (`amount`) ou o vínculo com o banco (`bankId`).*

* **Método**: `PUT`
* **Endpoint**: `/api/vaults/{vaultId}`
    * `{vaultId}` (UUID): ID do cofre a ser atualizado.
* **Corpo da Requisição** (`VaultUpdateDto`):

    ```json
    {
      "name": "Minhas Economias para Férias", // Opcional
      "description": "Guardar dinheiro para a viagem de fim de ano", // Opcional
      "currency": "USD" // Opcional
    }
    ```
    * **Campos `VaultUpdateDto`**:
        * `name` (String, opcional, max 100): Novo nome do cofre.
        * `description` (String, opcional, max 255): Nova descrição do cofre.
        * `currency` (String, opcional, max 10): Nova moeda do cofre.

* **Retornos de Sucesso**:
    * **`200 OK`**: Cofre atualizado com sucesso.
        * Corpo: Objeto `VaultDto` do cofre atualizado.

* **Retornos de Erro Possíveis**:
    * **`400 Bad Request`**: Dados inválidos na requisição (ex: violação de tamanho dos campos).
    * **`401 Unauthorized`**: Token JWT ausente, inválido ou expirado.
    * **`404 Not Found`**: Cofre com o ID fornecido não encontrado, não pertence ao usuário autenticado, ou usuário autenticado não encontrado.
        * Corpo: Mensagem de erro (ex: "Cofre com ID X não encontrado ou não pertence ao usuário.").
    * **`500 Internal Server Error`**: Erro inesperado no servidor.

---

### 6. Deletar Cofre

Deleta um cofre do usuário autenticado.
* Se o cofre estiver vinculado a um banco, o valor (`amount`) do cofre é devolvido ao saldo do banco.
* Se o cofre não estiver vinculado a um banco e possuir saldo (`amount > 0`), a exclusão não é permitida para evitar perda de valor. O valor deve ser zerado antes (operação não diretamente exposta nesta API, sugerindo uma futura funcionalidade de "resgate").

* **Método**: `DELETE`
* **Endpoint**: `/api/vaults/{vaultId}`
    * `{vaultId}` (UUID): ID do cofre a ser deletado.

* **Retornos de Sucesso**:
    * **`204 No Content`**: Cofre deletado com sucesso. Nenhum corpo na resposta.

* **Retornos de Erro Possíveis**:
    * **`400 Bad Request` (`InvalidOperationException`)**: Tentativa de deletar um cofre não vinculado a um banco que ainda possui saldo.
        * Corpo: Mensagem de erro (ex: "Não é possível excluir o cofre (ID: X), pois ele não está vinculado a nenhum banco e o valor de Y Z será perdido...").
    * **`401 Unauthorized`**: Token JWT ausente, inválido ou expirado.
    * **`404 Not Found`**: Cofre com o ID fornecido não encontrado, não pertence ao usuário autenticado, ou usuário/banco associado não encontrado durante a operação.
        * Corpo: Mensagem de erro.
    * **`500 Internal Server Error`**: Erro inesperado no servidor.

---

## Considerações Importantes

* **Propriedade de Dados**: A API garante que um usuário só possa interagir com os cofres (e bancos associados) que lhe pertencem.
* **Interação com Saldo do Banco**:
    * **Criação de Cofre**: Se um `bankId` é fornecido, o `initialAmount` do cofre é subtraído do saldo do banco correspondente. É necessário que o banco tenha saldo suficiente.
    * **Deleção de Cofre**: Se o cofre estava vinculado a um `bankId`, o `amount` atual do cofre é adicionado de volta ao saldo do banco.
* **Atualização de Valor do Cofre**: A API de atualização (`PUT /api/vaults/{vaultId}`) não permite modificar diretamente o `amount` do cofre nem seu vínculo com um `bankId`. Essas operações (como depositar ou sacar do cofre) precisariam de endpoints específicos ou seriam consequências de outras operações no sistema (não detalhadas nesta API de Cofres).
* **Deleção de Cofre Não Vinculado com Saldo**: Um cofre que não está associado a um `bankId` e que possui `amount > 0` não pode ser deletado diretamente. Isso previne a perda de fundos "órfãos". O serviço sugere que o valor seja removido antes da exclusão.


