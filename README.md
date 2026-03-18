# Client Manager API

API REST para gerenciamento de clientes com autenticação JWT, integração com serviços de CEP e controle de acesso por perfis.

## Tecnologias

- **Java 8**
- **Spring Boot 2.7.18** (Web, Data JPA, Security, Validation)
- **H2 Database** (banco em memória)
- **JWT** (JJWT 0.9.1)
- **Swagger/OpenAPI 3.0** (Springfox 3.0.0)
- **Lombok**
- **JUnit 5 + Mockito** (testes unitários e de integração)
- **Maven**

## Requisitos

- **JDK 8** (1.8)
- **Maven 3.6+**

## Como Executar

```bash
git clone <url-do-repositório>
cd clientmanager
mvn spring-boot:run
```

A aplicação estará disponível em `http://localhost:8080`.

Dois usuários são criados automaticamente na inicialização:

| Usuário | Senha       | Perfil | Permissões        |
|---------|-------------|--------|--------------------|
| admin   | 123qwel@#   | ADMIN  | Leitura + Escrita  |
| user    | 123qwe123   | USER   | Somente leitura    |

## Como Rodar os Testes

```bash
mvn test
```

O projeto possui **84 testes** automatizados:

- **Testes unitários** (Mockito): AuthService, ClientService, MaskUtils, JwtTokenProvider
- **Testes de integração** (MockMvc): AuthController, ClientController, ZipCodeController
- Cenários cobertos: sucesso (200/201/204), validação (400), autenticação (401), autorização (403) e recurso não encontrado (404)

## Documentação Interativa (Swagger)

Com a aplicação rodando:

```
http://localhost:8080/swagger-ui/index.html
```

Para testar endpoints protegidos:
1. Faça login via `POST /auth/login`
2. Copie o token retornado
3. Clique em **Authorize** e cole: `Bearer <seu-token>`

## Endpoints

### Autenticação

| Método | Endpoint    | Acesso  | Descrição          |
|--------|-------------|---------|---------------------|
| POST   | /auth/login | Público | Retorna token JWT   |

**Request:**
```json
{
  "username": "admin",
  "password": "123qwel@#"
}
```

**Response (200):**
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "type": "Bearer",
  "role": "ADMIN"
}
```

### Clientes

| Método | Endpoint      | Acesso      | Descrição        |
|--------|---------------|-------------|-------------------|
| POST   | /clients      | ADMIN       | Criar cliente     |
| GET    | /clients      | Autenticado | Listar clientes   |
| GET    | /clients/{id} | Autenticado | Buscar por ID     |
| PUT    | /clients/{id} | ADMIN       | Atualizar cliente |
| DELETE | /clients/{id} | ADMIN       | Remover cliente   |

**Criar cliente — Request:**
```json
{
  "name": "Joao Silva",
  "cpf": "123.456.789-09",
  "address": {
    "zipCode": "70040-010",
    "street": "SBS Quadra 2",
    "neighborhood": "Asa Sul",
    "city": "Brasilia",
    "state": "DF",
    "complement": "Bloco A"
  },
  "phones": [
    {
      "type": "MOBILE",
      "number": "(61) 99999-8888"
    }
  ],
  "emails": [
    {
      "address": "joao@email.com"
    }
  ]
}
```

**Response (201):**
```json
{
  "id": 1,
  "name": "Joao Silva",
  "cpf": "123.456.789-09",
  "address": {
    "zipCode": "70040-010",
    "street": "SBS Quadra 2",
    "neighborhood": "Asa Sul",
    "city": "Brasilia",
    "state": "DF",
    "complement": "Bloco A"
  },
  "phones": [
    {
      "id": 1,
      "type": "MOBILE",
      "number": "(61) 99999-8888"
    }
  ],
  "emails": [
    {
      "id": 1,
      "address": "joao@email.com"
    }
  ]
}
```

### Consulta de CEP

| Método | Endpoint       | Acesso      | Descrição                 |
|--------|----------------|-------------|---------------------------|
| GET    | /cep/{zipCode} | Autenticado | Consultar endereço por CEP |

Aceita CEP com ou sem máscara. Se o provedor primário (ViaCEP) estiver indisponível, o fallback (OpenCEP) é acionado automaticamente.

**Response (200):**
```json
{
  "zipCode": "70040-010",
  "street": "SBS Quadra 2",
  "complement": "",
  "neighborhood": "Asa Sul",
  "city": "Brasília",
  "state": "DF"
}
```

## Regras de Negócio

**Nome:** obrigatório, 3 a 100 caracteres, aceita apenas letras, números e espaços.

**CPF:** obrigatório e único por cliente. Validado com dígitos verificadores (anotação customizada `@CPF`). Persistido sem máscara, exibido com máscara (xxx.xxx.xxx-xx). Aceita entrada com ou sem máscara.

**Endereço:** obrigatório. CEP, logradouro, bairro, cidade e UF são campos obrigatórios. Complemento é opcional. CEP persistido sem máscara, exibido com máscara (xxxxx-xxx).

**Telefones:** ao menos um obrigatório. Tipos aceitos: `RESIDENTIAL`, `COMMERCIAL`, `MOBILE` (validado no DTO com `@ValidPhoneType`). Persistido sem máscara, exibido com máscara — (xx) xxxx-xxxx para fixo, (xx) xxxxx-xxxx para celular.

**E-mails:** ao menos um obrigatório. Deve ser um formato de e-mail válido.

## Decisões Técnicas

### Arquitetura em Camadas

```
Controller → Service → Repository → Entity
    ↕            ↕
   DTO      Exception
```

Os controllers recebem e respondem HTTP. A lógica de negócio (validação de CPF duplicado, masking, conversão DTO/Entity) fica nos services. Os repositories expõem queries derivadas do Spring Data JPA.

### Autenticação JWT

Fluxo stateless:

1. Cliente envia credenciais para `POST /auth/login`
2. `AuthService` valida via `AuthenticationManager` + `BCryptPasswordEncoder`
3. `JwtTokenProvider` gera token HS512 com username e role (expiração: 24h)
4. Nas requisições seguintes, `JwtAuthenticationFilter` intercepta o header `Authorization: Bearer <token>`, valida e injeta a autenticação no `SecurityContext`

Endpoints de escrita (`POST`, `PUT`, `DELETE`) exigem role `ADMIN`. Endpoints de leitura (`GET`) exigem apenas autenticação.

### Integração de CEP com Fallback

Utiliza o **Strategy Pattern** com injeção ordenada (`@Order`):

1. **ViaCEP** (`@Order(1)`) — provedor primário
2. **OpenCEP** (`@Order(2)`) — fallback automático se o ViaCEP estiver indisponível

Adicionar um novo provedor requer apenas criar uma classe com `@Order(3)` implementando `ZipCodeProvider`, sem alterar código existente (Open/Closed Principle).

### Validação Customizada

O projeto implementa duas anotações de Bean Validation customizadas:

- `@CPF` — valida formato (11 dígitos) e dígitos verificadores do CPF
- `@ValidPhoneType` — valida se o tipo de telefone é um dos valores aceitos (`RESIDENTIAL`, `COMMERCIAL`, `MOBILE`)

Ambas seguem o padrão `Annotation` + `ConstraintValidator` do Jakarta Validation, mantendo a validação na camada de DTO e retornando mensagens claras no `ErrorResponse`.

### Masking de Dados

Dados sensíveis (CPF, CEP, telefone) são **persistidos sem máscara** e **exibidos com máscara**. A classe `MaskUtils` centraliza aplicação e remoção de máscaras.

### Tratamento de Erros

O `GlobalExceptionHandler` padroniza todas as respostas de erro:

```json
{
  "timestamp": "2026-03-18T12:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "errors": ["name: Name is required", "cpf: Invalid CPF"]
}
```

Exceções tratadas: validação (400), regra de negócio (400), recurso não encontrado (404), credenciais inválidas (401), argumento inválido (400) e erros inesperados (500).

## Estrutura do Projeto

```
src/main/java/com/felipelima/clientmanager/
├── ClientManagerApplication.java
├── config/
│   ├── DataInitializer.java          # Cria usuários padrão ao iniciar
│   ├── RestTemplateConfig.java       # Bean do RestTemplate para chamadas HTTP
│   ├── SecurityConfig.java           # Regras de autenticação e autorização
│   └── SwaggerConfig.java            # Configuração do Swagger com suporte JWT
├── controller/
│   ├── AuthController.java           # POST /auth/login
│   ├── ClientController.java         # CRUD /clients
│   └── ZipCodeController.java        # GET /cep/{zipCode}
├── dto/
│   ├── request/                      # DTOs de entrada com validação
│   └── response/                     # DTOs de saída com dados mascarados
├── entity/
│   ├── Address.java
│   ├── Client.java
│   ├── Email.java
│   ├── Phone.java
│   ├── User.java
│   └── enums/
├── exception/
│   ├── BusinessException.java        # Regras de negócio (400)
│   ├── ErrorResponse.java            # Estrutura padronizada de erro
│   ├── GlobalExceptionHandler.java   # Handler centralizado
│   └── ResourceNotFoundException.java # Recurso não encontrado (404)
├── integration/zipcode/
│   ├── ZipCodeProvider.java          # Interface Strategy
│   ├── ZipCodeService.java           # Orquestrador com fallback
│   ├── ViaCepProvider.java           # Provedor primário
│   └── OpenCepProvider.java          # Provedor fallback
├── repository/
│   ├── ClientRepository.java
│   └── UserRepository.java
├── security/
│   ├── CustomUserDetailsService.java
│   ├── JwtAuthenticationFilter.java
│   └── JwtTokenProvider.java
├── service/
│   ├── AuthService.java
│   ├── ClientService.java
│   └── MaskUtils.java
└── validation/
    ├── CPF.java                      # Anotação customizada @CPF
    ├── CpfValidator.java             # Validador de CPF
    ├── ValidPhoneType.java           # Anotação customizada @ValidPhoneType
    └── PhoneTypeValidator.java       # Validador de tipo de telefone
```

## Melhorias Futuras

- **Paginação:** implementar `Pageable` no `findAll` para suportar grandes volumes de dados
- **Banco de dados relacional:** substituir H2 por PostgreSQL ou MySQL para uso em produção
- **Cache:** adicionar cache na consulta de CEP para reduzir chamadas externas
- **Docker:** criar `Dockerfile` e `docker-compose.yml` para facilitar deploy
- **CI/CD:** configurar pipeline com GitHub Actions para build e testes automáticos
- **Auditoria:** adicionar campos `createdAt` e `updatedAt` nas entidades
- **Rate limiting:** limitar requisições por IP para proteção contra abuso
