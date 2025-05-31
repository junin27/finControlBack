# finControlBack

## Visão Geral

Bem-vindo à documentação da API do finControlBack. Esta API permite o gerenciamento completo de suas finanças pessoais, incluindo autenticação de usuários, categorias de transações, contas bancárias e cofres para objetivos financeiros.

## Convenções Gerais da API

As seguintes convenções aplicam-se a todos os endpoints da API:

-   **URL Base:** Todos os endpoints são prefixados com a URL base da sua aplicação (ex: `http://localhost:8080`).
-   **Autenticação:**
    -   Endpoints públicos (como `/auth/register` e `/auth/login`) não requerem autenticação.
    -   Todos os outros endpoints sob o prefixo `/api` (ex: `/api/users`, `/api/categories`, `/api/banks`, `/api/vaults`) requerem um token JWT Bearer. Inclua o token no cabeçalho `Authorization` de suas requisições:
        `Authorization: Bearer <seu_token_jwt>`
-   **Formato de Resposta:** As respostas são sempre em formato JSON.
-   **Respostas de Erro Padrão (`ErrorResponseDto`):** Erros são retornados com um status HTTP apropriado e um corpo JSON contendo:
    -   `timestamp`: Data e hora da ocorrência do erro (ISO 8601).
    -   `status`: Código de status HTTP.
    -   `error`: Descrição curta do erro (ex: "Not Found", "Bad Request", "Unauthorized", "Forbidden", "Conflict").
    -   `message`: Mensagem detalhada sobre o erro.
    -   `path`: O caminho do endpoint que foi chamado.
    -   `details` (opcional): Uma lista de mensagens de erro específicas, geralmente para erros de validação de campos.

---

## 1. Autenticação e Gerenciamento de Usuários

Esta seção cobre o registro, login e gerenciamento de dados de usuários.

### 1.1. Endpoints de Autenticação

Controlador responsável: `AuthController`
Rota base: `/auth`

#### 1.1.1. Registrar Novo Usuário

-   **Endpoint:** `POST /auth/register`
-   **Funcionalidade:** Cria um novo usuário no sistema.
-   **Autenticação:** Nenhuma autenticação é necessária.

##### Corpo da Requisição (`UserRegisterDto`)

| Campo             | Tipo         | Obrigatório | Descrição                                                                                                | Validações                                                                                                                                                                                                                                                                                                                         | Exemplo                     |
|-------------------|--------------|-------------|----------------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------|
| `name`            | String       | Sim         | Nome completo do usuário.                                                                                | Deve ter entre 5 e 100 caracteres. Deve conter ao menos duas palavras, cada uma com no mínimo 2 letras. Somente letras e apóstrofos são permitidos. Ex: "João Silva".                                                                                                                                                              | "Fulano de Tal"             |
| `email`           | String       | Sim         | Endereço de e-mail do usuário.                                                                           | Deve ser um e-mail válido e único no sistema. Máximo de 150 caracteres.                                                                                                                                                                                                                                                              | "fulano.tal@example.com"    |
| `password`        | String       | Sim         | Senha de acesso do usuário.                                                                              | Deve ter no mínimo 6 e no máximo 100 caracteres. Deve conter ao menos uma letra e um número.                                                                                                                                                                                                                                         | "Senha@123"                 |
| `confirmPassword` | String       | Sim         | Confirmação da senha.                                                                                    | Deve ser idêntica ao campo `password`. Mínimo de 6 e máximo de 100 caracteres.                                                                                                                                                                                                                                                      | "Senha@123"                 |
| `salary`          | BigDecimal   | Sim         | Salário mensal inicial do usuário.                                                                       | Deve ser um valor numérico maior ou igual a zero.                                                                                                                                                                                                                                                                                  | `3500.00`                   |

##### Respostas Esperadas

-   **`201 Created`**: Usuário registrado com sucesso.
    -   **Corpo da Resposta (`UserDto`):**
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
    -   **Headers:** `Location` contendo a URI do novo recurso criado (ex: `/auth/register/3fa85f64-5717-4562-b3fc-2c963f66afa6`).

##### Possíveis Erros

-   **`400 Bad Request`**: Dados de registro inválidos.
    -   **Motivos:**
        -   Campos obrigatórios faltando.
        -   `name`: Não informado, menos de duas palavras, caracteres inválidos, ou fora do tamanho permitido (5-100 caracteres).
        -   `email`: Formato inválido ou tamanho excedido.
        -   `password`: Menos de 6 caracteres, não contém letras e números.
        -   `confirmPassword`: Não confere com a senha informada.
        -   `salary`: Valor negativo.
    -   **Exemplo de Corpo da Resposta (Erro de Validação Múltipla):**
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
    -   **Exemplo de Corpo da Resposta (Senhas Não Conferem):**
        ```json
        {
            "timestamp": "2025-05-30T22:01:00Z",
            "status": 400,
            "error": "Bad Request",
            "message": "As duas senhas não conferem, elas precisam ter os mesmos caracteres exatamente iguais.",
            "path": "/auth/register"
        }
        ```
-   **`409 Conflict`**: O e-mail fornecido já está cadastrado no sistema.
    -   **Exemplo de Corpo da Resposta:**
        ```json
        {
            "timestamp": "2025-05-30T22:02:00Z",
            "status": 409,
            "error": "Conflict",
            "message": "O e-mail 'existente@example.com' já está cadastrado.",
            "path": "/auth/register"
        }
        ```

#### 1.1.2. Autenticar Usuário (Login)

-   **Endpoint:** `POST /auth/login`
-   **Funcionalidade:** Autentica um usuário existente e retorna um token JWT.
-   **Autenticação:** Nenhuma autenticação é necessária.

##### Corpo da Requisição (`LoginDto`)

| Campo    | Tipo   | Obrigatório | Descrição                               | Validações                                                                 | Exemplo                  |
|----------|--------|-------------|-----------------------------------------|----------------------------------------------------------------------------|--------------------------|
| `email`  | String | Sim         | Endereço de e-mail do usuário.          | Obrigatório. Deve ser um e-mail válido. Máximo de 150 caracteres.          | "joao.silva@example.com" |
| `password` | String | Sim         | Senha de acesso do usuário.             | Obrigatório.                                                               | "Senha@123"              |

##### Respostas Esperadas

-   **`200 OK`**: Autenticado com sucesso.
    -   **Corpo da Resposta:**
        ```json
        {
            "token": "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2FvLnNpbHZhQGV4YW1wbGUuY29tIiwiaWF0IjoxNzA5Mjg0MzIwLCJleHAiOjE3MDkzNzA3MjB9.exampleToken",
            "userId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
            "userName": "João Silva"
        }
        ```
        -   `token`: Token JWT para ser usado em requisições autenticadas.
        -   `userId`: ID do usuário autenticado.
        -   `userName`: Nome do usuário autenticado.

##### Possíveis Erros

-   **`400 Bad Request`**: Dados de login inválidos.
    -   **Motivos:** E-mail ou senha não fornecidos ou em formato incorreto.
    -   **Exemplo de Corpo da Resposta (Campo Faltando):**
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
-   **`401 Unauthorized`**: Credenciais inválidas (senha incorreta).
    -   **Exemplo de Corpo da Resposta:**
        ```json
        {
            "timestamp": "2025-05-30T22:04:00Z",
            "status": 401,
            "error": "Unauthorized",
            "message": "A sua senha está incorreta.",
            "path": "/auth/login"
        }
        ```
-   **`404 Not Found`**: O e-mail fornecido não está cadastrado no sistema.
    -   **Exemplo de Corpo da Resposta:**
        ```json
        {
            "timestamp": "2025-05-30T22:05:00Z",
            "status": 404,
            "error": "Not Found",
            "message": "O email 'naoexiste@example.com' não está cadastrado no sistema.",
            "path": "/auth/login"
        }
        ```

### 1.2. Endpoints de Gerenciamento de Usuários

Controlador responsável: `UserController`
Rota base: `/api/users`
**Autenticação:** Requerida para todos os endpoints nesta subseção.

#### 1.2.1. Listar Todos os Usuários

-   **Endpoint:** `GET /api/users`
-   **Funcionalidade:** Retorna uma lista de todos os usuários cadastrados. (Geralmente, este endpoint é restrito a administradores).
-   **Autenticação:** Requerida.

##### Respostas Esperadas

-   **`200 OK`**: Lista de usuários retornada com sucesso.
    -   **Corpo da Resposta (Array de `UserDto`):**
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

##### Possíveis Erros

-   **`401 Unauthorized`**: Token JWT ausente, inválido ou expirado.
-   **`403 Forbidden`**: O usuário autenticado não tem permissão para acessar este recurso.

#### 1.2.2. Buscar Usuário por ID

-   **Endpoint:** `GET /api/users/{id}`
-   **Funcionalidade:** Retorna os dados de um usuário específico com base no seu ID. (O usuário autenticado pode buscar seus próprios dados, ou um administrador pode buscar qualquer usuário).
-   **Autenticação:** Requerida.

##### Parâmetros de Caminho

| Parâmetro | Tipo   | Obrigatório | Descrição        | Exemplo                                |
|-----------|--------|-------------|------------------|----------------------------------------|
| `id`      | UUID   | Sim         | ID do usuário.   | `3fa85f64-5717-4562-b3fc-2c963f66afa6` |

##### Respostas Esperadas

-   **`200 OK`**: Usuário encontrado.
    -   **Corpo da Resposta (`UserDto`):**
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

##### Possíveis Erros

-   **`401 Unauthorized`**: Token JWT ausente, inválido ou expirado.
-   **`403 Forbidden`**: O usuário autenticado não tem permissão para acessar os dados deste usuário específico.
-   **`404 Not Found`**: Usuário com o ID especificado não encontrado.
    -   **Exemplo de Corpo da Resposta:**
        ```json
        {
            "timestamp": "2025-05-31T10:10:00Z",
            "status": 404,
            "error": "Not Found",
            "message": "Usuário não encontrado com ID: 3fa85f64-5717-4562-b3fc-2c963f66afa6",
            "path": "/api/users/3fa85f64-5717-4562-b3fc-2c963f66afa6"
        }
        ```

#### 1.2.3. Atualizar Usuário

-   **Endpoint:** `PUT /api/users/{id}`
-   **Funcionalidade:** Atualiza os dados de um usuário existente. Permite atualizar nome, senha e/ou salário. O e-mail não pode ser alterado por este endpoint.
-   **Autenticação:** Requerida. O usuário só pode atualizar seus próprios dados (a menos que seja um administrador).

##### Parâmetros de Caminho

| Parâmetro | Tipo   | Obrigatório | Descrição                  | Exemplo                                |
|-----------|--------|-------------|----------------------------|----------------------------------------|
| `id`      | UUID   | Sim         | ID do usuário a ser atualizado. | `3fa85f64-5717-4562-b3fc-2c963f66afa6` |

##### Corpo da Requisição (`UserUpdateDto`)

*Pelo menos um dos campos (`name`, `password`, `salary`) deve ser fornecido para atualização.*

| Campo    | Tipo       | Obrigatório | Descrição                                                                                                | Validações                                                                                                                                                                                                                                                                                                                         | Exemplo                     |
|----------|------------|-------------|----------------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------|
| `name`   | String     | Não         | Novo nome completo do usuário.                                                                           | Se fornecido, deve ter entre 5 e 100 caracteres. Deve conter ao menos duas palavras, cada uma com no mínimo 2 letras. Somente letras e apóstrofos são permitidos. Não pode ser vazio ou conter apenas espaços.                                                                                                                             | "Fulano de Tal Silva"       |
| `password` | String     | Não         | Nova senha de acesso do usuário.                                                                         | Se fornecida, deve ter no mínimo 6 caracteres e conter ao menos uma letra e um número. Não pode ser em branco se o campo estiver presente.                                                                                                                                                                                                | "NovaSenha@456"             |
| `salary` | BigDecimal | Não         | Novo salário mensal do usuário.                                                                          | Se fornecido, deve ser um valor numérico maior ou igual a zero.                                                                                                                                                                                                                                                                    | `3800.75`                   |

##### Respostas Esperadas

-   **`200 OK`**: Usuário atualizado com sucesso.
    -   **Corpo da Resposta (`UserDto`):**
        ```json
        {
            "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
            "name": "Fulano de Tal Silva",
            "email": "fulano@example.com",
            "salary": 3800.75,
            "createdAt": "2025-05-26T14:32:00",
            "updatedAt": "2025-05-31T10:15:00"
        }
        ```

##### Possíveis Erros

-   **`400 Bad Request`**: Dados de atualização inválidos.
    -   **Motivos:**
        -   `name`: Vazio, menos de duas palavras, caracteres inválidos, ou fora do tamanho permitido.
        -   `password`: Menos de 6 caracteres, não contém letras e números.
        -   `salary`: Valor negativo.
    -   **Exemplo de Corpo da Resposta:**
        ```json
        {
            "timestamp": "2025-05-31T10:20:00Z",
            "status": 400,
            "error": "Bad Request",
            "message": "A nova senha está fraca, ela precisa possuir ao menos letras e números.",
            "path": "/api/users/3fa85f64-5717-4562-b3fc-2c963f66afa6"
        }
        ```
-   **`401 Unauthorized`**: Token JWT ausente, inválido ou expirado.
-   **`403 Forbidden`**: Tentativa de atualizar dados de outro usuário sem permissão.
-   **`404 Not Found`**: Usuário com o ID especificado não encontrado.

#### 1.2.4. Remover Usuário

-   **Endpoint:** `DELETE /api/users/{id}`
-   **Funcionalidade:** Remove um usuário do sistema com base no seu ID.
-   **Autenticação:** Requerida. Geralmente, apenas administradores ou o próprio usuário podem realizar esta ação.

##### Parâmetros de Caminho

| Parâmetro | Tipo   | Obrigatório | Descrição                  | Exemplo                                |
|-----------|--------|-------------|----------------------------|----------------------------------------|
| `id`      | UUID   | Sim         | ID do usuário a ser removido. | `3fa85f64-5717-4562-b3fc-2c963f66afa6` |

##### Respostas Esperadas

-   **`204 No Content`**: Usuário removido com sucesso. Nenhum corpo de resposta.

##### Possíveis Erros

-   **`401 Unauthorized`**: Token JWT ausente, inválido ou expirado.
-   **`403 Forbidden`**: Tentativa de remover outro usuário sem permissão.
-   **`404 Not Found`**: Usuário com o ID especificado não encontrado para exclusão.

### 1.3. Modelos de Dados (DTOs) para Autenticação e Usuários

#### 1.3.1. `UserDto`
Representa os dados de um usuário retornados pela API.

| Campo       | Tipo          | Descrição                                  | Exemplo                                |
|-------------|---------------|--------------------------------------------|----------------------------------------|
| `id`        | UUID          | Identificador único do usuário.            | `3fa85f64-5717-4562-b3fc-2c963f66afa6` |
| `name`      | String        | Nome completo do usuário.                  | "Fulano da Silva"                      |
| `email`     | String        | E-mail de login.                           | "fulano@example.com"                   |
| `salary`    | BigDecimal    | Salário do usuário.                        | `3000.00`                              |
| `createdAt` | LocalDateTime | Timestamp de criação do registro.          | "2025-05-26T14:32:00"                  |
| `updatedAt` | LocalDateTime | Timestamp da última atualização do registro. | "2025-05-27T09:15:20"                  |

#### 1.3.2. `UserRegisterDto`
Dados para registro de um novo usuário (detalhado na seção [1.1.1](#111-registrar-novo-usuário)).

#### 1.3.3. `LoginDto`
Credenciais para autenticação (detalhado na seção [1.1.2](#112-autenticar-usuário-login)).

#### 1.3.4. `UserUpdateDto`
Dados para atualização de um usuário existente (detalhado na seção [1.2.3](#123-atualizar-usuário)).

*(Nota: `UserSimpleDto` e `ErrorResponseDto` são descritos em outras seções ou nas Convenções Gerais).*

---

## 2. Gerenciamento de Categorias

Esta seção detalha os endpoints para o gerenciamento de categorias de despesas e receitas do usuário.

Controlador responsável: `CategoryController`
Rota base: `/api/categories`
**Autenticação:** Requerida para todos os endpoints nesta seção. O sistema identifica o usuário autenticado através do token JWT para garantir que as operações sejam realizadas apenas nas categorias pertencentes a ele.

### 2.1. Endpoints de Categoria

#### 2.1.1. Listar Todas as Categorias do Usuário

-   **Endpoint:** `GET /api/categories`
-   **Funcionalidade:** Retorna uma lista de todas as categorias pertencentes ao usuário autenticado.

##### Respostas Esperadas

-   **`200 OK`**: Lista de categorias retornada com sucesso.
    -   **Corpo da Resposta (Array de `CategoryDetailResponseDto`):**
        ```json
        [
            {
                "user": {
                    "id": "2ec7d1c2-a306-4ffe-9603-dc39408d5241",
                    "name": "Usuário Exemplo"
                },
                "category": {
                    "id": "7fa85f64-1234-4562-b3fc-2c963f66afa6",
                    "name": "Alimentação",
                    "description": "Gastos com supermercado e restaurantes",
                    "createdAt": "2025-05-18T13:45:00",
                    "updatedAt": "2025-05-18T14:00:00"
                }
            }
        ]
        ```
        Se o usuário não possuir categorias, retorna uma lista vazia `[]`.

##### Possíveis Erros

-   **`401 Unauthorized`**: Token JWT ausente, inválido ou expirado.
-   **`404 Not Found`**: Usuário autenticado não encontrado no sistema.

#### 2.1.2. Buscar Categoria por ID

-   **Endpoint:** `GET /api/categories/{id}`
-   **Funcionalidade:** Retorna os dados de uma categoria específica do usuário autenticado.

##### Parâmetros de Caminho

| Parâmetro | Tipo   | Obrigatório | Descrição           | Exemplo                                |
|-----------|--------|-------------|---------------------|----------------------------------------|
| `id`      | UUID   | Sim         | ID da categoria.    | `7fa85f64-1234-4562-b3fc-2c963f66afa6` |

##### Respostas Esperadas

-   **`200 OK`**: Categoria encontrada.
    -   **Corpo da Resposta (`CategoryDetailResponseDto`):** (Estrutura como no exemplo de Listar Todas)

##### Possíveis Erros

-   **`401 Unauthorized`**.
-   **`404 Not Found`**: Categoria não encontrada ou não pertence ao usuário.

#### 2.1.3. Criar Nova Categoria

-   **Endpoint:** `POST /api/categories`
-   **Funcionalidade:** Cria uma nova categoria para o usuário autenticado.

##### Corpo da Requisição (`CategoryCreateDto`)

| Campo         | Tipo   | Obrigatório | Descrição                                                                 | Validações                                                                 | Exemplo                                   |
|---------------|--------|-------------|---------------------------------------------------------------------------|----------------------------------------------------------------------------|-------------------------------------------|
| `name`        | String | Sim         | Nome da categoria.                                                        | Obrigatório. 1-100 caracteres.                                           | "Lazer"                                   |
| `description` | String | Não         | Descrição opcional. Padrão: "Campo não Informado pelo Usuário".           | Máx 255 caracteres.                                                        | "Gastos com cinema, parques, shows, etc." |

##### Respostas Esperadas

-   **`201 Created`**: Categoria criada.
    -   **Corpo da Resposta (`CategoryDetailResponseDto`):** (Estrutura como no exemplo de Listar Todas, com os dados da nova categoria)
    -   **Headers:** `Location` com a URI do novo recurso.

##### Possíveis Erros

-   **`400 Bad Request`**: Dados inválidos (ex: `name` faltando).
-   **`401 Unauthorized`**.
-   **`404 Not Found`**: Usuário autenticado não encontrado.

#### 2.1.4. Atualizar Categoria Existente

-   **Endpoint:** `PUT /api/categories/{id}`
-   **Funcionalidade:** Atualiza nome e/ou descrição de uma categoria.

##### Parâmetros de Caminho

| Parâmetro | Tipo   | Obrigatório | Descrição                     |
|-----------|--------|-------------|-------------------------------|
| `id`      | UUID   | Sim         | ID da categoria a atualizar.  |

##### Corpo da Requisição (`CategoryUpdateDto`)

*Pelo menos um campo deve ser fornecido.*

| Campo         | Tipo   | Obrigatório | Descrição                                                                                                | Validações                                                                                               |
|---------------|--------|-------------|----------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------|
| `name`        | String | Não         | Novo nome. Se fornecido, não pode ser vazio.                                                             | 1-100 caracteres.                                                                                        |
| `description` | String | Não         | Nova descrição. String vazia `""` para limpar.                                                           | Máx 255 caracteres.                                                                                      |

##### Respostas Esperadas

-   **`200 OK`**: Categoria atualizada.
    -   **Corpo da Resposta (`CategoryDetailResponseDto`):** (Dados atualizados da categoria)

##### Possíveis Erros

-   **`400 Bad Request`**: Dados inválidos.
-   **`401 Unauthorized`**.
-   **`404 Not Found`**: Categoria ou usuário não encontrado.

#### 2.1.5. Remover Categoria por ID

-   **Endpoint:** `DELETE /api/categories/{id}`
-   **Funcionalidade:** Remove uma categoria específica.

##### Parâmetros de Caminho

| Parâmetro | Tipo   | Obrigatório | Descrição                   |
|-----------|--------|-------------|-----------------------------|
| `id`      | UUID   | Sim         | ID da categoria a remover.  |

##### Respostas Esperadas

-   **`204 No Content`**: Categoria removida.

##### Possíveis Erros

-   **`400 Bad Request`**: Categoria em uso (ex: por despesas).
-   **`401 Unauthorized`**.
-   **`404 Not Found`**: Categoria ou usuário não encontrado.

#### 2.1.6. Atualizar TODAS as Categorias do Usuário (Em Lote)

-   **Endpoint:** `PUT /api/categories/user-all`
-   **Funcionalidade:** Atualiza todas as categorias do usuário com os mesmos dados.

##### Corpo da Requisição (`CategoryMassUpdateDto`)

*Pelo menos um campo deve ser fornecido.*

| Campo         | Tipo   | Obrigatório | Descrição                                                                                                | Validações                                                                                               |
|---------------|--------|-------------|----------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------|
| `name`        | String | Não         | Novo nome para TODAS as categorias. Se fornecido, não pode ser vazio.                                    | 1-100 caracteres.                                                                                        |
| `description` | String | Não         | Nova descrição para TODAS as categorias. String vazia `""` para limpar.                                  | Máx 255 caracteres.                                                                                      |

##### Respostas Esperadas

-   **`200 OK`**: Categorias atualizadas.
    -   **Corpo da Resposta (Array de `CategoryDetailResponseDto`):** Lista de todas as categorias do usuário com dados atualizados.

##### Possíveis Erros

-   **`400 Bad Request`**: Dados inválidos.
-   **`401 Unauthorized`**.
-   **`404 Not Found`**: Usuário não encontrado.

#### 2.1.7. Deletar TODAS as Categorias do Usuário (Em Lote)

-   **Endpoint:** `DELETE /api/categories/user-all`
-   **Funcionalidade:** Deleta todas as categorias do usuário.

##### Respostas Esperadas

-   **`204 No Content`**: Categorias deletadas.

##### Possíveis Erros

-   **`400 Bad Request`**: Alguma categoria em uso.
-   **`401 Unauthorized`**.
-   **`404 Not Found`**: Usuário não encontrado.

### 2.2. Modelos de Dados (DTOs) para Categorias

#### 2.2.1. `CategoryCreateDto`
(Descrito em [2.1.3](#213-criar-nova-categoria))

#### 2.2.2. `CategoryUpdateDto`
(Descrito em [2.1.4](#214-atualizar-categoria-existente))

#### 2.2.3. `CategoryMassUpdateDto`
(Descrito em [2.1.6](#216-atualizar-todas-as-categorias-do-usuário-em-lote))

#### 2.2.4. `CategoryDataDto`
Representa os dados detalhados de uma categoria.

| Campo         | Tipo          | Descrição                                  |
|---------------|---------------|--------------------------------------------|
| `id`          | UUID          | Identificador único da categoria.          |
| `name`        | String        | Nome da categoria.                         |
| `description` | String        | Descrição detalhada da categoria.          |
| `createdAt`   | LocalDateTime | Timestamp de criação da categoria.         |
| `updatedAt`   | LocalDateTime | Timestamp da última atualização.           |

#### 2.2.5. `UserSimpleDto`
Representa dados simplificados do usuário.

| Campo | Tipo   | Descrição                 |
|-------|--------|---------------------------|
| `id`  | UUID   | Identificador único do usuário. |
| `name`| String | Nome do usuário.          |

#### 2.2.6. `CategoryDetailResponseDto`
Resposta padrão para operações de categoria.

| Campo      | Tipo              | Descrição                         |
|------------|-------------------|-----------------------------------|
| `user`     | `UserSimpleDto`   | Dados simplificados do usuário.   |
| `category` | `CategoryDataDto` | Dados detalhados da categoria.    |

#### 2.2.7. `CategoryDto` (Referência)
DTO mais completo, pode ser usado internamente.

| Campo         | Tipo          | Descrição                                                                 |
|---------------|---------------|---------------------------------------------------------------------------|
| `id`          | UUID          | UUID da categoria (somente leitura).                                      |
| `userId`      | UUID          | UUID do usuário dono da categoria (somente leitura).                      |
| `name`        | String        | Nome da categoria (obrigatório na criação, 1-100 caracteres).             |
| `description` | String        | Descrição detalhada (opcional, padrão se não informado).                  |
| `createdAt`   | LocalDateTime | Timestamp de criação (somente leitura).                                   |
| `updatedAt`   | LocalDateTime | Timestamp da última atualização (somente leitura).                        |

---

## 3. Gerenciamento de Bancos

Esta documentação descreve a API para gerenciamento de bancos.

Rota base: `/api/banks`
**Autenticação:** Requerida para todos os endpoints nesta seção.

### 3.1. Objeto Banco (`BankDto`)

Representa os dados detalhados de um banco.

| Campo            | Tipo          | Descrição                                                                       |
|------------------|---------------|---------------------------------------------------------------------------------|
| `id`             | UUID          | UUID do banco.                                                                  |
| `name`           | String        | Nome do banco.                                                                  |
| `description`    | String        | Descrição do banco.                                                             |
| `totalIncome`    | BigDecimal    | Soma total das receitas (entradas) associadas a este banco (calculado).         |
| `totalExpense`   | BigDecimal    | Soma total das despesas (saídas) associadas a este banco (calculado).           |
| `currentBalance` | BigDecimal    | Saldo atual do banco (armazenado e ajustado por operações).                   |
| `createdAt`      | LocalDateTime | Timestamp de criação do banco.                                                |
| `updatedAt`      | LocalDateTime | Timestamp da última atualização do banco.                                     |

*(Exemplo JSON do `BankDto` omitido para brevidade, estrutura conforme tabela)*

### 3.2. Endpoints de Banco

#### 3.2.1. Criar Novo Banco

-   **Endpoint**: `POST /api/banks`
-   **Corpo da Requisição** (`BankCreateDto`):
    -   `name` (String, obrigatório): Nome do banco.
    -   `description` (String, opcional): Descrição. Padrão: "Campo não Informado pelo Usuário".
    -   `initialBalance` (BigDecimal, opcional): Saldo inicial. Padrão: 0.
-   **Retorno Sucesso**: `201 Created` com `BankDto` e header `Location`.
-   **Erros Possíveis**: `400 Bad Request`, `401 Unauthorized`, `500 Internal Server Error`.

#### 3.2.2. Listar Todos os Bancos

-   **Endpoint**: `GET /api/banks`
-   **Retorno Sucesso**: `200 OK` com array de `BankDto`.
-   **Erros Possíveis**: `401 Unauthorized`, `500 Internal Server Error`.

#### 3.2.3. Atualizar Banco Existente

-   **Endpoint**: `PUT /api/banks/{id}`
    -   `{id}` (UUID): ID do banco.
-   **Corpo da Requisição** (`BankUpdateDto`):
    -   `name` (String, opcional): Novo nome.
    -   `description` (String, opcional): Nova descrição.
    -   `balance` (BigDecimal, opcional): Novo saldo (ajusta diretamente).
-   **Retorno Sucesso**: `200 OK` com `BankDto` atualizado.
-   **Erros Possíveis**: `400 Bad Request`, `401 Unauthorized`, `403 Forbidden`, `404 Not Found`, `500 Internal Server Error`.

#### 3.2.4. Remover Banco por ID

-   **Endpoint**: `DELETE /api/banks/{id}`
    -   `{id}` (UUID): ID do banco.
-   **Retorno Sucesso**: `204 No Content`.
-   **Erros Possíveis**: `401 Unauthorized`, `403 Forbidden`, `404 Not Found`, `500 Internal Server Error`.
    *(Nota: Receitas e despesas associadas são removidas em cascata).*

#### 3.2.5. Remover Todas as Receitas de um Banco

-   **Endpoint**: `DELETE /api/banks/{id}/clear-incomes`
    -   `{id}` (UUID): ID do banco.
-   **Retorno Sucesso**: `204 No Content`. (Saldo do banco é atualizado).
-   **Erros Possíveis**: `401 Unauthorized`, `403 Forbidden`, `404 Not Found`, `500 Internal Server Error`.

#### 3.2.6. Remover Todas as Despesas de um Banco

-   **Endpoint**: `DELETE /api/banks/{id}/clear-expenses`
    -   `{id}` (UUID): ID do banco.
-   **Retorno Sucesso**: `204 No Content`. (Saldo do banco é atualizado).
-   **Erros Possíveis**: `401 Unauthorized`, `403 Forbidden`, `404 Not Found`, `500 Internal Server Error`.

### 3.3. Modelos de Dados (DTOs) para Bancos

-   **`BankDto`**: Descrito em [3.1](#31-objeto-banco-bankdto).
-   **`BankCreateDto`**:
    -   `name`: String
    -   `description`: String (opcional)
    -   `initialBalance`: BigDecimal (opcional)
-   **`BankUpdateDto`**:
    -   `name`: String (opcional)
    -   `description`: String (opcional)
    -   `balance`: BigDecimal (opcional)

### 3.4. Considerações Importantes para Bancos

-   **Propriedade de Dados**: Usuários só interagem com seus próprios bancos.
-   **Saldo do Banco (`currentBalance`)**: É o saldo autoritativo, ajustado por diversas operações.
-   **Campos Calculados em `BankDto`**: `totalIncome` e `totalExpense` são calculados no momento da consulta.
-   **Exclusão em Cascata**: Remover um banco remove suas receitas e despesas associadas.

---

## 4. Gerenciamento de Cofres

Esta documentação descreve a API para gerenciamento de cofres.

Rota base: `/api/vaults`
**Autenticação:** Requerida para todos os endpoints nesta seção.

### 4.1. Objeto Cofre (`VaultDto`)

Representa os dados detalhados de um cofre.

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

*(Exemplo JSON do `VaultDto` omitido para brevidade, estrutura conforme tabela)*

### 4.2. Endpoints de Cofre

#### 4.2.1. Criar Novo Cofre

-   **Endpoint**: `POST /api/vaults`
-   **Corpo da Requisição** (`VaultCreateDto`):
    -   `name` (String, obrigatório, max 100): Nome.
    -   `description` (String, opcional, max 255): Descrição. Padrão: "Campo não Informado pelo Usuário".
    -   `initialAmount` (BigDecimal, obrigatório, >= 0): Valor inicial.
    -   `currency` (String, obrigatório, max 10): Moeda.
    -   `bankId` (UUID, opcional): ID do banco para debitar `initialAmount`.
-   **Retorno Sucesso**: `201 Created` com `VaultDto` e header `Location`.
-   **Erros Possíveis**: `400 Bad Request` (dados inválidos, saldo insuficiente), `401 Unauthorized`, `404 Not Found` (usuário/banco), `500 Internal Server Error`.

#### 4.2.2. Listar Todos os Cofres do Usuário

-   **Endpoint**: `GET /api/vaults`
-   **Retorno Sucesso**: `200 OK` com array de `VaultDto`.
-   **Erros Possíveis**: `401 Unauthorized`, `404 Not Found` (usuário), `500 Internal Server Error`.

#### 4.2.3. Buscar Cofre por ID

-   **Endpoint**: `GET /api/vaults/{vaultId}`
    -   `{vaultId}` (UUID): ID do cofre.
-   **Retorno Sucesso**: `200 OK` com `VaultDto`.
-   **Erros Possíveis**: `401 Unauthorized`, `404 Not Found` (cofre/usuário), `500 Internal Server Error`.

#### 4.2.4. Listar Cofres por Banco

-   **Endpoint**: `GET /api/vaults/bank/{bankId}`
    -   `{bankId}` (UUID): ID do banco.
-   **Retorno Sucesso**: `200 OK` com array de `VaultDto` vinculados ao banco.
-   **Erros Possíveis**: `401 Unauthorized`, `404 Not Found` (banco/usuário), `500 Internal Server Error`.

#### 4.2.5. Atualizar Cofre Existente

-   **Endpoint**: `PUT /api/vaults/{vaultId}`
    -   `{vaultId}` (UUID): ID do cofre.
-   **Corpo da Requisição** (`VaultUpdateDto`):
    -   `name` (String, opcional, max 100): Novo nome.
    -   `description` (String, opcional, max 255): Nova descrição.
    -   `currency` (String, opcional, max 10): Nova moeda.
    *(Nota: Não altera `amount` ou `bankId`)*.
-   **Retorno Sucesso**: `200 OK` com `VaultDto` atualizado.
-   **Erros Possíveis**: `400 Bad Request`, `401 Unauthorized`, `404 Not Found` (cofre/usuário), `500 Internal Server Error`.

#### 4.2.6. Deletar Cofre

-   **Endpoint**: `DELETE /api/vaults/{vaultId}`
    -   `{vaultId}` (UUID): ID do cofre.
-   **Retorno Sucesso**: `204 No Content`.
    *(Nota: Se vinculado a banco, valor retorna ao saldo do banco. Se não vinculado e com saldo > 0, exclusão é impedida).*
-   **Erros Possíveis**: `400 Bad Request` (cofre não vinculado com saldo), `401 Unauthorized`, `404 Not Found` (cofre/usuário/banco associado), `500 Internal Server Error`.

### 4.3. Modelos de Dados (DTOs) para Cofres

-   **`VaultDto`**: Descrito em [4.1](#41-objeto-cofre-vaultdto).
-   **`VaultCreateDto`**:
    -   `name`: String
    -   `description`: String (opcional)
    -   `initialAmount`: BigDecimal
    -   `currency`: String
    -   `bankId`: UUID (opcional)
-   **`VaultUpdateDto`**:
    -   `name`: String (opcional)
    -   `description`: String (opcional)
    -   `currency`: String (opcional)

### 4.4. Considerações Importantes para Cofres

-   **Propriedade de Dados**: Usuários só interagem com seus próprios cofres.
-   **Interação com Saldo do Banco**: Na criação (se `bankId` fornecido) e deleção (se cofre estava vinculado).
-   **Atualização de Valor do Cofre**: Não por `PUT /api/vaults/{vaultId}`; requereria outros endpoints.
-   **Deleção de Cofre Não Vinculado com Saldo**: Impedida para evitar perda de fundos.

---

## 5. Gerenciamento de Renda Extra (Entradas de Dinheiro)

Esta seção detalha os endpoints para o gerenciamento de entradas de renda extra do usuário.

Controlador responsável: `ExtraIncomeController`
Rota base: `/api/extra-income`
**Autenticação:** Requerida para todos os endpoints nesta seção.

### 5.1. Objeto Renda Extra (`ExtraIncome` - Entidade Principal)

Representa uma entrada de renda extra.

| Campo         | Tipo          | Descrição                                                                 | Exemplo (quando aplicável) |
|---------------|---------------|---------------------------------------------------------------------------|----------------------------|
| `id`          | Long          | Identificador único da renda extra (gerado automaticamente).              | `101`                      |
| `user`        | Object        | Usuário proprietário (contém `id`, `name`, etc. - serialização pode variar). | `{ "id": "uuid-user", ... }` |
| `category`    | Object        | Categoria associada (contém `id`, `name`, etc. - serialização pode variar).| `{ "id": "uuid-category", ... }` |
| `amount`      | BigDecimal    | Valor da renda extra.                                                     | `350.00`                   |
| `bank`        | Object        | Banco associado (opcional, contém `id`, `name`, etc. - serialização pode variar). | `{ "id": "uuid-bank", ... }` ou `null` |
| `description` | String        | Descrição opcional da renda extra.                                        | "Freelance de design"      |
| `date`        | LocalDate     | Data da entrada da renda extra.                                           | "2025-05-18"               |

*(Nota: A representação exata de `user`, `category` e `bank` no JSON de resposta pode variar dependendo da configuração de serialização do Jackson e do estado de carregamento das entidades LAZY. Geralmente, incluirão pelo menos os IDs).*

### 5.2. Endpoints de Renda Extra

#### 5.2.1. Criar Nova Renda Extra

-   **Endpoint:** `POST /api/extra-income`
-   **Funcionalidade:** Cria uma nova entrada de renda extra para o usuário autenticado.
-   **Autenticação:** Requerida.

##### Corpo da Requisição (`ExtraIncomeDto`)

| Campo         | Tipo       | Obrigatório | Descrição                                                     | Validações                                      | Exemplo                                   |
|---------------|------------|-------------|---------------------------------------------------------------|-------------------------------------------------|-------------------------------------------|
| `amount`      | BigDecimal | Sim         | Valor da renda extra.                                         | Positivo (`@Positive`).                         | `350.00`                                  |
| `categoryId`  | UUID       | Sim         | UUID da categoria associada à renda extra.                    | Obrigatório (`@NotNull`).                       | `e7a1c3b2-4f9d-4a3d-8c9e-1b2d3f4a5e6f`    |
| `description` | String     | Não         | Descrição opcional da renda extra.                            | Máximo de 255 caracteres (`@Size(max = 255)`).  | "Freelance de design"                     |
| `date`        | LocalDate  | Sim         | Data da entrada no formato `YYYY-MM-DD`.                      | Obrigatório (`@NotNull`).                       | "2025-05-18"                              |

*(Nota: A associação com um `Bank` não é feita através deste DTO de criação. A renda extra será criada sem um banco vinculado por este endpoint específico).*

##### Respostas Esperadas

-   **`201 Created`**: Renda extra criada com sucesso.
    -   **Corpo da Resposta (`ExtraIncome` - Entidade):**
        ```json
        {
            "id": 101,
            "user": {
                "id": "2ec7d1c2-a306-4ffe-9603-dc39408d5241",
                // ... outros campos do usuário conforme serialização
            },
            "category": {
                "id": "e7a1c3b2-4f9d-4a3d-8c9e-1b2d3f4a5e6f",
                // ... outros campos da categoria conforme serialização
            },
            "amount": 350.00,
            "bank": null, // Não vinculado a um banco por este endpoint
            "description": "Freelance de design",
            "date": "2025-05-18"
        }
        ```

##### Possíveis Erros

-   **`400 Bad Request`**: Dados de criação inválidos (ex: `amount` não positivo, `categoryId` ou `date` ausentes).
    -   **Exemplo de Corpo da Resposta:**
        ```json
        {
            "timestamp": "2025-05-31T12:00:00Z",
            "status": 400,
            "error": "Erro de Validação de Campo",
            "message": "Um ou mais campos falharam na validação. Veja os detalhes.",
            "path": "/api/extra-income",
            "details": [
                "amount: must be greater than 0" // Exemplo
            ]
        }
        ```
-   **`401 Unauthorized`**: Token JWT ausente, inválido ou expirado.
-   **`404 Not Found`**:
    -   Usuário autenticado não encontrado.
        -   Mensagem: `User not found with email {userEmail}`
    -   Categoria com o `categoryId` fornecido não encontrada.
        -   Mensagem: `Category not found with ID {categoryId}`

#### 5.2.2. Listar Todas as Rendas Extras do Usuário

-   **Endpoint:** `GET /api/extra-income`
-   **Funcionalidade:** Retorna uma lista de todas as rendas extras pertencentes ao usuário autenticado.
-   **Autenticação:** Requerida.

##### Respostas Esperadas

-   **`200 OK`**: Lista de rendas extras retornada com sucesso.
    -   **Corpo da Resposta (Array de `ExtraIncome` - Entidade):**
        ```json
        [
            {
                "id": 101,
                "user": { "id": "2ec7d1c2-a306-4ffe-9603-dc39408d5241", ... },
                "category": { "id": "e7a1c3b2-4f9d-4a3d-8c9e-1b2d3f4a5e6f", ... },
                "amount": 350.00,
                "bank": null,
                "description": "Freelance de design",
                "date": "2025-05-18"
            },
            {
                "id": 102,
                "user": { "id": "2ec7d1c2-a306-4ffe-9603-dc39408d5241", ... },
                "category": { "id": "f8b2d4c3-5e0e-5b4e-9d0f-2c3e4g5b6f7h", ... },
                "amount": 500.00,
                "bank": { "id": "a1b2c3d4-e5f6-7890-1234-567890abcdef", "name": "Banco Principal", ... }, // Exemplo com banco
                "description": "Venda de item usado",
                "date": "2025-05-20"
            }
        ]
        ```
        Se o usuário não possuir rendas extras, retorna uma lista vazia `[]`.

##### Possíveis Erros

-   **`401 Unauthorized`**: Token JWT ausente, inválido ou expirado.
-   **`404 Not Found`**: Usuário autenticado não encontrado.
    -   Mensagem: `User not found with email {userEmail}`

### 5.3. Modelos de Dados (DTOs) para Renda Extra

#### 5.3.1. `ExtraIncomeDto` (Criação)
Dados para registrar uma nova renda extra.

| Campo         | Tipo       | Obrigatório | Descrição                                                     |
|---------------|------------|-------------|---------------------------------------------------------------|
| `amount`      | BigDecimal | Sim         | Valor da renda extra (deve ser positivo).                     |
| `categoryId`  | UUID       | Sim         | UUID da categoria associada.                                  |
| `description` | String     | Não         | Descrição opcional (máx 255 caracteres).                      |
| `date`        | LocalDate  | Sim         | Data da entrada no formato `YYYY-MM-DD`.                      |

#### 5.3.2. `ExtraIncome` (Entidade Principal - Resposta)
Representa a estrutura completa de uma renda extra como retornada pela API (detalhada em [5.1](#51-objeto-renda-extra-extraincome---entidade-principal)).

#### 5.3.3. `ExtraIncomeSimpleDto` (Referência)
DTO simplificado para contextos específicos (ex: visualização em listas de contas a receber). Não é diretamente retornado pelos endpoints atuais de `/api/extra-income`.

| Campo         | Tipo       | Descrição                                                       |
|---------------|------------|-----------------------------------------------------------------|
| `id`          | Long       | ID da renda extra.                                              |
| `description` | String     | Descrição da renda extra.                                       |
| `value`       | BigDecimal | Valor da renda extra (corresponde ao `amount` da entidade).     |
| `bankId`      | UUID       | ID do banco associado (opcional).                               |
| `bankName`    | String     | Nome do banco associado (opcional).                             |

### 5.4. Considerações Importantes para Renda Extra

-   **Propriedade de Dados**: Todas as rendas extras são vinculadas ao usuário autenticado.
-   **Associação com Categoria**: Toda renda extra deve ser associada a uma categoria existente.
-   **Associação com Banco**:
    -   A entidade `ExtraIncome` permite uma associação opcional com um `Bank`.
    -   O endpoint de criação `POST /api/extra-income` **não** define uma associação com banco. Rendas extras criadas por este endpoint inicialmente não terão um `bankId`.
    -   Mecanismos para associar/desassociar uma renda extra a um banco ou registrar uma renda extra diretamente em um banco precisariam de endpoints adicionais ou modificações nos DTOs existentes.
-   **ID da Renda Extra**: Utiliza `Long` como tipo de identificador, diferentemente de outras entidades que usam `UUID`.
-   **Operações CRUD**: Atualmente, a API expõe apenas a criação (`POST`) e listagem (`GET`) de rendas extras. Funcionalidades de atualização e exclusão para rendas extras individuais não estão presentes nos endpoints de `ExtraIncomeController`.

---

## 6. Gerenciamento de Contas a Receber

Esta seção detalha os endpoints para o gerenciamento das contas a receber do usuário, que estão vinculadas a registros de Renda Extra.

Controlador responsável: `ReceivableController`
Rota base: `/api/receivables`
**Autenticação:** Requerida para todos os endpoints nesta seção.

### 6.1. Objeto Conta a Receber (`ReceivableResponseDto`)

Representa os dados detalhados de uma conta a receber, como retornado pela API.

| Campo                  | Tipo                     | Descrição                                                                 | Exemplo                                   |
|------------------------|--------------------------|---------------------------------------------------------------------------|-------------------------------------------|
| `id`                   | UUID                     | ID único da conta a receber (somente leitura).                            | `02d1a2b3-c4d5-e6f7-a8b9-c0d1e2f3a4b5`    |
| `extraIncome`          | `ExtraIncomeSimpleDto`   | Detalhes simplificados da Renda Extra associada.                          | (ver [6.3.3](#633-extraincomesimpledto))  |
| `receiptMethod`        | `ReceiptMethodEnum`      | Meio de recebimento esperado.                                             | `PIX`                                     |
| `dueDate`              | LocalDate                | Data de vencimento (prevista para recebimento).                           | `"2025-12-31"`                            |
| `automaticBankReceipt` | Boolean                  | Indica se o recebimento deve ser processado automaticamente com o banco.  | `true`                                    |
| `status`               | `ReceivableStatusEnum`   | Status atual da conta a receber.                                          | `PENDING`                                 |
| `user`                 | `UserSimpleDto`          | Dados simplificados do usuário proprietário (somente leitura).            | (ver [2.2.5](#225-usersimpledto))         |
| `createdAt`            | LocalDateTime            | Timestamp de criação do registro da conta a receber (somente leitura).    | `"2025-06-01T10:00:00"`                   |
| `updatedAt`            | LocalDateTime            | Timestamp da última atualização do registro (somente leitura).            | `"2025-06-01T10:05:00"`                   |

### 6.2. Endpoints de Contas a Receber

#### 6.2.1. Criar Nova Conta a Receber

-   **Endpoint:** `POST /api/receivables`
-   **Funcionalidade:** Cria uma nova conta a receber para o usuário autenticado, vinculada a um registro de `ExtraIncome` existente.
-   **Autenticação:** Requerida.

##### Corpo da Requisição (`ReceivableCreateDto`)

| Campo                  | Tipo                  | Obrigatório | Descrição                                                                 | Validações                                      | Exemplo        |
|------------------------|-----------------------|-------------|---------------------------------------------------------------------------|-------------------------------------------------|----------------|
| `extraIncomeId`        | Long                  | Sim         | ID do registro de `ExtraIncome` associado.                                | Obrigatório.                                    | `101`          |
| `receiptMethod`        | `ReceiptMethodEnum`   | Sim         | Meio de recebimento.                                                      | Obrigatório.                                    | `PIX`          |
| `dueDate`              | LocalDate             | Sim         | Data de vencimento para o recebimento (formato `YYYY-MM-DD`).             | Obrigatório. Não pode ser no passado.           | `"2025-12-31"` |
| `automaticBankReceipt` | Boolean               | Sim         | Indica se o recebimento deve ser processado automaticamente com o banco.  | Obrigatório.                                    | `true`         |

##### Respostas Esperadas

-   **`201 Created`**: Conta a receber criada com sucesso.
    -   **Corpo da Resposta (`ReceivableResponseDto`):** Detalhes da conta a receber criada (conforme estrutura em [6.1](#61-objeto-conta-a-receber-receivableresponsedto)).
    -   **Headers:** `Location` contendo a URI do novo recurso criado (ex: `/api/receivables/02d1a2b3-c4d5-e6f7-a8b9-c0d1e2f3a4b5`).

##### Possíveis Erros

-   **`400 Bad Request`**: Dados inválidos fornecidos.
    -   **Motivos:** Campos obrigatórios faltando, `extraIncomeId` inválido, `dueDate` no passado.
    -   **Exemplo de Corpo da Resposta:**
        ```json
        {
            "timestamp": "2025-06-01T11:00:00Z",
            "status": 400,
            "error": "Bad Request",
            "message": "Due date cannot be in the past.", // Ou outra mensagem específica
            "path": "/api/receivables"
        }
        ```
-   **`401 Unauthorized`**: Token JWT ausente, inválido ou expirado.
-   **`404 Not Found`**:
    -   Usuário autenticado não encontrado.
        -   Mensagem: `Authenticated user not found with email: {userEmail}`
    -   `ExtraIncome` com o `extraIncomeId` fornecido não encontrado ou não pertence ao usuário.
        -   Mensagem: `ExtraIncome with ID {extraIncomeId} not found or does not belong to the user.`
-   **`500 Internal Server Error`**: Erro inesperado no servidor.

#### 6.2.2. Listar Contas a Receber com Filtros Opcionais

-   **Endpoint:** `GET /api/receivables`
-   **Funcionalidade:** Lista todas as contas a receber do usuário autenticado, com suporte a paginação e filtros opcionais por status e intervalo de datas de vencimento.
-   **Autenticação:** Requerida.

##### Parâmetros de Query (Opcionais)

| Parâmetro   | Tipo                   | Descrição                                         | Exemplo        |
|-------------|------------------------|---------------------------------------------------|----------------|
| `status`    | `ReceivableStatusEnum` | Filtra pelo status da conta a receber.            | `PENDING`      |
| `startDate` | LocalDate              | Filtra por data de vencimento a partir de (YYYY-MM-DD). | `"2025-01-01"` |
| `endDate`   | LocalDate              | Filtra por data de vencimento até (YYYY-MM-DD).   | `"2025-12-31"` |
| `page`      | Integer                | Número da página (começa em 0). Padrão: `0`.      | `0`            |
| `size`      | Integer                | Tamanho da página. Padrão: `10`.                  | `20`           |
| `sort`      | String                 | Campo para ordenação (ex: `dueDate,asc` ou `dueDate,desc`). Padrão: `dueDate,asc`. | `dueDate,desc` |

##### Respostas Esperadas

-   **`200 OK`**: Lista paginada de contas a receber retornada com sucesso.
    -   **Corpo da Resposta (`Page<ReceivableResponseDto>`):** Objeto de paginação contendo a lista de `ReceivableResponseDto` e informações de paginação.
        ```json
        {
            "content": [
                // Array de ReceivableResponseDto (estrutura em 6.1)
            ],
            "pageable": {
                "sort": {
                    "sorted": true,
                    "unsorted": false,
                    "empty": false
                },
                "offset": 0,
                "pageNumber": 0,
                "pageSize": 10,
                "paged": true,
                "unpaged": false
            },
            "totalPages": 1,
            "totalElements": 2,
            "last": true,
            "size": 10,
            "number": 0,
            "sort": {
                "sorted": true,
                "unsorted": false,
                "empty": false
            },
            "numberOfElements": 2,
            "first": true,
            "empty": false
        }
        ```

##### Possíveis Erros

-   **`401 Unauthorized`**.
-   **`500 Internal Server Error`**.

#### 6.2.3. Buscar Conta a Receber Específica por ID

-   **Endpoint:** `GET /api/receivables/{id}`
-   **Funcionalidade:** Recupera os detalhes de uma conta a receber específica pelo seu ID, se pertencer ao usuário autenticado.
-   **Autenticação:** Requerida.

##### Parâmetros de Caminho

| Parâmetro | Tipo   | Obrigatório | Descrição                           | Exemplo                                   |
|-----------|--------|-------------|-------------------------------------|-------------------------------------------|
| `id`      | UUID   | Sim         | ID da conta a receber a ser buscada. | `02d1a2b3-c4d5-e6f7-a8b9-c0d1e2f3a4b5`    |

##### Respostas Esperadas

-   **`200 OK`**: Conta a receber encontrada.
    -   **Corpo da Resposta (`ReceivableResponseDto`):** Detalhes da conta a receber (conforme estrutura em [6.1](#61-objeto-conta-a-receber-receivableresponsedto)).

##### Possíveis Erros

-   **`401 Unauthorized`**.
-   **`403 Forbidden`**: (Implícito se a lógica de serviço restringir acesso, embora o controller atual use `findByIdAndUserId`).
-   **`404 Not Found`**: Conta a receber não encontrada ou não pertence ao usuário.
    -   Mensagem: `Receivable with ID {id} not found or does not belong to the user.`
-   **`500 Internal Server Error`**.

#### 6.2.4. Atualizar Conta a Receber Existente

-   **Endpoint:** `PATCH /api/receivables/{id}`
-   **Funcionalidade:** Atualiza uma conta a receber existente (ex: data de vencimento, meio de recebimento). Somente campos não nulos no corpo da requisição serão atualizados. Não é possível atualizar se já foi marcada como recebida.
-   **Autenticação:** Requerida.

##### Parâmetros de Caminho

| Parâmetro | Tipo   | Obrigatório | Descrição                              |
|-----------|--------|-------------|----------------------------------------|
| `id`      | UUID   | Sim         | ID da conta a receber a ser atualizada. |

##### Corpo da Requisição (`ReceivableUpdateDto`)

*Apenas os campos a serem alterados precisam ser fornecidos.*

| Campo                  | Tipo                  | Obrigatório | Descrição                                                                 | Validações                                      | Exemplo        |
|------------------------|-----------------------|-------------|---------------------------------------------------------------------------|-------------------------------------------------|----------------|
| `receiptMethod`        | `ReceiptMethodEnum`   | Não         | Novo meio de recebimento.                                                 | -                                               | `BANK_SLIP`    |
| `dueDate`              | LocalDate             | Não         | Nova data de vencimento (formato `YYYY-MM-DD`).                           | Não pode ser no passado.                        | `"2026-01-15"` |
| `automaticBankReceipt` | Boolean               | Não         | Alterar a flag de recebimento automático no banco.                        | -                                               | `false`        |

##### Respostas Esperadas

-   **`200 OK`**: Conta a receber atualizada com sucesso.
    -   **Corpo da Resposta (`ReceivableResponseDto`):** Detalhes atualizados da conta a receber.

##### Possíveis Erros

-   **`400 Bad Request`**: Dados inválidos ou operação não permitida.
    -   **Motivos:** Tentativa de atualizar uma conta já recebida (`status` `RECEIVED` ou `RECEIVED_LATE`), nova `dueDate` no passado.
    -   **Exemplo de Corpo da Resposta:**
        ```json
        {
            "timestamp": "2025-06-01T11:05:00Z",
            "status": 400,
            "error": "Bad Request",
            "message": "Cannot update a receivable that has already been marked as received. Status: RECEIVED",
            "path": "/api/receivables/02d1a2b3-c4d5-e6f7-a8b9-c0d1e2f3a4b5"
        }
        ```
-   **`401 Unauthorized`**.
-   **`403 Forbidden`**.
-   **`404 Not Found`**: Conta a receber não encontrada ou não pertence ao usuário.
-   **`500 Internal Server Error`**.

#### 6.2.5. Marcar Conta a Receber como Recebida Manualmente

-   **Endpoint:** `PUT /api/receivables/{id}/mark-as-received`
-   **Funcionalidade:** Marca uma conta a receber com status `PENDING` ou `OVERDUE` como `RECEIVED` ou `RECEIVED_LATE`, respectivamente. Se `automaticBankReceipt` for `true` e a `ExtraIncome` associada tiver um banco vinculado, o saldo do banco é atualizado.
-   **Autenticação:** Requerida.

##### Parâmetros de Caminho

| Parâmetro | Tipo   | Obrigatório | Descrição                                              |
|-----------|--------|-------------|--------------------------------------------------------|
| `id`      | UUID   | Sim         | ID da conta a receber a ser marcada como recebida.     |

##### Respostas Esperadas

-   **`200 OK`**: Conta a receber marcada como recebida/recebida com atraso com sucesso.
    -   **Corpo da Resposta (`ReceivableResponseDto`):** Detalhes atualizados da conta a receber.

##### Possíveis Erros

-   **`400 Bad Request`**: Operação inválida.
    -   **Motivos:** Conta já marcada como recebida.
    -   Mensagem: `Receivable with ID {id} has already been marked as received.`
-   **`401 Unauthorized`**.
-   **`403 Forbidden`**.
-   **`404 Not Found`**: Conta a receber não encontrada, ou banco associado (para recebimento automático) não encontrado.
-   **`500 Internal Server Error`**.

#### 6.2.6. Deletar Conta a Receber

-   **Endpoint:** `DELETE /api/receivables/{id}`
-   **Funcionalidade:** Deleta uma conta a receber específica pelo seu ID, se pertencer ao usuário autenticado.
    *Atenção: Se a conta a receber foi processada automaticamente e atualizou o saldo de um banco, esta exclusão não reverte automaticamente a transação bancária.*
-   **Autenticação:** Requerida.

##### Parâmetros de Caminho

| Parâmetro | Tipo   | Obrigatório | Descrição                              |
|-----------|--------|-------------|----------------------------------------|
| `id`      | UUID   | Sim         | ID da conta a receber a ser deletada.  |

##### Respostas Esperadas

-   **`204 No Content`**: Conta a receber deletada com sucesso.

##### Possíveis Erros

-   **`401 Unauthorized`**.
-   **`403 Forbidden`**.
-   **`404 Not Found`**: Conta a receber não encontrada ou não pertence ao usuário.
-   **`500 Internal Server Error`**.

### 6.3. Modelos de Dados (DTOs) e Enums para Contas a Receber

#### 6.3.1. `ReceivableCreateDto`
Dados para criar uma nova conta a receber.

| Campo                  | Tipo                  | Descrição                                                                 |
|------------------------|-----------------------|---------------------------------------------------------------------------|
| `extraIncomeId`        | Long                  | ID do registro de `ExtraIncome` associado (obrigatório).                  |
| `receiptMethod`        | `ReceiptMethodEnum`   | Meio de recebimento (obrigatório).                                        |
| `dueDate`              | LocalDate             | Data de vencimento (obrigatório, não pode ser no passado).                |
| `automaticBankReceipt` | Boolean               | Indica recebimento automático no banco (obrigatório).                     |

#### 6.3.2. `ReceivableUpdateDto`
Dados para atualizar uma conta a receber existente. Apenas campos fornecidos são atualizados.

| Campo                  | Tipo                  | Descrição                                                                 |
|------------------------|-----------------------|---------------------------------------------------------------------------|
| `receiptMethod`        | `ReceiptMethodEnum`   | Novo meio de recebimento (opcional).                                      |
| `dueDate`              | LocalDate             | Nova data de vencimento (opcional, não pode ser no passado).              |
| `automaticBankReceipt` | Boolean               | Alterar flag de recebimento automático no banco (opcional).               |

#### 6.3.3. `ExtraIncomeSimpleDto` (Contexto de Contas a Receber)
Representa dados simplificados da Renda Extra associada a uma conta a receber.

| Campo         | Tipo       | Descrição                                                       |
|---------------|------------|-----------------------------------------------------------------|
| `id`          | Long       | ID da Renda Extra.                                              |
| `description` | String     | Descrição da Renda Extra.                                       |
| `value`       | BigDecimal | Valor da Renda Extra (corresponde ao `amount` da entidade `ExtraIncome`). |
| `bankId`      | UUID       | ID do banco associado à Renda Extra (opcional).                 |
| `bankName`    | String     | Nome do banco associado à Renda Extra (opcional).               |

#### 6.3.4. `ReceivableResponseDto`
Resposta detalhada para operações de contas a receber (estrutura detalhada em [6.1](#61-objeto-conta-a-receber-receivableresponsedto)).

#### 6.3.5. Enums

##### `ReceivableStatusEnum`
Status possíveis para uma conta a receber:
-   `PENDING`: Pendente de recebimento.
-   `RECEIVED`: Recebido no prazo.
-   `OVERDUE`: Vencido e não recebido.
-   `RECEIVED_LATE`: Recebido com atraso.

##### `ReceiptMethodEnum`
Meios de recebimento possíveis:
-   `CASH`: Dinheiro
-   `CREDIT_CARD`: Cartão de Crédito
-   `DEBIT_CARD`: Cartão de Débito
-   `PIX`: PIX
-   `BANK_SLIP`: Boleto Bancário
-   `CHECK`: Cheque
-   `LOAN`: Empréstimo (se aplicável como meio de receber de terceiros)
-   `TRANSFER`: Transferência Bancária
-   `CRYPTOCURRENCY`: Criptomoeda
-   `OTHER`: Outro

### 6.4. Considerações Importantes para Contas a Receber

-   **Vínculo com Renda Extra**: Toda conta a receber deve estar vinculada a um registro de `ExtraIncome` existente.
-   **Status da Conta**: O status é gerenciado pelo sistema, podendo ser alterado por ações do usuário (marcar como recebido) ou por processos automáticos (marcar como vencido).
-   **Recebimento Automático no Banco**: Se `automaticBankReceipt` for `true` e a `ExtraIncome` vinculada tiver um banco associado, o saldo desse banco será creditado quando a conta for marcada como recebida (manual ou automaticamente).
-   **Jobs Agendados (Implícito)**: A lógica de serviço (`ReceivableService`) inclui métodos para processar contas vencidas (`processOverdueReceivablesJob`) e para processar recebimentos automáticos no banco (`processAutomaticBankReceiptsJob`). Estes jobs não são expostos como endpoints da API, mas são parte crucial do funcionamento do sistema.
-   **Paginação**: A listagem de contas a receber (`GET /api/receivables`) suporta paginação para lidar com grandes volumes de dados.
-   **Atualização de Contas Recebidas**: Não é permitido atualizar contas que já foram marcadas como `RECEIVED` ou `RECEIVED_LATE`.

---

## 7. Gerenciamento de Despesas

Esta seção detalha os endpoints para o gerenciamento das despesas do usuário.

Controlador responsável: `ExpenseController`
Rota base: `/api/expenses`
**Autenticação:** Requerida para todos os endpoints nesta seção.

### 7.1. Objeto Despesa (`ExpenseDetailResponseDto`)

Representa os dados detalhados de uma despesa, como retornado pela API. Este DTO aninha informações do usuário e os dados completos da despesa.

#### 7.1.1. Estrutura `ExpenseDetailResponseDto`

| Campo     | Tipo                  | Descrição                                                                 |
|-----------|-----------------------|---------------------------------------------------------------------------|
| `user`    | `UserSimpleDto`       | Dados simplificados do usuário proprietário (ver [1.3.1](#131-userdto) ou [2.2.5](#225-usersimpledto) para estrutura). |
| `expense` | `ExpenseDataDto`      | Dados detalhados da despesa (ver estrutura abaixo).                       |

#### 7.1.2. Estrutura `ExpenseDataDto` (Aninhado em `ExpenseDetailResponseDto`)

| Campo         | Tipo                | Descrição                                                                 | Exemplo                                   |
|---------------|---------------------|---------------------------------------------------------------------------|-------------------------------------------|
| `id`          | UUID                | UUID da despesa.                                                          | `a1b2c3d4-e5f6-7890-1234-567890abcdef`    |
| `name`        | String              | Nome da despesa.                                                          | "Conta de Luz"                            |
| `description` | String              | Descrição detalhada da despesa.                                           | "Pagamento mensal referente a maio"       |
| `value`       | BigDecimal          | Valor da despesa.                                                         | `150.75`                                  |
| `expenseDate` | LocalDate           | Data em que a despesa ocorreu (formato `YYYY-MM-DD`).                     | `"2025-05-27"`                            |
| `category`    | `CategorySimpleDto` | Categoria da despesa (contém `id` e `name` da categoria).                 | `{ "id": "uuid-cat", "name": "Moradia" }` |
| `bank`        | `BankSimpleDto`     | Banco associado à despesa (opcional, contém `id` e `name` do banco).      | `{ "id": "uuid-bank", "name": "Banco X" }` ou `null` |
| `createdAt`   | LocalDateTime       | Timestamp de criação da despesa.                                          | `"2025-05-28T10:15:30Z"`                  |
| `updatedAt`   | LocalDateTime       | Timestamp da última atualização da despesa.                               | `"2025-05-28T10:20:00Z"`                  |

*(Nota: `CategorySimpleDto` e `BankSimpleDto` são DTOs simplificados contendo `id` e `name` das respectivas entidades).*

### 7.2. Endpoints de Despesas

#### 7.2.1. Criar Nova Despesa

-   **Endpoint:** `POST /api/expenses`
-   **Funcionalidade:** Cria uma nova despesa para o usuário autenticado.
-   **Autenticação:** Requerida.

##### Corpo da Requisição (`ExpenseCreateDto`)

| Campo         | Tipo       | Obrigatório | Descrição                                                     | Validações                                      | Exemplo                                   |
|---------------|------------|-------------|---------------------------------------------------------------|-------------------------------------------------|-------------------------------------------|
| `name`        | String     | Sim         | Nome da despesa.                                              | 1-100 caracteres.                               | "Conta de luz"                            |
| `description` | String     | Não         | Descrição opcional. Padrão: "Campo não Informado pelo Usuário". | -                                               | "Pagamento mensal referente a maio"       |
| `value`       | BigDecimal | Sim         | Valor da despesa.                                             | Deve ser maior que 0 (`@DecimalMin("0.01")`).   | `150.75`                                  |
| `categoryId`  | UUID       | Sim         | ID da categoria associada.                                    | Obrigatório.                                    | `b9244a85-9d51-46e7-b626-259259862ad1`    |
| `bankId`      | UUID       | Não         | ID do banco para associar à despesa (opcional).               | -                                               | `daa0e2a7-2ad6-42b9-8271-1d7e9facc027`    |
| `expenseDate` | LocalDate  | Sim         | Data em que a despesa ocorreu (formato `YYYY-MM-DD`).         | Obrigatório.                                    | `"2025-05-27"`                            |

##### Respostas Esperadas

-   **`201 Created`**: Despesa criada com sucesso.
    -   **Corpo da Resposta (`ExpenseDetailResponseDto`):** Detalhes da despesa criada (conforme estrutura em [7.1](#71-objeto-despesa-expensedetailresponsedto)).
    -   **Headers:** `Location` contendo a URI do novo recurso criado (ex: `/api/expenses/a1b2c3d4-e5f6-7890-1234-567890abcdef`).

##### Possíveis Erros

-   **`400 Bad Request`**: Dados inválidos fornecidos.
    -   **Motivos:** Campos obrigatórios (`name`, `value`, `categoryId`, `expenseDate`) faltando ou inválidos (ex: `value` não positivo, `name` fora do tamanho).
-   **`401 Unauthorized`**: Token JWT ausente, inválido ou expirado.
-   **`404 Not Found`**:
    -   Usuário autenticado não encontrado.
    -   Categoria com o `categoryId` fornecido não encontrada ou não pertence ao usuário.
    -   Banco com o `bankId` (se fornecido) não encontrado ou não pertence ao usuário.
-   **`500 Internal Server Error`**.

#### 7.2.2. Listar Todas as Despesas do Usuário

-   **Endpoint:** `GET /api/expenses`
-   **Funcionalidade:** Retorna uma lista de todas as despesas pertencentes ao usuário autenticado.
-   **Autenticação:** Requerida.

##### Respostas Esperadas

-   **`200 OK`**: Lista de despesas retornada com sucesso.
    -   **Corpo da Resposta (Array de `ExpenseDetailResponseDto`):**
        ```json
        [
            {
                "user": { "id": "uuid-user", "name": "Usuário Exemplo" },
                "expense": {
                    "id": "a1b2c3d4-e5f6-7890-1234-567890abcdef",
                    "name": "Aluguel",
                    "description": "Pagamento mensal",
                    "value": 1200.00,
                    "expenseDate": "2025-05-05",
                    "category": { "id": "uuid-cat-moradia", "name": "Moradia" },
                    "bank": { "id": "uuid-bank-main", "name": "Banco Principal" },
                    "createdAt": "2025-05-05T09:00:00Z",
                    "updatedAt": "2025-05-05T09:00:00Z"
                }
            }
            // ... outras despesas
        ]
        ```
        Se o usuário não possuir despesas, retorna uma lista vazia `[]`.

##### Possíveis Erros

-   **`401 Unauthorized`**.
-   **`404 Not Found`**: Usuário autenticado não encontrado.
-   **`500 Internal Server Error`**.

#### 7.2.3. Buscar Despesa Específica por ID

-   **Endpoint:** `GET /api/expenses/{id}`
-   **Funcionalidade:** Recupera os detalhes de uma despesa específica pelo seu ID, se pertencer ao usuário autenticado.
-   **Autenticação:** Requerida.

##### Parâmetros de Caminho

| Parâmetro | Tipo   | Obrigatório | Descrição                       |
|-----------|--------|-------------|---------------------------------|
| `id`      | UUID   | Sim         | ID da despesa a ser buscada.    |

##### Respostas Esperadas

-   **`200 OK`**: Despesa encontrada.
    -   **Corpo da Resposta (`ExpenseDetailResponseDto`):** Detalhes da despesa (conforme estrutura em [7.1](#71-objeto-despesa-expensedetailresponsedto)).

##### Possíveis Erros

-   **`401 Unauthorized`**.
-   **`404 Not Found`**: Despesa não encontrada ou não pertence ao usuário.
    -   Mensagem: `Despesa com ID {id} não encontrada ou não pertence ao usuário.`
-   **`500 Internal Server Error`**.

#### 7.2.4. Atualizar Despesa Existente

-   **Endpoint:** `PUT /api/expenses/{id}`
-   **Funcionalidade:** Atualiza uma despesa existente do usuário autenticado. Somente campos não nulos no corpo da requisição serão atualizados.
-   **Autenticação:** Requerida.

##### Parâmetros de Caminho

| Parâmetro | Tipo   | Obrigatório | Descrição                          |
|-----------|--------|-------------|------------------------------------|
| `id`      | UUID   | Sim         | ID da despesa a ser atualizada.    |

##### Corpo da Requisição (`ExpenseUpdateDto`)

*Apenas os campos a serem alterados precisam ser fornecidos.*

| Campo         | Tipo       | Obrigatório | Descrição                                                     | Validações                                      |
|---------------|------------|-------------|---------------------------------------------------------------|-------------------------------------------------|
| `name`        | String     | Não         | Novo nome da despesa.                                         | Se fornecido, não pode ser vazio; 1-100 caracteres. |
| `description` | String     | Não         | Nova descrição opcional.                                      | -                                               |
| `value`       | BigDecimal | Não         | Novo valor da despesa.                                        | Se fornecido, deve ser maior que 0.             |
| `categoryId`  | UUID       | Não         | Novo ID da categoria associada.                               | -                                               |
| `bankId`      | UUID       | Não         | Novo ID do banco associado (enviar `null` para desassociar).  | -                                               |
| `expenseDate` | LocalDate  | Não         | Nova data da despesa (formato `YYYY-MM-DD`).                  | -                                               |

##### Respostas Esperadas

-   **`200 OK`**: Despesa atualizada com sucesso.
    -   **Corpo da Resposta (`ExpenseDetailResponseDto`):** Detalhes atualizados da despesa.

##### Possíveis Erros

-   **`400 Bad Request`**: Dados inválidos fornecidos.
    -   **Motivos:** `name` vazio, `value` não positivo.
-   **`401 Unauthorized`**.
-   **`404 Not Found`**: Despesa, usuário, categoria ou banco (se `bankId` fornecido) não encontrado ou não pertence ao usuário.
    -   Mensagens: `Não é possível editar a despesa (ID: {id}), porque você não possui ela cadastrada.`, `Categoria com ID {categoryId} não encontrada ou não pertence ao usuário.`, `Banco com ID {bankId} não encontrado ou não pertence ao usuário.`
-   **`500 Internal Server Error`**.

#### 7.2.5. Deletar Despesa

-   **Endpoint:** `DELETE /api/expenses/{id}`
-   **Funcionalidade:** Deleta uma despesa específica pelo seu ID, se pertencer ao usuário autenticado.
-   **Autenticação:** Requerida.

##### Parâmetros de Caminho

| Parâmetro | Tipo   | Obrigatório | Descrição                          |
|-----------|--------|-------------|------------------------------------|
| `id`      | UUID   | Sim         | ID da despesa a ser deletada.      |

##### Respostas Esperadas

-   **`204 No Content`**: Despesa deletada com sucesso.

##### Possíveis Erros

-   **`401 Unauthorized`**.
-   **`404 Not Found`**: Despesa não encontrada ou não pertence ao usuário.
    -   Mensagem: `Não é possível deletar a despesa (ID: {id}), porque você não possui ela cadastrada.`
-   **`500 Internal Server Error`**.

#### 7.2.6. Atualizar TODAS as Despesas do Usuário (Em Lote)

-   **Endpoint:** `PUT /api/expenses/user-all`
-   **Funcionalidade:** Atualiza campos específicos (descrição, data, categoria, banco) em TODAS as despesas do usuário autenticado.
-   **Autenticação:** Requerida.

##### Corpo da Requisição (`ExpenseMassUpdateDto`)

*Apenas os campos a serem aplicados a todas as despesas devem ser fornecidos.*

| Campo         | Tipo      | Obrigatório | Descrição                                                                 | Validações                                      |
|---------------|-----------|-------------|---------------------------------------------------------------------------|-------------------------------------------------|
| `description` | String    | Não         | Nova descrição. Envie `""` (string vazia) para limpar.                    | Máx 255 caracteres.                             |
| `expenseDate` | LocalDate | Não         | Nova data da despesa (formato `YYYY-MM-DD`).                              | -                                               |
| `categoryId`  | UUID      | Não         | Novo ID de categoria para TODAS as despesas.                              | Categoria deve existir e pertencer ao usuário.  |
| `bankId`      | UUID      | Não         | Novo ID de banco para TODAS as despesas (enviar `null` para desassociar). | Banco deve existir e pertencer ao usuário.      |

##### Respostas Esperadas

-   **`200 OK`**: Despesas atualizadas com sucesso.
    -   **Corpo da Resposta (Array de `ExpenseDetailResponseDto`):** Lista de todas as despesas do usuário com os dados atualizados.

##### Possíveis Erros

-   **`400 Bad Request`**: Dados inválidos (ex: `description` excede tamanho).
-   **`401 Unauthorized`**.
-   **`404 Not Found`**: Usuário, categoria ou banco (se IDs fornecidos) não encontrado.
-   **`500 Internal Server Error`**.

#### 7.2.7. Deletar TODAS as Despesas do Usuário (Em Lote)

-   **Endpoint:** `DELETE /api/expenses/user-all`
-   **Funcionalidade:** Deleta todas as despesas pertencentes ao usuário autenticado.
-   **Autenticação:** Requerida.

##### Respostas Esperadas

-   **`204 No Content`**: Todas as despesas do usuário foram deletadas com sucesso.

##### Possíveis Erros

-   **`401 Unauthorized`**.
-   **`404 Not Found`**: Usuário autenticado não encontrado.
-   **`500 Internal Server Error`**.

### 7.3. Modelos de Dados (DTOs) para Despesas

#### 7.3.1. `ExpenseCreateDto`
Dados para criar uma nova despesa.

| Campo         | Tipo       | Descrição                                                     |
|---------------|------------|---------------------------------------------------------------|
| `name`        | String     | Nome da despesa (obrigatório, 1-100 caracteres).              |
| `description` | String     | Descrição opcional.                                           |
| `value`       | BigDecimal | Valor da despesa (obrigatório, > 0).                          |
| `categoryId`  | UUID       | ID da categoria associada (obrigatório).                      |
| `bankId`      | UUID       | ID do banco associado (opcional).                             |
| `expenseDate` | LocalDate  | Data da despesa (obrigatório, formato `YYYY-MM-DD`).          |

#### 7.3.2. `ExpenseUpdateDto`
Dados para atualizar uma despesa existente. Apenas campos fornecidos são atualizados.

| Campo         | Tipo       | Descrição                                                     |
|---------------|------------|---------------------------------------------------------------|
| `name`        | String     | Novo nome (opcional, 1-100 caracteres se fornecido).          |
| `description` | String     | Nova descrição (opcional).                                    |
| `value`       | BigDecimal | Novo valor (opcional, > 0 se fornecido).                      |
| `categoryId`  | UUID       | Novo ID da categoria (opcional).                              |
| `bankId`      | UUID       | Novo ID do banco (opcional, `null` para desassociar).         |
| `expenseDate` | LocalDate  | Nova data da despesa (opcional, formato `YYYY-MM-DD`).        |

#### 7.3.3. `ExpenseMassUpdateDto`
Dados para atualizar campos específicos em todas as despesas de um usuário.

| Campo         | Tipo      | Descrição                                                                 |
|---------------|-----------|---------------------------------------------------------------------------|
| `description` | String    | Nova descrição para todas as despesas (opcional, máx 255 caracteres).     |
| `expenseDate` | LocalDate | Nova data para todas as despesas (opcional, formato `YYYY-MM-DD`).        |
| `categoryId`  | UUID      | Novo ID de categoria para todas as despesas (opcional).                   |
| `bankId`      | UUID      | Novo ID de banco para todas as despesas (opcional, `null` para desassociar).|

#### 7.3.4. `ExpenseDataDto`
Estrutura detalhada dos dados de uma despesa (usada dentro de `ExpenseDetailResponseDto`). Detalhada em [7.1.2](#712-estrutura-expensedatadto-aninhado-em-expensedetailresponsedto).

#### 7.3.5. `ExpenseDetailResponseDto`
Resposta padrão para a maioria das operações de despesa (estrutura detalhada em [7.1.1](#711-estrutura-expensedetailresponsedto)).

#### 7.3.6. `ExpenseDto` (Referência)
DTO mais simples que pode ser usado internamente ou para outros cenários. Contém IDs e nomes para categoria e banco, em vez de objetos aninhados.

| Campo             | Tipo          | Descrição                                                     |
|-------------------|---------------|---------------------------------------------------------------|
| `id`              | UUID          | UUID da despesa.                                              |
| `name`            | String        | Nome da despesa.                                              |
| `description`     | String        | Descrição.                                                    |
| `value`           | BigDecimal    | Valor.                                                        |
| `expenseDate`     | LocalDate     | Data da despesa.                                              |
| `categoryId`      | UUID          | UUID da categoria.                                            |
| `categoryName`    | String        | Nome da categoria.                                            |
| `bankId`          | UUID          | UUID do banco (pode ser nulo).                                |
| `bankDisplayName` | String        | Nome/Status do banco.                                         |
| `createdAt`       | LocalDateTime | Timestamp de criação.                                         |
| `updatedAt`       | LocalDateTime | Timestamp da última atualização.                                |

### 7.4. Considerações Importantes para Despesas

-   **Propriedade de Dados**: Todas as despesas são vinculadas ao usuário autenticado.
-   **Associação com Categoria**: Toda despesa deve ser associada a uma categoria existente e pertencente ao usuário.
-   **Associação com Banco**: Uma despesa pode, opcionalmente, ser associada a um banco existente e pertencente ao usuário.
-   **Validação de Valores**: O valor da despesa (`value`) deve ser sempre positivo.
-   **Operações em Lote**: A API permite atualizar campos específicos ou deletar todas as despesas de um usuário de uma vez.


