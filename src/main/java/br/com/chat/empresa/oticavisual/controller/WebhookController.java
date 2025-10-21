package br.com.chat.empresa.oticavisual.controller;

import br.com.chat.empresa.oticavisual.dto.EvolutionWebhook;
import br.com.chat.empresa.oticavisual.service.EvolutionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.springframework.scheduling.annotation.Scheduled;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/whatsapp")
public class WebhookController {

    private final EvolutionService evolutionService;


    private final Map<String, Conversation> conversationState = new ConcurrentHashMap<>();

    private static class Conversation {
        String state;
        long lastInteractionTimestamp; // Guarda o tempo em milissegundos

        Conversation(String state) {
            this.state = state;
            this.lastInteractionTimestamp = Instant.now().toEpochMilli();
        }

        void updateTimestamp() {
            this.lastInteractionTimestamp = Instant.now().toEpochMilli();
        }
    }

    @Autowired
    public WebhookController(EvolutionService evolutionService) {
        this.evolutionService = evolutionService;
    }

    // --- NOVO MÉTODO PARA VERIFICAR O HORÁRIO ---
    /**
     * Verifica se o momento atual está fora do horário comercial.
     * Horários definidos:
     * - Seg a Sex: 08:00 às 18:00
     * - Sábado: 08:00 às 13:00
     * - Domingo: Fechado
     */

    private boolean isForaDoHorario() {
        try {
            LocalDateTime agora = LocalDateTime.now();
            DayOfWeek diaDaSemana = agora.getDayOfWeek();
            LocalTime horaAtual = agora.toLocalTime();

            if (diaDaSemana == DayOfWeek.SUNDAY) {
                return true;
            }

            if (diaDaSemana == DayOfWeek.SATURDAY) {
                LocalTime inicioSabado = LocalTime.of(8, 0);
                LocalTime fimSabado = LocalTime.of(13, 0);
                return horaAtual.isBefore(inicioSabado) || horaAtual.isAfter(fimSabado);
            }

            LocalTime inicioSemana = LocalTime.of(8, 0);
            LocalTime fimSemana = LocalTime.of(22, 0);
            return horaAtual.isBefore(inicioSemana) || horaAtual.isAfter(fimSemana);

        } catch (Exception e) {
            System.err.println("Erro ao verificar horário: " + e.getMessage());
            return false;
        }
    }


    @PostMapping(value = "/webhook", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void handleEvolutionWebhook(@RequestBody EvolutionWebhook payload) {

        if (payload == null || payload.event == null || !"messages.upsert".equals(payload.event) ||
                payload.data == null || payload.data.key == null || payload.data.message == null ||
                payload.data.message.conversation == null || payload.data.key.fromMe) {

            System.out.println("Ignorando evento...");
            return;
        }

        String from = payload.data.key.remoteJid;
        String body = payload.data.message.conversation;
        String messageLower = body.toLowerCase().trim();

        System.out.println("Mensagem recebida de " + from + ": " + body);

        Conversation currentConversation = conversationState.get(from);

        if (currentConversation != null) {
            currentConversation.updateTimestamp();
        }

        if (currentConversation != null && "HUMAN_CHAT_ACTIVE".equals(currentConversation.state)) {
            System.out.println("Bot em silêncio para " + from + ". Atendimento humano ativo.");

            if (messageLower.equals("/encerrar")) {
                conversationState.remove(from);
                evolutionService.sendTextMessage(from, "Atendimento finalizado. Obrigado! Se precisar de algo mais, mande um 'olá'.");
            }
            return;
        }

        if (currentConversation != null) {
            switch (currentConversation.state) {
                case "AWAITING_NAME":
                    String responseWithName = "Obrigado, " + body + ".\n" +
                            "Sua solicitação de agendamento foi registrada. Em alguns minutos, um de nossos atendentes retornará sua mensagem para confirmar a melhor data!";
                    evolutionService.sendTextMessage(from, responseWithName);
                    conversationState.put(from, new Conversation("HUMAN_CHAT_ACTIVE"));
                    return;

                case "AWAITING_CPF_EXAME":
                    String responseWithCpf = "Recebemos seu CPF. Localizei seu cadastro!\n" +
                            "Por questões de segurança, um de nossos atendentes já foi notificado e irá enviar seu resultado ou o status do seu pedido aqui no WhatsApp em breve.";
                    evolutionService.sendTextMessage(from, responseWithCpf);
                    conversationState.put(from, new Conversation("HUMAN_CHAT_ACTIVE"));
                    return;
            }
        }


        if (messageLower.equals("oi") || messageLower.equals("olá") || messageLower.equals("bom dia")  || messageLower.equals("ola")  || messageLower.equals("oii")  || messageLower.equals("boa tarde")) {


            if (isForaDoHorario()) {
                String msgAusencia = "Olá! No momento, estamos fora do horário de atendimento, que é de Seg. a Sex. (8h às 18h) e Sáb. (8h às 13h). Responderemos assim que possível!";
                evolutionService.sendTextMessage(from, msgAusencia);
                return;
            }


            String menu = "Olá! Seja bem-vindo(a) à *Ótica Visual*! Sou o Oticus, seu assistente virtual. 😊\n" +
                    "Agradecemos o seu contato. Como podemos te ajudar hoje?\n\n" +
                    "Digite o *número* da opção desejada:\n\n" +
                    "*1.* Agendar exame de vista\n" +
                    "*2.* Consultar exame ou status do pedido\n" +
                    "*3.* Preços de lentes e produtos\n" +
                    "*4.* Nosso horário de funcionamento\n" +
                    "*5.* Formas de pagamento\n" +
                    "*6.* Falar com um atendente";
            evolutionService.sendTextMessage(from, menu);


            // 1. Agendar Exame
        } else if (messageLower.equals("1")) {
            evolutionService.sendTextMessage(from, "Entendido, vamos iniciar seu agendamento. Para começar, por favor, digite seu *nome completo*:");
            conversationState.put(from, new Conversation("AWAITING_NAME")); // Define o estado de espera

            // 2. Consultar Exame ou Pedido
        } else if (messageLower.equals("2")) {
            evolutionService.sendTextMessage(from, "Certo. Para consultar o resultado do seu exame ou o status do seu pedido, por favor, digite o seu *CPF (apenas números)*:");
            conversationState.put(from, new Conversation("AWAITING_CPF_EXAME")); // Define o estado de espera

            // 3. Preços
        } else if (messageLower.equals("3")) {
            evolutionService.sendTextMessage(from, "Sobre qual tipo de lente ou produto você gostaria de saber o preço? (Ex: lentes para miopia, lentes multifocais, armações, etc.)");

            // 4. Horário
        } else if (messageLower.equals("4")) {
            evolutionService.sendTextMessage(from, "Nosso horário de funcionamento é de *segunda a sexta, das 8h às 18h*, e aos *sábados, das 8h às 13h*.");

            // 5. Pagamento
        } else if (messageLower.equals("5")) {
            evolutionService.sendTextMessage(from, "Você pode fazer o pagamento via *Pix, cartão de débito ou crédito*. Parcelamos em até 10x sem juros!");

            // 6. Falar com Atendente
        } else if (messageLower.equals("6")) {
            String response = "Ok! Em alguns minutos, um de nossos atendentes retornará sua mensagem aqui mesmo. Por favor, aguarde.";
            evolutionService.sendTextMessage(from, response);
            conversationState.put(from, new Conversation("HUMAN_CHAT_ACTIVE"));
        }

        // Respostas a palavras-chave
        else if (messageLower.contains("multifocais") || messageLower.contains("miopia") || messageLower.contains("armações")) {
            evolutionService.sendTextMessage(from, "Os preços de lentes e armações variam bastante... Se desejar, digite *6* para falar com um atendente.");

        } else {
            String fallback = "Desculpe, não entendi. Digite *'olá'* para ver o menu principal de opções.";
            evolutionService.sendTextMessage(from, fallback);
        }
    }

    @Scheduled(fixedRate = 10, timeUnit = TimeUnit.MINUTES) // Roda a cada 30 minutos
    public void cleanupInactiveConversations() {
        System.out.println("Executando tarefa de limpeza de conversas inativas...");

        long now = Instant.now().toEpochMilli();
        //long timeoutInMillis = TimeUnit.MINUTES.toMillis(15);
        long threeHoursInMillis = TimeUnit.HOURS.toMillis(3); // Alterado para 3 horas

        conversationState.entrySet().removeIf(entry -> {
            Conversation conversation = entry.getValue();
            if ("HUMAN_CHAT_ACTIVE".equals(conversation.state)) {
                long timeSinceLastInteraction = now - conversation.lastInteractionTimestamp;

                if (timeSinceLastInteraction > threeHoursInMillis) {
                    System.out.println("Encerrando conversa inativa por mais de 3 horas com: " + entry.getKey());
                    evolutionService.sendTextMessage(entry.getKey(), "Nosso atendimento foi encerrado por inatividade. Caso precise de algo mais, basta mandar um 'olá' para ver o menu. 👍");
                    return true;
                }
            }
            return false;
        });
    }
}