package ma.fstg.lab19.voiture.repositories;

import ma.fstg.lab19.voiture.entities.Voiture;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VoitureRepository extends JpaRepository<Voiture, Long> {
    java.util.List<Voiture> findByClientIdOrderById(Long clientId);
}
