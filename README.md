# Vehicle Management API

REST API para gerenciamento de clientes e veículos, construída com Java 25, Spring Boot, Hibernate/JPA e MySQL.

## Visão geral

Esta API permite:

- CRUD de clientes.
- CRUD de veículos, com vínculo obrigatório a um cliente (owner).
- Consulta de veículos por proprietário.

As entidades normalizam dados automaticamente:

- `name`, `make`, `model` e `licensePlate` são gravados em maiúsculas.
- `email` é gravado em minúsculas.

## Stack

- Java 25
- Spring Boot 4.0.3
- Spring Data JPA + Hibernate
- MySQL
- MapStruct 1.5.5.Final
- Lombok
- Springdoc OpenAPI (Swagger UI)

## Requisitos

- Java 25 (toolchain configurada no Gradle)
- MySQL 8+

## Configuração

A aplicação usa variáveis de ambiente para conexão com o banco:

- `URL_DB`
- `USER_DB`
- `PASSWORD_DB`

Exemplo:

```bash
export URL_DB=jdbc:mysql://localhost:3306/vehicle_management
export USER_DB=root
export PASSWORD_DB=secret
```

## Como rodar

```bash
./gradlew bootRun
```

## Swagger / OpenAPI

- UI: `http://localhost:8080/swagger-ui/index.html`
- JSON: `http://localhost:8080/v3/api-docs`

## Validações e regras

### Clientes

- `name`: obrigatório, 3 a 100 caracteres.
- `email`: obrigatório, formato válido, até 255 caracteres.
- `cpf`: obrigatório, válido (`@CPF`).
- `birthDate`: obrigatório e no passado.

### Veículos

- `make`: obrigatório, 3 a 50 caracteres.
- `model`: obrigatório, 3 a 100 caracteres.
- `year`: obrigatório, mínimo 1886 e não pode ser no futuro.
- `licensePlate`: obrigatório, 7 a 8 caracteres, padrão brasileiro antigo ou Mercosul.
- `ownerId`: obrigatório e positivo (somente no `POST`).

Observação: o `PUT /vehicles/{vehicleId}` não permite alterar o proprietário.

## Endpoints

Base URL: `/api/v1`

### Clientes

- `POST /clients`
- `GET /clients/{id}`
- `GET /clients`
- `PUT /clients/{id}`
- `DELETE /clients/{id}`

Payload `POST /clients` e `PUT /clients/{id}`:

```json
{
  "name": "Ana Silva",
  "email": "ana@exemplo.com",
  "cpf": "12345678901",
  "birthDate": "1990-05-20"
}
```

### Veículos

- `POST /vehicles`
- `GET /vehicles/{id}`
- `GET /vehicles`
- `GET /vehicles?ownerId={ownerId}`
- `PUT /vehicles/{id}`
- `DELETE /vehicles/{id}`

Payload `POST /vehicles`:

```json
{
  "make": "Toyota",
  "model": "Corolla",
  "year": 2020,
  "licensePlate": "ABC1D23",
  "ownerId": 1
}
```

Payload `PUT /vehicles/{vehicleId}`:

```json
{
  "make": "Toyota",
  "model": "Corolla",
  "year": 2021,
  "licensePlate": "ABC1D23"
}
```

## Observações

- `POST` retorna `201 Created` com header `Location`.
- A API possui tratamento de exceções com resposta padronizada.

Exemplo de resposta de erro:

```json
{
  "timestamp": "2025-01-01T12:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed. Please check your request parameters",
  "path": "/api/v1/vehicles",
  "details": [
    "licensePlate: Deve seguir o padrão brasileiro antigo ou Mercosul"
  ]
}
```

Principais códigos retornados:

- `400` para erros de validação.
- `404` quando o recurso não é encontrado.
- `409` para conflito (ex.: CPF/placa duplicados).
- `422` para violações de regra de negócio.
