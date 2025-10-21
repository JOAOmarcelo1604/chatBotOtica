# 🤖 Bot WhatsApp para Ótica Visual

Este projeto é um assistente virtual (chatbot) para o WhatsApp, desenvolvido para a **Ótica Visual**. O objetivo é automatizar o primeiro nível de atendimento ao cliente, respondendo a perguntas frequentes e coletando informações para agendamentos, 24 horas por dia.

O projeto é construído em **Java** com o framework **Spring Boot** e se conecta ao WhatsApp através da **Evolution API** (enquanto aguarda a aprovação da API oficial via Twilio/Meta).

## ✨ Funcionalidades Atuais

* **Saudação e Menu Dinâmico:** Responde a saudações ("olá", "oi", "bom dia", etc.) com um menu principal numerado.
* **Verificação de Horário de Atendimento:** O bot verifica automaticamente o horário comercial (Seg-Sex: 8h-18h, Sáb: 8h-13h) e envia uma mensagem de "ausência" se o cliente chamar fora desse horário.
* **Serviços Automatizados:**
    * **1. Agendar exame:** Coleta o nome completo do cliente.
    * **2. Consultar exame/pedido:** Coleta o CPF do cliente.
    * **3. Preços:** Fornece informações iniciais e direciona para um atendente.
    * **4. Horário de funcionamento:** Informa os horários da loja.
    * **5. Formas de pagamento:** Informa as formas de pagamento aceitas.
* **Gerenciamento de Estado:** O bot "lembra" o que perguntou. Se ele pede um nome (opção 1), ele sabe que a próxima mensagem será o nome, permitindo um fluxo de conversa coeso.
* **Transferência para Atendimento Humano (Handoff):**
    * Quando um cliente escolhe a opção "6. Falar com um atendente" (ou conclui a coleta de dados), o bot entra em modo silencioso (`HUMAN_CHAT_ACTIVE`).
    * Isso permite que um atendente humano assuma a conversa sem a interferência do bot.
* **Reativação Automática (Timeout):**
    * Se um chat transferido para um humano ficar inativo por um período configurável (atualmente em 3 minutos para testes), o bot envia uma mensagem de encerramento por inatividade e "pega" o cliente de volta, deixando-o pronto para um novo atendimento automático.

## 🛠️ Tecnologias Utilizadas

* **Backend:** Java 17+
* **Framework:** Spring Boot 3+
    * `Spring Web`: Para criar a API REST que recebe os webhooks.
    * `Spring Scheduling`: Para a tarefa agendada de limpeza de chats inativos.
* **Conexão WhatsApp:** [Evolution API](https://github.com/EvolutionAPI/evolution-api) (atualmente)
* **Ferramentas de Desenvolvimento:**
    * `Maven`: Gerenciador de dependências.
    * `ngrok`: Para expor a aplicação local (na porta `8081`) para a internet e receber os webhooks.

## 🚀 Como Executar o Projeto

Para rodar este projeto, você precisa de 3 componentes rodando simultaneamente: A **Evolution API**, esta **Aplicação Spring Boot** e o **ngrok**.

### Pré-requisitos

* JDK 17 ou superior.
* Maven 3.x.
* Conta no [ngrok](https://ngrok.com/).
* Uma instância da [Evolution API](https://github.com/EvolutionAPI/evolution-api) instalada e rodando (ex: via Docker) com um número de WhatsApp de teste conectado.

### 1. Configurar e Rodar a Evolution API

1.  Certifique-se que sua instância da Evolution API está rodando na porta `8080`.
2.  No arquivo `.env` da Evolution API, certifique-se de que o webhook global está ativado:
    ```env
    WEBHOOK_GLOBAL_ENABLED=true
    WEBHOOK_EVENTS_MESSAGES_UPSERT=true
    ```
3.  Deixe o `WEBHOOK_GLOBAL_URL` em branco por enquanto.

### 2. Configurar e Rodar a Aplicação Spring Boot (Este Projeto)

1.  Clone este repositório:
    ```bash
    git clone [URL-DO-SEU-REPOSITÓRIO]
    ```
2.  Abra o projeto em sua IDE (IntelliJ, VSCode, etc).
3.  Edite o arquivo `src/main/resources/application.properties`:
    ```properties
    # Define a porta da aplicação Spring, para não conflitar com a Evolution API
    server.port=8081

    # Configurações da Evolution API
    evolution.api.url=http://localhost:8080
    evolution.api.key=[SUA-API-KEY-GLOBAL-DA-EVOLUTION]
    evolution.instance.name=[NOME-DA-SUA-INSTANCIA-EVOLUTION]
    ```
4.  Rode a aplicação Spring Boot pela sua IDE ou pelo terminal:
    ```bash
    mvn spring-boot:run
    ```

### 3. Configurar o Túnel com ngrok

1.  Em um **novo terminal**, inicie o `ngrok` para expor a porta `8081` (onde o Spring está rodando):
    ```bash
    ngrok http 8081
    ```
2.  O `ngrok` vai gerar uma URL pública (ex: `https://seu-id-aleatorio.ngrok-free.app`). **Copie essa URL.**

### 4. Conectar Tudo

1.  Volte ao arquivo `.env` da **Evolution API**.
2.  Cole a URL do `ngrok` no campo `WEBHOOK_GLOBAL_URL`, adicionando o caminho do nosso controller:
    ```env
    WEBHOOK_GLOBAL_URL='[https://seu-id-aleatorio.ngrok-free.app/api/whatsapp/webhook](https://seu-id-aleatorio.ngrok-free.app/api/whatsapp/webhook)'
    ```
3.  **Reinicie sua instância da Evolution API** (ex: `docker-compose down && docker-compose up -d`) para que ela leia a nova URL do webhook.

Pronto! Agora você pode enviar uma mensagem "Olá" para o seu número de teste e o fluxo será iniciado.

## 🗺️ Planos Futuros (Roadmap)

-   [ ] **Migração para API Oficial:** Substituir a Evolution API pela **API Oficial do WhatsApp via Twilio** (assim que a verificação da empresa na Meta for concluída).
-   [ ] **Interface de Atendimento:** Integrar o bot com uma plataforma de atendimento humano open source, como o **[Chatwoot](https://github.com/chatwoot/chatwoot)**, para gerenciar de forma profissional as conversas transferidas.
-   [ ] **Persistência de Dados:** Migrar o gerenciamento de estado (`conversationState`) de um `Map` em memória para um banco de dados (como H2 ou PostgreSQL) para que as conversas não sejam perdidas se a aplicação reiniciar.
-   [ ] **Botões Interativos:** Após a migração para a API oficial, implementar botões de resposta rápida e listas, tornando a interação mais fácil para o usuário.
