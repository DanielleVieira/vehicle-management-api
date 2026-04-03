# Vehicle Management API

API REST para gerenciamento de clientes e veiculos, construída com Java 25, Spring Boot, Spring Data JPA e MySQL.

## Visão Geral

A aplicação expõe endpoints para:

- cadastrar, consultar, atualizar e remover clientes;
- cadastrar, consultar, atualizar e remover veiculos;
- listar veiculos de um proprietário específico.

Algumas normalizações acontecem automaticamente antes da persistência:

- `name`, `make`, `model` e `licensePlate` são gravados em maiúsculas;
- `email` é gravado em minúsculas;
- `cpf` e `licensePlate` são salvos sem pontuação.

## Stack

- Java 25
- Spring Boot 4.0.3
- Spring Web MVC
- Spring Data JPA / Hibernate
- Bean Validation
- MySQL
- MapStruct 1.5.5.Final
- Lombok
- Springdoc OpenAPI
- Gradle 9.3.1 Wrapper

## Requisitos

- Java 25
- MySQL 8+

## Configuração

A aplicação lê a conexão com o banco a partir destas variáveis de ambiente:

- `URL_DB`
- `USER_DB`
- `PASSWORD_DB`

Exemplo:

```bash
export URL_DB=jdbc:mysql://localhost:3306/vehicle_management
export USER_DB=root
export PASSWORD_DB=secret
```

O datasource é configurado em [application.yaml](/home/danielle/IdeaProjects/vehicle-management-api/src/main/resources/application.yaml).

## Como Executar

Subir a aplicação:

```bash
./gradlew bootRun
```

Executar os testes:

```bash
./gradlew test
```

Executar um teste específico:

```bash
./gradlew test --tests com.github.daniellevieira.vehiclemanagementapi.controller.ClientControllerTest
```

## Documentação da API

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## Endpoints

Base URL: `/api/v1`

### Clientes

- `POST /clients`
- `GET /clients`
- `GET /clients/{clientId}`
- `PUT /clients/{clientId}`
- `DELETE /clients/{clientId}`

Payload de criação e atualização:

```json
{
  "name": "Ana Silva",
  "email": "ana@exemplo.com",
  "cpf": "123.456.789-09",
  "birthDate": "1990-05-20"
}
```

Exemplo de resposta:

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
- `DELETE /vehicles/{vehicleId}`

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

Exemplo de resposta:

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

## Regras de Validação

### Clientes

- `name`: obrigatório, entre 3 e 100 caracteres.
- `email`: obrigatório, email válido, até 255 caracteres.
- `cpf`: obrigatório e válido no padrão CPF.
- `birthDate`: obrigatória e deve estar no passado.
- `clientId`: obrigatório nos endpoints com path parameter e deve ser positivo.

### Veículos

- `make`: obrigatório, entre 3 e 50 caracteres.
- `model`: obrigatório, entre 3 e 100 caracteres.
- `year`: obrigatório e mínimo `1886`.
- `licensePlate`: obrigatória, entre 7 e 8 caracteres, formato brasileiro antigo ou Mercosul.
- `ownerId`: obrigatório no `POST /vehicles` e deve ser positivo.
- `vehicleId`: obrigatório nos endpoints com path parameter e deve ser positivo.

## Regras de Negócio

- não é possível cadastrar ou atualizar cliente com CPF já existente;
- não é possível cadastrar ou atualizar veículo com placa já existente;
- o ano do veículo não pode ser maior que o ano atual;
- um veículo só pode ser criado para um cliente existente;
- a consulta `GET /vehicles?ownerId={ownerId}` exige que o proprietário exista;
- o `PUT /vehicles/{vehicleId}` não permite alterar o proprietário.

## Tratamento de Erros

A API retorna uma estrutura padronizada para erros:

```json
{
  "timestamp": "2026-04-02T12:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed. Please check your request parameters",
  "path": "/api/v1/vehicles",
  "details": [
    "licensePlate: Deve seguir o padrão brasileiro antigo ou Mercosul"
  ]
}
```

Principais status retornados:

- `400 Bad Request`: erro de validação, tipo inválido ou request malformado;
- `404 Not Found`: recurso não encontrado;
- `409 Conflict`: CPF ou placa já cadastrados;
- `422 Unprocessable Content`: violação de regra de negócio.

## Testes Existentes

Atualmente o projeto possui testes para:

- contexto da aplicação;
- `ClientController`;
- `ClientService`.
