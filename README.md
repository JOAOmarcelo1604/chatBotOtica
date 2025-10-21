🤖 Bot de Atendimento WhatsApp para Ótica Visual
Este é um projeto de um assistente virtual (chatbot) para WhatsApp, desenvolvido para automatizar o primeiro atendimento da Ótica Visual. O bot é capaz de responder a saudações, apresentar um menu de opções, filtrar solicitações de clientes e transferir para o atendimento humano quando necessário.

O projeto foi construído com Java e Spring Boot, e utiliza a Evolution API para a conexão com o WhatsApp.

✨ Funcionalidades Principais
Menu de Navegação: Apresenta um menu de opções por números para guiar o cliente.

Verificação de Horário: Detecta automaticamente se a mensagem foi recebida fora do horário comercial e envia uma mensagem de ausência.

Gerenciamento de Estado: O bot "lembra" o que perguntou ao cliente (ex: aguardando um nome ou CPF) para dar continuidade ao fluxo de conversa.

Handoff para Atendimento Humano: Em fluxos específicos (agendamento, consulta) ou por solicitação direta (opção "Falar com atendente"), o bot entra em modo "silencioso" para que um operador humano possa assumir a conversa.

Time-out de Conversa: Reativa automaticamente o bot para um cliente se a conversa com o atendente humano ficar inativa por um período configurado (ex: 3 horas), evitando que clientes fiquem "presos" no atendimento humano.

Fluxos de Conversa Implementados:

Agendamento de exame de vista (coleta o nome).

Consulta de exame ou status de pedido (coleta o CPF).

Informações sobre preços.

Informações sobre horário de funcionamento.

Informações sobre formas de pagamento.

🚀 Tecnologias Utilizadas
Java 17+ (ou a versão que você estiver usando)

Spring Boot (para o servidor web e serviços)

Spring Web (para criar os endpoints REST do webhook)

Spring Scheduler (para a tarefa de limpeza de conversas inativas)

Maven (para gerenciamento de dependências)

Evolution API (como conector não-oficial para o WhatsApp)

Ngrok (para expor a aplicação local para a web durante o desenvolvimento)

⚙️ Como Executar o Projeto
Siga estes passos para configurar e rodar o projeto localmente.

Pré-requisitos
Java (JDK) 17 ou superior instalado.

Maven instalado.

Uma instância da Evolution API deve estar instalada e rodando (ex: via Docker).

O ngrok (ou uma ferramenta similar) para expor sua porta local.

1. Clonar o Repositório
Bash
git clone https://github.com/seu-usuario/seu-repositorio.git
cd seu-repositorio
2. Configurar a Aplicação (Spring Boot)
Abra o arquivo src/main/resources/application.properties.

Adicione ou verifique as seguintes propriedades:

Properties
# Define a porta da aplicação (ex: 8081, para não conflitar com a Evolution)
server.port=8081

# Configurações da Evolution API
# Verifique se a URL e porta da sua instância estão corretas
evolution.api.url=http://localhost:8080
evolution.api.key=SUA_API_KEY_AQUI
evolution.instance.name=NOME_DA_SUA_INSTANCIA
3. Configurar o Webhook (Evolution API)
Inicie sua aplicação Spring Boot: mvn spring-boot:run (ou pela sua IDE).

Em um novo terminal, exponha sua porta local com o ngrok:

Bash
ngrok http 8081
O ngrok gerará uma URL pública (ex: https://xxxx-xxxx.ngrok-free.app).

Abra o arquivo .env da sua Evolution API.

Configure o webhook global para apontar para o seu endpoint do Spring Boot:

Snippet de código
# Ativa o webhook global
WEBHOOK_GLOBAL_ENABLED=true

# Define a URL do seu bot
WEBHOOK_GLOBAL_URL='https://xxxx-xxxx.ngrok-free.app/api/whatsapp/webhook'

# Garanta que o evento de nova mensagem está ativado
WEBHOOK_EVENTS_MESSAGES_UPSERT=true
Reinicie sua instância da Evolution API (ex: docker-compose down && docker-compose up -d) para que ela leia as novas configurações do .env.

4. Testar
Com a Aplicação Spring e a Evolution API rodando, envie uma mensagem "Olá" para o número de WhatsApp conectado à Evolution API. O bot deverá responder com o menu principal!

🗺️ Estrutura do Projeto
br.com.chat.empresa.oticavisual.controller.WebhookController

Contém o endpoint principal /api/whatsapp/webhook.

Recebe todas as notificações de eventos da Evolution API.

Gerencia o estado da conversa (conversationState).

Contém a lógica de verificação de horário (isForaDoHorario).

Possui a tarefa agendada (cleanupInactiveConversations) para o time-out.

br.com.chat.empresa.oticavisual.service.EvolutionService

Classe responsável por formatar e enviar mensagens (respostas) para a Evolution API.

br.com.chat.empresa.oticavisual.dto.EvolutionWebhook

Classe DTO (Data Transfer Object) que mapeia o JSON de entrada do webhook, permitindo o acesso fácil a from (cliente) e body (mensagem).

🔮 Próximos Passos (Roadmap)
[ ] Migração para API Oficial: Substituir a EvolutionService por um TwilioService para migrar para a API oficial do WhatsApp (via Twilio) assim que a verificação da empresa na Meta for concluída.

[ ] Interface de Atendimento Humano: Desenvolver ou integrar uma plataforma de chat (como Chatwoot) para que os atendentes humanos possam assumir as conversas do estado HUMAN_CHAT_ACTIVE.

[ ] Persistência de Dados: Mudar o gerenciamento de estado (conversationState) de um Map em memória para um banco de dados (ex: H2, PostgreSQL) para que o bot não perca o estado das conversas se for reiniciado.
