package br.com.chat.empresa.oticavisual.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
public class EvolutionService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${evolution.api.url}")
    private String evolutionApiUrl;

    @Value("${evolution.api.key}")
    private String apiKey;

    @Value("${evolution.instance.name}")
    private String instanceName;

    public void sendTextMessage(String to, String message) {

        String url = evolutionApiUrl + "/message/sendText/" + instanceName;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("apikey", apiKey);


        Map<String, Object> body = new HashMap<>();
        body.put("number", to);
        body.put("text", message); // <-- Esta é a correção


        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            String response = restTemplate.postForObject(url, entity, String.class);
            System.out.println("Resposta da Evolution API: " + response);
        } catch (Exception e) {
            System.err.println("Erro ao enviar mensagem via Evolution API: " + e.getMessage());
        }
    }
}