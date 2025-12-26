package io.vobc.vobc_back;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class VobcBackApplication {

    public static void main(String[] args) {
        SpringApplication.run(VobcBackApplication.class, args);
    }
    @PostConstruct
    public void checkWallet() {
        System.out.println("ENV TNS_ADMIN=" + System.getenv("TNS_ADMIN"));
        System.out.println("PROP oracle.net.tns_admin=" + System.getProperty("oracle.net.tns_admin"));
    }


}
