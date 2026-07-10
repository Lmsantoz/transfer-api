# Transfer API

API REST para gerenciamento de clientes, contas bancárias e transferências entre contas, desenvolvida com Spring Boot 4 e Java 21.

## Índice

- [Stack tecnológica](#stack-tecnológica)
- [Modelo de domínio](#modelo-de-domínio)
- [Endpoints](#endpoints)
- [Regras de negócio](#regras-de-negócio)
- [Configuração e execução local](#configuração-e-execução-local)
- [Testes](#testes)
- [Estrutura do projeto](#estrutura-do-projeto)
- [Pontos de atenção conhecidos](#pontos-de-atenção-conhecidos)
- [Licença](#licença)

## Stack tecnológica

| Tecnologia | Versão |
|---|---|
| Java | 21 |
| Spring Boot (starter-parent) | 4.0.6 |
| Spring Data JPA | via BOM do Spring Boot |
| Spring Web MVC | via BOM do Spring Boot |
| Spring Validation | via BOM do Spring Boot |
| Flyway (`flyway-database-postgresql`) | via BOM do Spring Boot |
| PostgreSQL Driver | via BOM do Spring Boot |
| Lombok | via BOM do Spring Boot |
| Testcontainers (postgresql, junit-jupiter) | 1.21.0 |
| Build tool | Maven (via Maven Wrapper `mvnw`/`mvnw.cmd`) |
| Banco de dados | PostgreSQL 16 (alpine, via Docker) |

## Modelo de domínio

```
Client (1) ────< (N) Account (1) ────< (N) Transfer >──── (1) Account
```

### Client
- `id` (UUID, gerado)
- `name` (obrigatório)
- `cpf` (obrigatório, sem constraint de unicidade no banco)
- `email` (obrigatório, validado como e-mail)

### Account
- `id` (UUID, gerado)
- `balance` (`BigDecimal`, obrigatório)
- `numberAccount` (número da conta)
- `client` (relação `@ManyToOne` com `Client`)

### Transfer
- `id` (UUID, gerado)
- `originAccount` (`@ManyToOne` com `Account`)
- `destinationAccount` (`@ManyToOne` com `Account`)
- `amount` (`BigDecimal`, obrigatório)
- `transferDate` (`LocalDateTime`, obrigatório)
- `status` (enum `StatusTransfer`: `SUCCESS` ou `FAILED` — persistido como `STRING`)

O schema é criado e versionado via migrations Flyway em `src/main/resources/db/migration` (`V1__create_clients_table.sql`, `V2__create_accounts_table.sql`, `V3__create_transfer_table.sql`).

## Endpoints

### `GET /api/accounts/{id}`

Busca uma conta pelo seu identificador.

- **Path param:** `id` (UUID)
- **Resposta:** `200 OK` com o objeto `Account` (incluindo o `Client` relacionado)
- **Erros:** `404 Not Found` se a conta não existir

### `POST /api/transfers`

Cria uma transferência entre duas contas.

- **Request body:**
```json
{
  "originId": "uuid-da-conta-origem",
  "destinationId": "uuid-da-conta-destino",
  "amount": 100.00
}
```
- **Resposta:** `201 Created` com o objeto `Transfer` criado (incluindo as contas de origem e destino)
- **Erros:**
  - `404 Not Found` — conta de origem ou destino inexistente
  - `400 Bad Request` (mensagem `"valor insuficiente"`) — saldo da conta de origem menor que o valor da transferência

> O DTO `TransferRequest` não possui anotações de validação (Bean Validation) e o controller não usa `@Valid`, portanto não há validação formal de campos obrigatórios ou de valores negativos/zero em `amount`.

## Regras de negócio

Implementadas em `TransferService`:

1. As contas de origem e destino precisam existir, senão a API responde `404 Not Found`.
2. O saldo da conta de origem é verificado antes do débito: se `balance < amount`, a operação é abortada com `400 Bad Request` e nenhuma alteração é persistida.
3. A transferência é executada dentro de uma transação (`@Transactional`): débito na origem, crédito no destino e criação do registro `Transfer` com status `SUCCESS` e data/hora atual.
4. Não há validação impedindo transferência de uma conta para ela mesma.
5. O status `FAILED` existe no enum `StatusTransfer`, mas não é utilizado atualmente — falhas resultam em exceção antes de qualquer persistência, e não em um registro de transferência com esse status.
6. O tratamento de erros é feito diretamente nos services via `ResponseStatusException` (não há classes de exceção customizadas nem `@ControllerAdvice`).

## Configuração e execução local

### Pré-requisitos

- JDK 21
- Docker e Docker Compose

### 1. Subir o banco de dados

```bash
docker compose up -d
```

Isso sobe um container PostgreSQL 16 (`transfer_db`) na porta `5433` (host) → `5432` (container), com um volume nomeado (`postgres_data`) para persistência.

> **Atenção:** o `docker-compose.yml` expõe o banco na porta `5433`, mas o exemplo de `POSTGRES_URL` abaixo usa a porta padrão `5432`. Ajuste a porta na sua URL JDBC conforme a que o container realmente expõe na sua máquina (`5433`, a menos que você altere o mapeamento).

### 2. Variáveis de ambiente

Crie um arquivo `.env` na raiz do projeto com:

```env
POSTGRES_URL=jdbc:postgresql://localhost:5433/transfer_api
POSTGRES_USERNAME=postgres
POSTGRES_PASSWORD=<sua-senha>
POSTGRES_DB=transfer_api
```

Essas variáveis são consumidas tanto pelo `docker-compose.yml` (usuário/senha/nome do banco do container) quanto pelo `application.properties` (URL/usuário/senha do datasource da aplicação).

### 3. Rodar a aplicação

```bash
./mvnw spring-boot:run
```

(No Windows, use `mvnw.cmd spring-boot:run`.)

A aplicação sobe na porta padrão do Spring Boot, **8080** (não há `server.port` customizado). As migrations Flyway são aplicadas automaticamente na inicialização.

## Testes

Localizados em `src/test/java`. Atualmente há um único arquivo de teste, `TransferServiceTest`, que é um **teste de integração** (usa `@SpringBootTest` + Testcontainers, subindo um container PostgreSQL efêmero em vez de mocks). Cenários cobertos:

- Verificação de que o container de banco sobe corretamente.
- Transferência bem-sucedida entre duas contas, validando os novos saldos de origem e destino.
- Transferência com saldo insuficiente, validando que uma `ResponseStatusException` é lançada.

Para rodar os testes:

```bash
./mvnw test
```

> Requer Docker disponível localmente, pois o Testcontainers sobe um container PostgreSQL real durante a execução dos testes.

## Estrutura do projeto

```
src/main/java/com/lucasmarques/transfer_api/
├── TransferApiApplication.java
├── controller/
│   ├── AccountController.java
│   └── TransferController.java
├── dto/
│   └── TransferRequest.java
├── entity/
│   ├── Account.java
│   ├── Client.java
│   └── Transfer.java
├── enums/
│   └── StatusTransfer.java
├── repository/
│   ├── AccountRepository.java
│   ├── ClientRepository.java
│   └── TransferRepository.java
└── service/
    ├── AccountService.java
    └── TransferService.java

src/main/resources/
├── application.properties
└── db/migration/
    ├── V1__create_clients_table.sql
    ├── V2__create_accounts_table.sql
    └── V3__create_transfer_table.sql

src/test/java/com/lucasmarques/transfer_api/
└── TransferServiceTest.java
```

## Pontos de atenção conhecidos

- O `.env` está listado no `.gitignore`, mas já foi commitado ao repositório em algum momento — revise o histórico e rotacione credenciais se necessário.
- `TransferService.findAll(Pageable)` existe mas não é exposto por nenhum endpoint de listagem.
- Não há constraint de unicidade para `cpf` em `clients` no schema do banco.
- `accounts.balance` e `transfer.amount` usam `DECIMAL(8,2)`, limitando valores a no máximo ~999999.99.

## Licença

Este projeto está licenciado sob a licença MIT — veja o arquivo [LICENSE](LICENSE) para mais detalhes.
