package com.dtaquito_backend.dtaquito_backend;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Objects;

@SpringBootApplication
@EnableScheduling
public class DtaquitoBackendApplication {

	public static void main(String[] args) {

		// Cargar las variables de entorno
		Dotenv dotenv = Dotenv.load();

		System.setProperty("spring.datasource.url", Objects.requireNonNull(dotenv.get("DB_URL")));
		System.setProperty("spring.datasource.username", Objects.requireNonNull(dotenv.get("DB_USERNAME")));
		System.setProperty("spring.datasource.password", Objects.requireNonNull(dotenv.get("DB_PASSWORD")));

		// Arrancar la aplicación Spring Boot
		SpringApplication.run(DtaquitoBackendApplication.class, args);
	}
}
