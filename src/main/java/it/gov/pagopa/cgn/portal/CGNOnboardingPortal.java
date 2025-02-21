package it.gov.pagopa.cgn.portal;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

import java.util.Arrays;

@SpringBootApplication
public class CGNOnboardingPortal {

    public static void main(String[] args) {
        SpringApplication.run(CGNOnboardingPortal.class, args);
    }

    @Bean
    @Profile("dev")
    public CommandLineRunner printBeans(ApplicationContext ctx) {
        return args -> {
            System.out.println("Registered beans:");
            Arrays.stream(ctx.getBeanDefinitionNames()).sorted().forEach(System.out::println);
        };
    }
}