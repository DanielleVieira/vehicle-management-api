# Vehicle Management API

API REST para gerenciamento de clientes, veículos e usuários com autenticação JWT.

## Stack

- Java 25
- Spring Boot 4.0.3
- Spring Web MVC
- Spring Data JPA / Hibernate
- Spring Security
- JWT (`jjwt` 0.13.0)
- Bean Validation
- MySQL
- H2 para testes
- MapStruct
- Lombok
- Springdoc OpenAPI
- Gradle Wrapper

## O que a API faz

- CRUD de clientes
- CRUD de veículos
- listagem de veículos por proprietário com `GET /api/v1/vehicles?ownerId={ownerId}`
- cadastro e login de usuários
- consulta de usuário por id
- controle de acesso com JWT e perfis `USER` e `ADMIN`

## Requisitos

- Java 25
- MySQL 8+

## Configuração

As configurações principais ficam em [application.yaml](/home/danielle/IdeaProjects/vehicle-management-api/src/main/resources/application.yaml) e [application-test.yaml](/home/danielle/IdeaProjects/vehicle-management-api/src/main/resources/application-test.yaml).

Defina estas variáveis de ambiente antes de subir a aplicação:

- `URL_DB`
- `USER_DB`
- `PASSWORD_DB`
- `SECRET_JWT`
- `ADMIN_USERNAME`
- `ADMIN_PASSWORD`

Exemplo:

```bash
export URL_DB=jdbc:mysql://localhost:3306/vehicle_management
export USER_DB=root
export PASSWORD_DB=secret
export SECRET_JWT=ZmFrZVNlY3JldEZvclRlc3RzRmFrZVNlY3JldEZvclRlc3RzMTIzNDU2
export ADMIN_USERNAME=admin@local
export ADMIN_PASSWORD=admin12345
```

Observações:

- `SECRET_JWT` precisa estar em Base64, porque a chave é carregada com `Decoders.BASE64.decode(...)`.
- Ao iniciar a aplicação, um usuário administrador é criado ou promovido para `ADMIN` com base em `ADMIN_USERNAME` e `ADMIN_PASSWORD`.
- O token JWT expira em 30 minutos.

## Como executar com Gradle

Subir a API:

```bash
./gradlew bootRun
```

Gerar o artefato:

```bash
./gradlew build
```

Executar a aplicação empacotada:

```bash
java -jar build/libs/vehicle-management-api-0.0.1-SNAPSHOT.jar
```

## Documentação da API

Com a aplicação em execução:

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## Autenticação e autorização

Rotas públicas:

- `POST /api/v1/auth/signup`
- `POST /api/v1/auth/login`
- Swagger/OpenAPI

Todas as outras rotas exigem token JWT no cabeçalho:

```http
Authorization: Bearer <token>
```

Regras de acesso:

- `DELETE /api/v1/clients/**`: somente `ADMIN`
- `DELETE /api/v1/vehicles/**`: somente `ADMIN`
- `/api/v1/users/**`: somente `ADMIN`
- demais rotas autenticadas: `USER` ou `ADMIN`

O CORS está liberado para:

- `http://localhost:3000`
- `http://localhost:5173`

## Endpoints

Base URL: `/api/v1`

### Auth

- `POST /auth/signup`
- `POST /auth/login`

Exemplo de cadastro/login:

```json
{
  "email": "user@example.com",
  "password": "secret123"
}
```

Exemplo de resposta:

```json
{
  "token": "<jwt>",
  "type": "Bearer",
  "userResponse": {
    "id": 1,
    "email": "user@example.com",
    "role": "USER"
  }
}
```

### Usuários

- `GET /users/{userId}` (`ADMIN`)

### Clientes

- `POST /clients`
- `GET /clients`
- `GET /clients/{clientId}`
- `PUT /clients/{clientId}`
- `DELETE /clients/{clientId}` (`ADMIN`)

Payload:

```json
{
  "name": "Ana Silva",
  "email": "ana@exemplo.com",
  "cpf": "123.456.789-09",
  "birthDate": "1990-05-20"
}
```

Resposta:

```json
{
  "id": 1,
  "name": "ANA SILVA",
  "email": "ana@exemplo.com",
  "cpf": "12345678909",
  "birthDate": "1990-05-20"
}
```

### Veículos

- `POST /vehicles`
- `GET /vehicles`
- `GET /vehicles/{vehicleId}`
- `GET /vehicles?ownerId={ownerId}`
- `PUT /vehicles/{vehicleId}`
- `DELETE /vehicles/{vehicleId}` (`ADMIN`)

Payload de criação:

```json
{
  "make": "Toyota",
  "model": "Corolla",
  "year": 2020,
  "licensePlate": "ABC1D23",
  "ownerId": 1
}
```

Payload de atualização:

```json
{
  "make": "Toyota",
  "model": "Corolla",
  "year": 2021,
  "licensePlate": "ABC1D23"
}
```

Resposta:

```json
{
  "id": 10,
  "make": "TOYOTA",
  "model": "COROLLA",
  "year": 2021,
  "licensePlate": "ABC1D23",
  "owner": {
    "id": 1,
    "name": "ANA SILVA",
    "email": "ana@exemplo.com",
    "cpf": "12345678909",
    "birthDate": "1990-05-20"
  }
}
```

## Regras de validação

### Auth e usuários

- `email`: obrigatório, formato válido, até 255 caracteres
- `password`: obrigatória, entre 8 e 72 caracteres

### Clientes

- `name`: obrigatório, entre 3 e 100 caracteres
- `email`: obrigatório, formato válido, até 255 caracteres
- `cpf`: obrigatório e válido
- `birthDate`: obrigatória e deve estar no passado
- `clientId`: deve ser positivo

### Veículos

- `make`: obrigatória, entre 3 e 50 caracteres
- `model`: obrigatório, entre 3 e 100 caracteres
- `year`: obrigatório e mínimo `1886`
- `licensePlate`: obrigatória e deve seguir o padrão brasileiro antigo ou Mercosul
- `ownerId`: obrigatório no `POST /vehicles` e deve ser positivo
- `vehicleId`: deve ser positivo

## Regras de negócio

- `name`, `make` e `model` são persistidos em maiúsculas
- `email` é persistido em minúsculas
- `cpf` é persistido sem pontos, espaços ou hífen
- `licensePlate` é persistida em maiúsculas e sem hífen
- não é possível cadastrar ou atualizar cliente com CPF já registrado
- não é possível cadastrar ou atualizar usuário com email já registrado
- não é possível cadastrar ou atualizar veículo com placa já registrada
- o ano do veículo não pode ser maior que o ano atual
- um veículo só pode ser criado para um cliente existente
- `GET /vehicles?ownerId={ownerId}` exige que o proprietário exista
- o `PUT /vehicles/{vehicleId}` não permite alterar o proprietário

## Respostas de erro

A API retorna um payload padrão:

```json
{
  "timestamp": "2026-04-10T12:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed. Please check your request parameters",
  "path": "/api/v1/vehicles",
  "details": [
    "licensePlate: must follow the old Brazilian standard or Mercosur standard."
  ]
}
```

Códigos mais comuns:

- `400 Bad Request`: validação, JSON inválido ou parâmetro inválido
- `401 Unauthorized`: token ausente, inválido ou credenciais incorretas
- `403 Forbidden`: usuário autenticado sem permissão
- `404 Not Found`: recurso não encontrado
- `409 Conflict`: CPF, email ou placa duplicados; ou violação de integridade no banco
- `422 Unprocessable Content`: regra de negócio violada, como ano maior que o atual
- `500 Internal Server Error`: falha não tratada
