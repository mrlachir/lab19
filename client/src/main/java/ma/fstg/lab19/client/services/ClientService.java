package ma.fstg.lab19.client.services;

import java.util.List;
import ma.fstg.lab19.client.entities.Client;
import ma.fstg.lab19.client.repositories.ClientRepository;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ClientService {
    private final ClientRepository repository;

    public ClientService(ClientRepository repository) { this.repository = repository; }

    public List<Client> findAll() { return repository.findAll(Sort.by("id")); }

    public Client findById(Long id) {
        return repository.findById(id).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Client non trouvé"));
    }
}
