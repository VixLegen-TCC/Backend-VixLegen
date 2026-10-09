package br.com.VixLegen.ProjetoVixLegen10;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@org.springframework.scheduling.annotation.EnableScheduling
public class Application {

    @org.springframework.context.annotation.Bean
    public java.time.Clock relogioPrazos() {
        return java.time.Clock.system(java.time.ZoneId.of("America/Sao_Paulo"));
    }


	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}
