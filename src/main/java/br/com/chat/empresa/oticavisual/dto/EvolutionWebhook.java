package br.com.chat.empresa.oticavisual.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EvolutionWebhook {

    @JsonProperty("event")
    public String event;

    @JsonProperty("instance")
    public String instance;

    @JsonProperty("data")
    public Data data;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Data {
        @JsonProperty("key")
        public Key key;
        @JsonProperty("message")
        public Message message;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Key {
        @JsonProperty("remoteJid")
        public String remoteJid;

        @JsonProperty("fromMe")
        public boolean fromMe;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Message {
        @JsonProperty("conversation")
        public String conversation;
    }
}