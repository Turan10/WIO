package app.wio;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class WioApplication {

	public static void main(String[] args) {
		SpringApplication.run(WioApplication.class, args);
	}
}
