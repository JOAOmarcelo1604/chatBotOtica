package br.com.chat.empresa.oticavisual.service;
import br.com.chat.empresa.oticavisual.configuration.TwilioConfig;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

import java.util.List;

@Service
public class TwilioService {

    private final TwilioConfig twilioConfig;

    @Autowired
    public TwilioService(TwilioConfig twilioConfig) {
        this.twilioConfig = twilioConfig;
    }

    @PostConstruct
    public void init() {
        Twilio.init(twilioConfig.getAccountSid(), twilioConfig.getAuthToken());
    }

    // Método antigo, podemos manter para enviar mensagens simples
    public void sendResponseMessage(String to, String message) {
        Message.creator(
                new PhoneNumber(to),
                new PhoneNumber(twilioConfig.getWhatsappNumber()),
                message
        ).create();
        System.out.println("Resposta simples enviada para " + to + ": " + message);
    }

    // NOVO MÉTODO: Enviar mensagem com botões
    public void sendInteractiveMessage(String to, String body, List<String> buttonLabels) {
        // A API espera que cada botão seja uma string no formato "reply:[label]"
        List<String> actions = buttonLabels.stream()
                .map(label -> "reply:" + label)
                .toList();

        Message.creator(
                new PhoneNumber(to),
                new PhoneNumber(twilioConfig.getWhatsappNumber()),
                body
        ).setPersistentAction(actions).create(); // Aqui está a mágica!

        System.out.println("Mensagem interativa enviada para " + to);
    }
}