# MeepleQueue 🎲 📌

MeepleQueue é um sistema de alta performance e escalabilidade voltado para a solicitação e o processamento assíncrono de reservas de mesas em tavernas de jogos de tabuleiro (*board games*). 

O projeto foi desenvolvido utilizando uma **Arquitetura Orientada a Eventos (EDA)** com **Microsserviços/Módulos Independentes**, separando a recepção de tráfego de alta disponibilidade (API REST) do processamento pesado de regras de negócio (Worker), utilizando o **RabbitMQ** como Message Broker e **MySQL** como banco de dados relacional.

---

## 🛠️ Stack Tecnológica

- **Linguagem:** Java 21 (LTS)
- **Framework Principal:** Spring Boot 3.x
- **Mensageria:** RabbitMQ (Protocolo AMQP)
- **Banco de Dados:** MySQL 8.x
- **Migração de Banco:** Flyway Database Migration
- **Documentação:** SpringDoc OpenAPI 3 (Swagger UI)
- **Mapeamento e Utilitários:** MapStruct, Lombok, Jackson JSR310 Module
- **Orquestração:** Docker & Docker Compose
- **Qualidade & Testes:** JUnit 5, Mockito, Spring Boot Test (Unitários, Integração e E2E)

---

## 📐 Arquitetura e Fluxo de Dados

O projeto adota a estratégia de **Package by Feature (Empacotamento por Funcionalidade)**, garantindo alta coesão, baixo acoplamento e facilitando a futura extração dos módulos para microsserviços 100% isolados.

```text
[ Cliente / Postman / Swagger ]
               │
               ▼ (HTTP POST /reservas)
   ┌───────────────────────┐
   │     api-reservas      │ ───► [ Valida JSON, mapeia DTO, salva PENDENTE ]
   └───────────────────────┘
               │
               ▼ (Envia ID via RabbitTemplate)
      [ Exchange: reservas.ex ] 
               │ (Routing Key: reserva.solicitada)
               ▼
    [ Fila: processar-reserva.queue ]
               │
               ▼ (Consome Assincronamente)
   ┌───────────────────────┐
   │    worker-reservas    │ ───► [ Valida Regras de Negócio Reais ]
   └───────────────────────┘
               │
               ▼ (Atualiza Status: CONFIRMADA / REJEITADA)
          [ MySQL DB ]
```

### Detalhes dos Componentes:
1. **`api-reservas` (Producer):** Endpoint HTTP ultra veloz. Recebe o payload, converte para entidade utilizando MapStruct, persiste o registro com o status `PENDENTE` e publica o ID da reserva na Exchange do RabbitMQ.
2. **`RabbitMQ Broker`:** Orquestra a distribuição de mensagens garantindo resiliência (filas duráveis). Se o Worker cair, as mensagens ficam salvas na fila de forma segura.
3. **`worker-reservas` (Consumer):** Escuta a fila de maneira assíncrona. Ao capturar uma mensagem, busca os dados completos no banco de dados e processa as regras de validação.

---

## 📋 Regras de Negócio (Worker Engine)

O Worker processa cada reserva aplicando validações lógicas rígidas, eliminando *hardcodes* e simulando um cenário real de operação de uma taverna:

- **Validação Cronológica:** Reservas para datas passadas são automaticamente **REJEITADAS**.
- **Validação de Capacidade da Mesa:** O estabelecimento possui mesas planejadas para acomodar grupos de **1 a 8 jogadores**. Solicitações fora dessa faixa são **REJEITADAS**.
- **Garantia de Estado:** Solicitações bem-sucedidas são alteradas para **CONFIRMADA** diretamente no banco de dados.

---

## 🗄️ Versionamento de Banco de Dados (Flyway)

A evolução do banco de dados é tratada de forma estritamente profissional usando migrações versionadas, eliminando o uso de `ddl-auto=update` em produção.

- **`V1__create_table_reservas.sql`**: Estrutura inicial da tabela de reservas.
- **`V2__update_reservas_table.sql`**: Migração para suporte a práticas modernas de persistência, incluindo **Soft Delete** (`deleted_at`) e auditoria nativa (`created_at`).

---

## 🧪 Pirâmide de Testes Completa

O projeto conta com uma cobertura rigorosa de testes automatizados, garantindo a estabilidade e a prevenção de regressões em qualquer refatoração:

- **Testes Unitários (JUnit 5 & Mockito):** Isolamento total das regras de negócio do Worker e comportamentos dos mappers.
- **Testes de Integração:** Validação da integração do Spring Data JPA com o banco de dados e publicação de mensagens no container do RabbitMQ.
- **Testes End-to-End (E2E):** Fluxo completo simulado, disparando uma chamada HTTP no Controller e validando o ciclo de vida do dado até o processamento assíncrono final.

---

## 🚀 Como Rodar o Projeto

### Pré-requisitos
- Docker e Docker Compose instalados.
- JDK 21 configurado (caso queira rodar os serviços fora do container).

### Passo 1: Subir a Infraestrutura (Docker)
Na raiz do projeto (onde está o arquivo `docker-compose.yml`), execute o terminal e digite:
```bash
docker compose up -d
```
> Isso iniciará o container do **MySQL 8** (porta interna exposta na `3307`) e o **RabbitMQ** (painel administrativo na porta `15672`).

### Passo 2: Configurar Variáveis de Ambiente
Tanto a API quanto o Worker utilizam injeção segura de credenciais. Configure as seguintes **Environment Variables** nas configurações de execução (Run/Debug Configurations) da sua IDE (IntelliJ/Eclipse):
- `DB_USER` = `admin`
- `DB_PASSWORD` = `admin`
- `RABBIT_USER` = `admin`
- `RABBIT_PASSWORD` = `admin`

### Passo 3: Executar as Aplicações
1. Execute a classe `ApiReservasApplication.java` para subir a API (Porta `8080`). O Flyway criará o banco automaticamente se ele não existir.
2. Execute a classe `WorkerReservasApplication.java` para ativar o consumidor.

---

## 📖 Documentação da API (Swagger / UI)

A API possui documentação interativa integrada com o Swagger UI, mapeando tipos de dados, payloads de exemplo e códigos de resposta HTTP.

- **Acesso local:** [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

### Exemplo de Payload (`POST /reservas`)

```json
{
  "nomeJogador": "Jogador",
  "nomeJogo": "Root",
  "dataPartida": "2026-06-15",
  "quantidadeJogadores": 4
}
```

**Resposta de Sucesso (HTTP 201 Created):**
```json
{
  "id": 1,
  "nomeJogador": "Jogador",
  "nomeJogo": "Root",
  "dataPartida": "2026-06-15",
  "quantidadeJogadores": 4,
  "status": "PENDENTE",
  "createdAt": "2026-05-26T14:30:00"
}
```

---

## 👥 Contato

Desenvolvido por **Matheus Osses De Lima** Sinta-se à vontade para explorar o código, rodar os testes e contribuir com o projeto! 😉
