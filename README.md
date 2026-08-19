# Transfer API

API REST de transferências bancárias construída com **Java 21** e **Spring Boot**, com foco em **consistência transacional** e **testes de integração com banco real** via Testcontainers.

![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.x-brightgreen?logo=springboot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue?logo=postgresql&logoColor=white)
![Flyway](https://img.shields.io/badge/Flyway-Migrations-red?logo=flyway&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker&logoColor=white)
![Testcontainers](https://img.shields.io/badge/Testcontainers-Integration%20Tests-9B489A)

---

## 📌 Sobre o projeto

A Transfer API simula o núcleo de um sistema bancário de transferências entre contas. Uma transferência debita a conta de origem e credita a conta de destino dentro de uma **única transação** (`@Transactional`): se qualquer etapa falhar — como saldo insuficiente —, toda a operação é revertida, garantindo que nenhum dinheiro "desapareça" ou seja criado no processo.

### Principais destaques técnicos

- **Atomicidade com `@Transactional`** — débito, crédito e registro da transferência acontecem em uma única transação com rollback automático em caso de falha
- **Validação de saldo** — transferências com saldo insuficiente são rejeitadas com `400 Bad Request` antes de qualquer alteração no banco
- **Lock pessimista contra race condition** — as contas são travadas com `SELECT ... FOR UPDATE` em ordem determinística de UUID, impedindo que transferências simultâneas furem a verificação de saldo e evitando deadlock quando A→B e B→A ocorrem ao mesmo tempo
- **Validação do payload** — Bean Validation rejeita valores nulos ou não positivos, e transferências entre a mesma conta são recusadas
- **Testes de integração com Testcontainers** — os testes sobem um **PostgreSQL real em container**, cobrindo o fluxo de sucesso e o de falha (não são mocks: o comportamento transacional é validado contra o banco de verdade)
- **Versionamento de schema com Flyway** — todas as tabelas são criadas por migrations SQL versionadas, reproduzíveis em qualquer ambiente
- **Configuração via variáveis de ambiente** — nenhuma credencial commitada no repositório

---

## 🛠️ Tecnologias

| Tecnologia | Uso |
|---|---|
| Java 21 | Linguagem |
| Spring Boot 4.x (Web MVC, Data JPA, Validation) | Framework da aplicação |
| PostgreSQL 16 | Banco de dados relacional |
| Flyway | Migrations e versionamento de schema |
| Docker Compose | Provisionamento do banco local |
| Testcontainers + JUnit 5 | Testes de integração com banco real |
| Lombok | Redução de boilerplate |
| Maven (wrapper incluso) | Build e gerenciamento de dependências |

---

## 🏗️ Arquitetura

O projeto segue uma arquitetura em camadas:

```
src/main/java/com/lucasmarques/transfer_api
├── controller/     # Endpoints REST (Transfer, Account)
├── service/        # Regras de negócio (débito, crédito, validação de saldo)
├── repository/     # Acesso a dados via Spring Data JPA
├── entity/         # Entidades JPA (Client, Account, Transfer)
├── dto/            # Objetos de transferência de dados (TransferRequest)
└── enums/          # StatusTransfer (SUCCESS, FAILED)
```

### Modelo de dados

```
Client 1 ──── N Account 1 ──── N Transfer (origem)
                       └────── N Transfer (destino)
```

- **Client** — nome, CPF e e-mail
- **Account** — saldo, número da conta e vínculo com o cliente
- **Transfer** — conta de origem, conta de destino, valor, data e status

Todas as chaves primárias são **UUID**, gerados pela aplicação.

---

## 🔗 Endpoints

### Criar transferência

```http
POST /api/transfers
Content-Type: application/json
```

```json
{
  "originId": "550e8400-e29b-41d4-a716-446655440000",
  "destinationId": "6ba7b810-9dad-11d1-80b4-00c04fd430c8",
  "amount": 150.00
}
```

| Resposta | Situação |
|---|---|
| `201 Created` | Transferência realizada — retorna o objeto `Transfer` com status `SUCCESS` |
| `400 Bad Request` | Saldo insuficiente na conta de origem |
| `404 Not Found` | Conta de origem ou destino inexistente |

### Consultar conta

```http
GET /api/accounts/{id}
```

| Resposta | Situação |
|---|---|
| `200 OK` | Retorna a conta com saldo atual |
| `404 Not Found` | Conta inexistente |

---

## 🚀 Como executar

### Pré-requisitos

- Java 21+
- Docker e Docker Compose
- (Opcional) Maven — o projeto inclui o Maven Wrapper (`./mvnw`)

### 1. Clone o repositório

```bash
git clone https://github.com/Lmsantoz/transfer-api.git
cd transfer-api
```

### 2. Configure as variáveis de ambiente

Crie um arquivo `.env` na raiz do projeto (ou exporte as variáveis no shell):

```env
POSTGRES_USERNAME=postgres
POSTGRES_PASSWORD=postgres
POSTGRES_DB=transfer_db
POSTGRES_URL=jdbc:postgresql://localhost:5433/transfer_db
```

> O banco expõe a porta **5433** no host para evitar conflito com instâncias locais do PostgreSQL na 5432.

### 3. Suba o banco de dados

```bash
docker compose up -d
```

### 4. Execute a aplicação

```bash
./mvnw spring-boot:run
```

As migrations do Flyway rodam automaticamente na inicialização, criando as tabelas `clients`, `accounts` e `transfer`.

A API estará disponível em `http://localhost:8080`.

---

## ✅ Testes

Os testes de integração usam **Testcontainers**: um container PostgreSQL é criado automaticamente durante a execução, sem necessidade de configurar banco manualmente — basta ter o Docker rodando.

```bash
./mvnw test
```

Cenários cobertos:

- **Transferência com sucesso** — valida o débito na origem e o crédito no destino consultando os saldos reais no banco
- **Transferência com saldo insuficiente** — valida que a operação é rejeitada e nenhum saldo é alterado
- **Transferências concorrentes** — 10 threads disparam transferências simultâneas contra a mesma conta; valida que o lock pessimista impede saldo negativo e que apenas as transferências cobertas pelo saldo são efetivadas
- **Conectividade** — valida a subida do container PostgreSQL

---

## 🗺️ Próximos passos

- [ ] Tratamento de erros centralizado com `@RestControllerAdvice` e payload de erro padronizado
- [ ] Endpoint de listagem de transferências com paginação
- [ ] Documentação interativa com Swagger / OpenAPI
- [ ] DTOs de resposta para desacoplar as entidades JPA da API pública

---

## 📄 Licença

Este projeto está sob a licença descrita no arquivo [LICENSE](LICENSE).

---

## 👤 Autor

**Lucas Marques**

[![GitHub](https://img.shields.io/badge/GitHub-Lmsantoz-181717?logo=github)](https://github.com/Lmsantoz)