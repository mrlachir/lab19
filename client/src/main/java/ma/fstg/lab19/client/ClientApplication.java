package ma.fstg.lab19.client;

import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableDiscoveryClient
public class ClientApplication {
    public static void main(String[] args) {
        SpringApplication.run(ClientApplication.class, args);
    }



    @org.springframework.context.annotation.Bean
    org.springframework.boot.CommandLineRunner initialiserBaseH2(
            ma.fstg.lab19.client.repositories.ClientRepository repository) {
        return args -> repository.saveAll(java.util.List.of(
            new ma.fstg.lab19.client.entities.Client(null, "Rabab SELIMANI", 23f),
            new ma.fstg.lab19.client.entities.Client(null, "Amal RAMI", 22f),
            new ma.fstg.lab19.client.entities.Client(null, "Samir SAFI", 22f)));
    }
}
