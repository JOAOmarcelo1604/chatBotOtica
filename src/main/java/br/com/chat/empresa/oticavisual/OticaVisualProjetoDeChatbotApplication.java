package br.com.chat.empresa.oticavisual;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class OticaVisualProjetoDeChatbotApplication {

	public static void main(String[] args) {
		SpringApplication.run(OticaVisualProjetoDeChatbotApplication.class, args);
	}

}
