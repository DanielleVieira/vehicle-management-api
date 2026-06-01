# Vehicle Management API

API REST para gerenciamento de clientes e veículos, com cadastro/login de usuários, autenticação JWT e controle de acesso por perfil.

## Stack

- Java 25
- Spring Boot 4.0.3
- Spring Web MVC
- Spring Data JPA / Hibernate
- Spring Security
- JWT com `jjwt` 0.13.0
- Bean Validation
- MySQL
- H2 para testes
- MapStruct
- Lombok
- Springdoc OpenAPI
- Gradle Wrapper
- Docker / Docker Compose

## Funcionalidades

- CRUD de clientes.
- CRUD de veículos.
- Listagem paginada de clientes e veículos.
- Filtro de veículos por proprietário com `GET /api/v1/vehicles?ownerId={ownerId}`.
- Cadastro e login de usuários.
- Autenticação stateless com JWT.
- Autorização por perfis `USER` e `ADMIN`.
- Seed automático de usuário administrador na inicialização.

## Requisitos

- Java 25.
- Docker e Docker Compose, caso use o banco via container.
- MySQL 8+, caso rode o banco fora do Compose.

## Configuração

A aplicação lê as configurações de ambiente em [application.yaml](/home/danielle/IdeaProjects/vehicle-management-api/src/main/resources/application.yaml). Para testes, usa [application-test.yaml](/home/danielle/IdeaProjects/vehicle-management-api/src/main/resources/application-test.yaml) com H2 em memória.

Variáveis usadas pela aplicação:

```env
URL_DB=jdbc:mysql://localhost:3306/<database_name>
USER_DB=<database_user>
PASSWORD_DB=<database_password>
DDL_AUTO=update
SECRET_JWT=<base64_jwt_secret>
JWT_EXPIRATION=1800000
ADMIN_USERNAME=<admin_email>
ADMIN_PASSWORD=<admin_password>
```

Variáveis extras usadas pelo `docker-compose.yml` para criar o banco:

```env
MYSQL_ROOT_PASSWORD=<mysql_root_password>
MYSQL_DATABASE=<database_name>
```

Observações:

- `SECRET_JWT` precisa estar em Base64, pois o token é assinado a partir de `Decoders.BASE64.decode(...)`.
- `JWT_EXPIRATION` é definido em milissegundos. O exemplo `1800000` equivale a 30 minutos.
- `DDL_AUTO` controla o `spring.jpa.hibernate.ddl-auto`; para desenvolvimento, `update` é suficiente.
- Na inicialização, a aplicação cria o usuário `ADMIN_USERNAME` como `ADMIN` ou promove esse usuário se ele já existir.

## Executando Com Gradle

Suba um MySQL local e exporte as variáveis de ambiente antes de iniciar a API.

```bash
export URL_DB=jdbc:mysql://localhost:3306/<database_name>
export USER_DB=<database_user>
export PASSWORD_DB=<database_password>
export DDL_AUTO=update
export SECRET_JWT=<base64_jwt_secret>
export JWT_EXPIRATION=1800000
export ADMIN_USERNAME=<admin_email>
export ADMIN_PASSWORD=<admin_password>
```

Rodar a aplicação:

```bash
./gradlew bootRun
```

Executar os testes:

```bash
./gradlew test
```

Gerar o artefato:

```bash
./gradlew build
```

Executar o JAR gerado:

```bash
java -jar build/libs/vehicle-management-api-0.0.1-SNAPSHOT.jar
```

## Executando Com Docker Compose

O Compose sobe dois serviços: `mysql` e `app`.

```bash
docker compose up --build
```

Para parar:

```bash
docker compose down
```

Para remover também o volume do MySQL:

```bash
docker compose down -v
```

## Documentação Da API

Com a aplicação rodando:

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## Autenticação

Rotas públicas:

- `POST /api/v1/auth/signup`
- `POST /api/v1/auth/login`
- `/swagger-ui/**`
- `/v3/api-docs/**`

As demais rotas exigem JWT no cabeçalho:

```http
Authorization: Bearer <token>
```

Regras de acesso:

- `DELETE /api/v1/clients/**`: somente `ADMIN`.
- `DELETE /api/v1/vehicles/**`: somente `ADMIN`.
- `/api/v1/users/**`: somente `ADMIN`.
- Demais rotas autenticadas: `USER` ou `ADMIN`.

CORS está liberado para:

- `http://localhost:3000`
- `http://localhost:5173`

## Paginação E Ordenação

As listagens usam `Pageable` do Spring:

- `GET /api/v1/clients?page=0&size=10&sort=name,asc`
- `GET /api/v1/vehicles?page=0&size=10&sort=model,asc`
- `GET /api/v1/vehicles?ownerId=1&page=0&size=10`

Padrões:

- Clientes: `page=0`, `size=10`, `sort=name,asc`.
- Veículos: `page=0`, `size=10`, `sort=model,asc`.
- Tamanho máximo de página: `100`.

## Endpoints

Base URL: `/api/v1`

### Auth

`POST /auth/signup`

```json
{
  "email": "user@example.com",
  "password": "secret123"
}
```

`POST /auth/login`

```json
{
  "email": "user@example.com",
  "password": "secret123"
}
```

Resposta:

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

### Clientes

- `POST /clients`: cria cliente.
- `GET /clients`: lista clientes com paginação.
- `GET /clients/{clientId}`: consulta cliente por id.
- `PUT /clients/{clientId}`: atualiza cliente.
- `DELETE /clients/{clientId}`: remove cliente. Requer `ADMIN`.

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

- `POST /vehicles`: cria veículo para um cliente existente.
- `GET /vehicles`: lista veículos com paginação.
- `GET /vehicles?ownerId={ownerId}`: lista veículos de um proprietário.
- `GET /vehicles/{vehicleId}`: consulta veículo por id.
- `PUT /vehicles/{vehicleId}`: atualiza dados do veículo.
- `DELETE /vehicles/{vehicleId}`: remove veículo. Requer `ADMIN`.

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
  "licensePlate": "ABC-1234"
}
```

Resposta:

```json
{
  "id": 10,
  "make": "TOYOTA",
  "model": "COROLLA",
  "year": 2021,
  "licensePlate": "ABC1234",
  "owner": {
    "id": 1,
    "name": "ANA SILVA",
    "email": "ana@exemplo.com",
    "cpf": "12345678909",
    "birthDate": "1990-05-20"
  }
}
```

## Validações

Auth e usuários:

- `email`: obrigatório, formato válido e no máximo 255 caracteres.
- `password` no cadastro: obrigatório, entre 8 e 72 caracteres.
- `password` no login: obrigatório.

Clientes:

- `name`: obrigatório, entre 3 e 100 caracteres.
- `email`: obrigatório, formato válido e no máximo 255 caracteres.
- `cpf`: obrigatório e válido.
- `birthDate`: obrigatória e deve estar no passado.
- `clientId`: deve ser positivo.

Veículos:

- `make`: obrigatório, entre 3 e 50 caracteres.
- `model`: obrigatório, entre 3 e 100 caracteres.
- `year`: obrigatório e mínimo `1886`.
- `licensePlate`: obrigatória, aceita padrão brasileiro antigo (`ABC-1234` ou `ABC1234`) ou Mercosul (`ABC1D23`).
- `ownerId`: obrigatório no cadastro e deve ser positivo.
- `vehicleId`: deve ser positivo.

## Regras De Negócio

- `name`, `make` e `model` são persistidos em maiúsculas.
- `email` é persistido em minúsculas.
- `cpf` é persistido sem pontos, espaços ou hífen.
- `licensePlate` é persistida em maiúsculas e sem hífen.
- Não é possível cadastrar ou atualizar cliente com CPF já registrado.
- Não é possível cadastrar usuário com email já registrado.
- Não é possível cadastrar ou atualizar veículo com placa já registrada.
- O ano do veículo não pode ser maior que o ano atual.
- Um veículo só pode ser criado para um cliente existente.
- `GET /vehicles?ownerId={ownerId}` exige que o proprietário exista.
- `PUT /vehicles/{vehicleId}` não altera o proprietário.

## Respostas De Erro

A API retorna um payload padrão para erros tratados:

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

Códigos comuns:

- `400 Bad Request`: validação, JSON inválido, parâmetro inválido ou ordenação por campo inexistente.
- `401 Unauthorized`: token ausente, inválido ou credenciais incorretas.
- `403 Forbidden`: usuário autenticado sem permissão.
- `404 Not Found`: recurso não encontrado.
- `409 Conflict`: CPF, email ou placa duplicados; ou violação de integridade no banco.
- `422 Unprocessable Content`: regra de negócio violada, como ano maior que o atual.
- `500 Internal Server Error`: falha não tratada.
