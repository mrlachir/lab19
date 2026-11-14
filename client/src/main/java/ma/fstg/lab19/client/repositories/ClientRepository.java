package ma.fstg.lab19.client.repositories;

import ma.fstg.lab19.client.entities.Client;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientRepository extends JpaRepository<Client, Long> {}
