package net.navjotsingh.wabai;

import net.navjotsingh.wabai.jobs.BirthdayCheckJob;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class WhatsappAutoBirthdayAiApplication {

	public static void main(String[] args) {
		SpringApplication.run(WhatsappAutoBirthdayAiApplication.class, args);
	}

	@Bean
	CommandLineRunner runJobDev(BirthdayCheckJob birthdayCheckJob) {
		return args -> {
			System.out.println("---Dev mode---");
			birthdayCheckJob.runJob();
		};
	}

}
