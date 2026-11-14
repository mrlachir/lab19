package ma.fstg.lab19.voiture;

import java.util.List;
import ma.fstg.lab19.voiture.entities.Voiture;
import ma.fstg.lab19.voiture.repositories.VoitureRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableFeignClients
public class VoitureApplication {
    public static void main(String[] args) {
        SpringApplication.run(VoitureApplication.class, args);
    }

    @Bean
    CommandLineRunner initialiserBaseH2(VoitureRepository repository) {
        return args -> repository.saveAll(List.of(
            new Voiture(null, "Toyota", "A 25 333", "Corolla", 1L, null),
            new Voiture(null, "Renault", "B 6 3456", "Megane", 1L, null),
            new Voiture(null, "Peugeot", "A 55 4444", "301", 2L, null)));
    }
}
