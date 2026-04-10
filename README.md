# Vehicle Management API

API REST para gerenciamento de clientes e veículos, construída com Java 25, Spring Boot, Spring Data JPA e MySQL.

## Visão Geral

A aplicação expõe operações para:

- cadastrar, consultar, atualizar e remover clientes;
- cadastrar, consultar, atualizar e remover veículos;
- listar veículos por proprietário com `GET /api/v1/vehicles?ownerId={ownerId}`.

Antes da persistência, a aplicação normaliza alguns campos automaticamente:

- `name`, `make`, `model` e `licensePlate` são gravados em maiúsculas;
- `email` é gravado em minúsculas;
- `cpf` é salvo sem pontos, hífen ou espaços;
- `licensePlate` é salva sem hífen.

## Stack

- Java 25
- Spring Boot 4.0.3
- Spring Web MVC
- Spring Data JPA / Hibernate
- Bean Validation
- MySQL
- H2 Database para testes
- MapStruct 1.5.5.Final
- Lombok
- Springdoc OpenAPI 3.0.2
- Gradle Wrapper 9.3.1

## Requisitos

- Java 25
- MySQL 8+

## Configuração

A configuração principal está em [src/main/resources/application.yaml](/home/danielle/IdeaProjects/vehicle-management-api/src/main/resources/application.yaml).

A aplicação espera as seguintes variáveis de ambiente para conectar ao MySQL:

- `URL_DB`
- `USER_DB`
- `PASSWORD_DB`

Exemplo:

```bash
export URL_DB=jdbc:mysql://localhost:3306/vehicle_management
export USER_DB=root
export PASSWORD_DB=secret
```

Os testes usam o perfil `test` com banco H2 em memória, definido em [src/main/resources/application-test.yaml](/home/danielle/IdeaProjects/vehicle-management-api/src/main/resources/application-test.yaml).

## Como Executar

Subir a aplicação:

```bash
./gradlew bootRun
```

Executar todos os testes:

```bash
./gradlew test
```

Executar um teste específico:

```bash
./gradlew test --tests com.github.daniellevieira.vehiclemanagementapi.controller.ClientControllerTest
```

## Documentação da API

Com a aplicação em execução:

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

Resposta de exemplo:

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

Resposta de exemplo:

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

- `name`: obrigatório, entre 3 e 100 caracteres;
- `email`: obrigatório, formato válido, até 255 caracteres;
- `cpf`: obrigatório e válido;
- `birthDate`: obrigatória e deve estar no passado;
- `clientId`: deve ser positivo nos endpoints com path parameter.

### Veículos

- `make`: obrigatório, entre 3 e 50 caracteres;
- `model`: obrigatório, entre 3 e 100 caracteres;
- `year`: obrigatório e mínimo `1886`;
- `licensePlate`: obrigatória e deve seguir o padrão brasileiro antigo ou Mercosul;
- `ownerId`: obrigatório no `POST /vehicles` e deve ser positivo;
- `vehicleId`: deve ser positivo nos endpoints com path parameter.

## Regras de Negócio

- não é possível cadastrar ou atualizar cliente com CPF já existente;
- não é possível cadastrar ou atualizar veículo com placa já existente;
- o ano do veículo não pode ser maior que o ano atual;
- um veículo só pode ser criado para um cliente existente;
- a listagem `GET /vehicles?ownerId={ownerId}` exige que o proprietário exista;
- o `PUT /vehicles/{vehicleId}` não permite alterar o proprietário.

## Tratamento de Erros

A API retorna uma estrutura padronizada para erros:

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

Principais respostas:

- `400 Bad Request`: erro de validação, body ausente, parâmetro inválido ou JSON malformado;
- `404 Not Found`: cliente, veículo ou proprietário não encontrado;
- `409 Conflict`: CPF ou placa já cadastrados;
- `422 Unprocessable Content`: violação de regra de negócio, como ano futuro.

## Testes

O projeto possui testes de:

- controller de clientes;
- controller de veículos;
- service de clientes;
- service de veículos;
- integração dos fluxos de clientes;
- integração dos fluxos de veículos.

Os testes automatizados usam H2 em memória com o perfil `test`.
