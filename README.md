# finControlBack

## Autenticação

Todas as rotas da API de Bancos requerem autenticação. Certifique-se de incluir o token de autenticação JWT no cabeçalho `Authorization` de suas requisições:

`Authorization: Bearer <seu_token_jwt>`

---

# Autenticação/Usuários
## Convenções

- **Formato da URL Base:** Todos os endpoints são prefixados com a URL base da sua aplicação (ex: `http://localhost:8080`).
- **Autenticação:** Endpoints sob `/api` requerem um token JWT Bearer no header `Authorization`.
- **Formato de Resposta:** As respostas são em JSON.
- **Respostas de Erro:** Erros são retornados com um status HTTP apropriado e um corpo JSON contendo:
    - `timestamp`: Data e hora da ocorrência do erro.
    - `status`: Código de status HTTP.
    - `error`: Descrição curta do erro (ex: "Not Found", "Bad Request").
    - `message`: Mensagem detalhada sobre o erro.
    - `path`: O caminho do endpoint que foi chamado.
    - `details` (opcional): Uma lista de mensagens de erro específicas, geralmente para erros de validação de campos.

## 1. Endpoints de Autenticação

Controlador responsável: `AuthController`
Rota base: `/auth`

### 1.1. Registrar Novo Usuário

- **Endpoint:** `POST /auth/register`
- **Funcionalidade:** Cria um novo usuário no sistema.
- **Autenticação:** Nenhuma autenticação é necessária.

#### Corpo da Requisição (`UserRegisterDto`)

| Campo             | Tipo         | Obrigatório | Descrição                                                                                                | Validações                                                                                                                                                                                                                                                                                                                         | Exemplo                     |
|-------------------|--------------|-------------|----------------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------|
| `name`            | String       | Sim         | Nome completo do usuário.                                                                                | Deve ter entre 5 e 100 caracteres. Deve conter ao menos duas palavras, cada uma com no mínimo 2 letras. Somente letras e apóstrofos são permitidos. Ex: "João Silva".                                                                                                                                                              | "Fulano de Tal"             |
| `email`           | String       | Sim         | Endereço de e-mail do usuário.                                                                           | Deve ser um e-mail válido e único no sistema. Máximo de 150 caracteres.                                                                                                                                                                                                                                                              | "fulano.tal@example.com"    |
| `password`        | String       | Sim         | Senha de acesso do usuário.                                                                              | Deve ter no mínimo 6 e no máximo 100 caracteres. Deve conter ao menos uma letra e um número.                                                                                                                                                                                                                                         | "Senha@123"                 |
| `confirmPassword` | String       | Sim         | Confirmação da senha.                                                                                    | Deve ser idêntica ao campo `password`. Mínimo de 6 e máximo de 100 caracteres.                                                                                                                                                                                                                                                      | "Senha@123"                 |
| `salary`          | BigDecimal   | Sim         | Salário mensal inicial do usuário.                                                                       | Deve ser um valor numérico maior ou igual a zero.                                                                                                                                                                                                                                                                                  | `3500.00`                   |

#### Respostas Esperadas

- **`201 Created`**: Usuário registrado com sucesso.
    - **Corpo da Resposta (`UserDto`):**
      ```json
      {
          "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
          "name": "Fulano de Tal",
          "email": "fulano.tal@example.com",
          "salary": 3500.00,
          "createdAt": "2025-05-31T10:00:00Z",
          "updatedAt": "2025-05-31T10:00:00Z"
      }
      ```
    - **Headers:** `Location` contendo a URI do novo recurso criado (ex: `/auth/register/3fa85f64-5717-4562-b3fc-2c963f66afa6`).

#### Possíveis Erros

- **`400 Bad Request`**: Dados de registro inválidos.
    - **Motivos:**
        - Campos obrigatórios faltando.
        - `name`: Não informado, menos de duas palavras, caracteres inválidos, ou fora do tamanho permitido (5-100 caracteres).
        - `email`: Formato inválido ou tamanho excedido.
        - `password`: Menos de 6 caracteres, não contém letras e números.
        - `confirmPassword`: Não confere com a senha informada.
        - `salary`: Valor negativo.
    - **Exemplo de Corpo da Resposta (Erro de Validação Múltipla):**
      ```json
      {
          "timestamp": "2025-05-30T22:00:00Z",
          "status": 400,
          "error": "Erro de Validação de Campo",
          "message": "Um ou mais campos falharam na validação. Veja os detalhes.",
          "path": "/auth/register",
          "details": [
              "name: Você precisa informar o seu nome completo com ao menos duas palavras, e cada palavra deve ter no mínimo 2 letras (somente letras e apóstrofos são permitidos).",
              "password: A sua senha está fraca, ela precisa possuir no mínimo 6 caracteres, com ao menos uma letra e um número."
          ]
      }
      ```
    - **Exemplo de Corpo da Resposta (Senhas Não Conferem):**
      ```json
      {
          "timestamp": "2025-05-30T22:01:00Z",
          "status": 400,
          "error": "Bad Request",
          "message": "As duas senhas não conferem, elas precisam ter os mesmos caracteres exatamente iguais.",
          "path": "/auth/register"
      }
      ```
- **`409 Conflict`**: O e-mail fornecido já está cadastrado no sistema.
    - **Exemplo de Corpo da Resposta:**
      ```json
      {
          "timestamp": "2025-05-30T22:02:00Z",
          "status": 409,
          "error": "Conflict",
          "message": "O e-mail 'existente@example.com' já está cadastrado.",
          "path": "/auth/register"
      }
      ```

### 1.2. Autenticar Usuário (Login)

- **Endpoint:** `POST /auth/login`
- **Funcionalidade:** Autentica um usuário existente e retorna um token JWT.
- **Autenticação:** Nenhuma autenticação é necessária.

#### Corpo da Requisição (`LoginDto`)

| Campo    | Tipo   | Obrigatório | Descrição                               | Validações                                                                 | Exemplo                  |
|----------|--------|-------------|-----------------------------------------|----------------------------------------------------------------------------|--------------------------|
| `email`  | String | Sim         | Endereço de e-mail do usuário.          | Obrigatório. Deve ser um e-mail válido. Máximo de 150 caracteres.          | "joao.silva@example.com" |
| `password` | String | Sim         | Senha de acesso do usuário.             | Obrigatório.                                                               | "Senha@123"              |

#### Respostas Esperadas

- **`200 OK`**: Autenticado com sucesso.
    - **Corpo da Resposta:**
      ```json
      {
          "token": "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2FvLnNpbHZhQGV4YW1wbGUuY29tIiwiaWF0IjoxNzA5Mjg0MzIwLCJleHAiOjE3MDkzNzA3MjB9.exampleToken",
          "userId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
          "userName": "João Silva"
      }
      ```
      - `token`: Token JWT para ser usado em requisições autenticadas.
      - `userId`: ID do usuário autenticado.
      - `userName`: Nome do usuário autenticado.

#### Possíveis Erros

- **`400 Bad Request`**: Dados de login inválidos.
    - **Motivos:** E-mail ou senha não fornecidos ou em formato incorreto.
    - **Exemplo de Corpo da Resposta (Campo Faltando):**
      ```json
      {
          "timestamp": "2025-05-30T22:03:00Z",
          "status": 400,
          "error": "Erro de Validação de Campo",
          "message": "Um ou mais campos falharam na validação. Veja os detalhes.",
          "path": "/auth/login",
          "details": [
              "email: O e-mail é obrigatório."
          ]
      }
      ```
- **`401 Unauthorized`**: Credenciais inválidas (senha incorreta).
    - **Exemplo de Corpo da Resposta:**
      ```json
      {
          "timestamp": "2025-05-30T22:04:00Z",
          "status": 401,
          "error": "Unauthorized",
          "message": "A sua senha está incorreta.",
          "path": "/auth/login"
      }
      ```
- **`404 Not Found`**: O e-mail fornecido não está cadastrado no sistema.
    - **Exemplo de Corpo da Resposta:**
      ```json
      {
          "timestamp": "2025-05-30T22:05:00Z",
          "status": 404,
          "error": "Not Found",
          "message": "O email 'naoexiste@example.com' não está cadastrado no sistema.",
          "path": "/auth/login"
      }
      ```

## 2. Endpoints de Gerenciamento de Usuários

Controlador responsável: `UserController`
Rota base: `/api/users`
**Autenticação:** Todos os endpoints nesta seção requerem um token JWT Bearer válido no header `Authorization`.

### 2.1. Listar Todos os Usuários

- **Endpoint:** `GET /api/users`
- **Funcionalidade:** Retorna uma lista de todos os usuários cadastrados.
- **Autenticação:** Requerida.

#### Respostas Esperadas

- **`200 OK`**: Lista de usuários retornada com sucesso.
    - **Corpo da Resposta (Array de `UserDto`):**
      ```json
      [
          {
              "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
              "name": "Fulano da Silva",
              "email": "fulano@example.com",
              "salary": 3000.00,
              "createdAt": "2025-05-26T14:32:00",
              "updatedAt": "2025-05-27T09:15:20"
          },
          {
              "id": "ab123c45-6789-0123-b4fc-5d074e77bfa7",
              "name": "Ciclana de Souza",
              "email": "ciclana@example.com",
              "salary": 4500.50,
              "createdAt": "2025-05-28T11:00:00",
              "updatedAt": "2025-05-29T16:45:10"
          }
      ]
      ```
      Se não houver usuários, retorna uma lista vazia `[]`.

#### Possíveis Erros

- **`401 Unauthorized`**: Token JWT ausente, inválido ou expirado.
- **`403 Forbidden`**: O usuário autenticado não tem permissão para acessar este recurso.

### 2.2. Buscar Usuário por ID

- **Endpoint:** `GET /api/users/{id}`
- **Funcionalidade:** Retorna os dados de um usuário específico com base no seu ID.
- **Autenticação:** Requerida.

#### Parâmetros de Caminho

| Parâmetro | Tipo   | Obrigatório | Descrição        | Exemplo                                |
|-----------|--------|-------------|------------------|----------------------------------------|
| `id`      | UUID   | Sim         | ID do usuário.   | `3fa85f64-5717-4562-b3fc-2c963f66afa6` |

#### Respostas Esperadas

- **`200 OK`**: Usuário encontrado.
    - **Corpo da Resposta (`UserDto`):**
      ```json
      {
          "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
          "name": "Fulano da Silva",
          "email": "fulano@example.com",
          "salary": 3000.00,
          "createdAt": "2025-05-26T14:32:00",
          "updatedAt": "2025-05-27T09:15:20"
      }
      ```

#### Possíveis Erros

- **`401 Unauthorized`**: Token JWT ausente, inválido ou expirado.
- **`403 Forbidden`**: O usuário autenticado não tem permissão para acessar este recurso (ex: tentar acessar dados de outro usuário, se a regra de negócio impedir).
- **`404 Not Found`**: Usuário com o ID especificado não encontrado.
    - **Exemplo de Corpo da Resposta:**
      ```json
      {
          "timestamp": "2025-05-31T10:10:00Z",
          "status": 404,
          "error": "Not Found",
          "message": "Usuário não encontrado com ID: 3fa85f64-5717-4562-b3fc-2c963f66afa6",
          "path": "/api/users/3fa85f64-5717-4562-b3fc-2c963f66afa6"
      }
      ```

### 2.3. Atualizar Usuário

- **Endpoint:** `PUT /api/users/{id}`
- **Funcionalidade:** Atualiza os dados de um usuário existente. Permite atualizar nome, senha e/ou salário. O e-mail não pode ser alterado por este endpoint.
- **Autenticação:** Requerida. O usuário só pode atualizar seus próprios dados (a menos que seja um administrador, dependendo das regras de negócio implementadas no `UserService`).

#### Parâmetros de Caminho

| Parâmetro | Tipo   | Obrigatório | Descrição                  | Exemplo                                |
|-----------|--------|-------------|----------------------------|----------------------------------------|
| `id`      | UUID   | Sim         | ID do usuário a ser atualizado. | `3fa85f64-5717-4562-b3fc-2c963f66afa6` |

#### Corpo da Requisição (`UserUpdateDto`)

| Campo    | Tipo       | Obrigatório | Descrição                                                                                                | Validações                                                                                                                                                                                                                                                                                                                         | Exemplo                     |
|----------|------------|-------------|----------------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------|
| `name`   | String     | Não         | Novo nome completo do usuário.                                                                           | Se fornecido, deve ter entre 5 e 100 caracteres. Deve conter ao menos duas palavras, cada uma com no mínimo 2 letras. Somente letras e apóstrofos são permitidos. Não pode ser vazio ou conter apenas espaços.                                                                                                                             | "Fulano de Tal Silva"       |
| `password` | String     | Não         | Nova senha de acesso do usuário.                                                                         | Se fornecida, deve ter no mínimo 6 caracteres e conter ao menos uma letra e um número. Não pode ser em branco se o campo estiver presente.                                                                                                                                                                                                | "NovaSenha@456"             |
| `salary` | BigDecimal | Não         | Novo salário mensal do usuário.                                                                          | Se fornecido, deve ser um valor numérico maior ou igual a zero.                                                                                                                                                                                                                                                                    | `3800.75`                   |

*Nota: Pelo menos um dos campos (`name`, `password`, `salary`) deve ser fornecido para atualização.*

#### Respostas Esperadas

- **`200 OK`**: Usuário atualizado com sucesso.
    - **Corpo da Resposta (`UserDto`):**
      ```json
      {
          "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
          "name": "Fulano de Tal Silva", // Nome atualizado
          "email": "fulano@example.com", // E-mail permanece o mesmo
          "salary": 3800.75, // Salário atualizado
          "createdAt": "2025-05-26T14:32:00",
          "updatedAt": "2025-05-31T10:15:00" // Timestamp de atualização modificado
      }
      ```

#### Possíveis Erros

- **`400 Bad Request`**: Dados de atualização inválidos.
    - **Motivos:**
        - `name`: Vazio, menos de duas palavras, caracteres inválidos, ou fora do tamanho permitido.
        - `password`: Menos de 6 caracteres, não contém letras e números.
        - `salary`: Valor negativo.
    - **Exemplo de Corpo da Resposta:**
      ```json
      {
          "timestamp": "2025-05-31T10:20:00Z",
          "status": 400,
          "error": "Bad Request",
          "message": "A nova senha está fraca, ela precisa possuir ao menos letras e números.", // Ou outra mensagem de validação específica
          "path": "/api/users/3fa85f64-5717-4562-b3fc-2c963f66afa6"
      }
      ```
- **`401 Unauthorized`**: Token JWT ausente, inválido ou expirado.
- **`403 Forbidden`**: Tentativa de atualizar dados de outro usuário sem permissão.
- **`404 Not Found`**: Usuário com o ID especificado não encontrado.
    - **Exemplo de Corpo da Resposta:**
      ```json
      {
          "timestamp": "2025-05-31T10:22:00Z",
          "status": 404,
          "error": "Not Found",
          "message": "Usuário não encontrado com ID: 3fa85f64-5717-4562-b3fc-2c963f66afa6",
          "path": "/api/users/3fa85f64-5717-4562-b3fc-2c963f66afa6"
      }
      ```

### 2.4. Remover Usuário

- **Endpoint:** `DELETE /api/users/{id}`
- **Funcionalidade:** Remove um usuário do sistema com base no seu ID.
- **Autenticação:** Requerida. Geralmente, apenas administradores ou o próprio usuário podem realizar esta ação.

#### Parâmetros de Caminho

| Parâmetro | Tipo   | Obrigatório | Descrição                  | Exemplo                                |
|-----------|--------|-------------|----------------------------|----------------------------------------|
| `id`      | UUID   | Sim         | ID do usuário a ser removido. | `3fa85f64-5717-4562-b3fc-2c963f66afa6` |

#### Respostas Esperadas

- **`204 No Content`**: Usuário removido com sucesso. Nenhum corpo de resposta.

#### Possíveis Erros

- **`401 Unauthorized`**: Token JWT ausente, inválido ou expirado.
- **`403 Forbidden`**: Tentativa de remover outro usuário sem permissão.
- **`404 Not Found`**: Usuário com o ID especificado não encontrado para exclusão.
    - **Exemplo de Corpo da Resposta:**
      ```json
      {
          "timestamp": "2025-05-31T10:25:00Z",
          "status": 404,
          "error": "Not Found",
          "message": "Usuário não encontrado com ID: 3fa85f64-5717-4562-b3fc-2c963f66afa6 para exclusão.",
          "path": "/api/users/3fa85f64-5717-4562-b3fc-2c963f66afa6"
      }
      ```

## 3. Modelos de Dados (DTOs)

### 3.1. `UserDto`
Representa os dados de um usuário retornados pela API.

| Campo       | Tipo          | Descrição                                  | Exemplo                                |
|-------------|---------------|--------------------------------------------|----------------------------------------|
| `id`        | UUID          | Identificador único do usuário.            | `3fa85f64-5717-4562-b3fc-2c963f66afa6` |
| `name`      | String        | Nome completo do usuário.                  | "Fulano da Silva"                      |
| `email`     | String        | E-mail de login.                           | "fulano@example.com"                   |
| `salary`    | BigDecimal    | Salário do usuário.                        | `3000.00`                              |
| `createdAt` | LocalDateTime | Timestamp de criação do registro.          | "2025-05-26T14:32:00"                  |
| `updatedAt` | LocalDateTime | Timestamp da última atualização do registro. | "2025-05-27T09:15:20"                  |

### 3.2. `UserRegisterDto`
Dados para registro de um novo usuário (detalhado na seção [1.1](#11-registrar-novo-usuário)).

### 3.3. `LoginDto`
Credenciais para autenticação (detalhado na seção [1.2](#12-autenticar-usuário-login)).

### 3.4. `UserUpdateDto`
Dados para atualização de um usuário existente (detalhado na seção [2.3](#23-atualizar-usuário)).

### 3.5. `ErrorResponseDto` (Implícito)
Estrutura padrão para respostas de erro.

| Campo       | Tipo          | Descrição                                                          |
|-------------|---------------|--------------------------------------------------------------------|
| `timestamp` | String        | Data e hora da ocorrência do erro (ISO 8601).                      |
| `status`    | Integer       | Código de status HTTP.                                             |
| `error`     | String        | Descrição curta do erro (ex: "Not Found", "Bad Request").          |
| `message`   | String        | Mensagem detalhada sobre o erro.                                   |
| `path`      | String        | O caminho do endpoint que foi chamado.                             |
| `details`   | Array<String> | (Opcional) Lista de mensagens de erro específicas (validação).     |




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


