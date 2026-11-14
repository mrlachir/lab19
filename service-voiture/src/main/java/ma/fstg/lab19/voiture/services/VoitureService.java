package ma.fstg.lab19.voiture.services;

import feign.FeignException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ma.fstg.lab19.voiture.clients.Client;
import ma.fstg.lab19.voiture.clients.ClientService;
import ma.fstg.lab19.voiture.entities.Voiture;
import ma.fstg.lab19.voiture.repositories.VoitureRepository;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class VoitureService {
    public record VoitureInput(String marque, String matricule, String model) {}

    private final VoitureRepository repository;
    private final ClientService clients;

    public VoitureService(VoitureRepository repository, ClientService clients) {
        this.repository = repository;
        this.clients = clients;
    }

    public List<Voiture> findAll() {
        List<Voiture> voitures = repository.findAll(Sort.by("id"));
        Map<Long, Client> owners = new HashMap<>();
        for (Voiture voiture : voitures) {
            voiture.setClient(owners.computeIfAbsent(voiture.getClientId(), this::owner));
        }
        return voitures;
    }

    public Voiture findById(Long id) {
        Voiture voiture = stored(id);
        voiture.setClient(owner(voiture.getClientId()));
        return voiture;
    }

    public List<Voiture> findByClient(Long id) {
        Client client = owner(id);
        List<Voiture> voitures = repository.findByClientIdOrderById(id);
        voitures.forEach(voiture -> voiture.setClient(client));
        return voitures;
    }

    public Voiture enregistrerVoiture(Long clientId, VoitureInput input) {
        if (!present(input.marque()) || !present(input.matricule()) || !present(input.model())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Marque, matricule et model sont obligatoires");
        }
        Client client = owner(clientId);
        return repository.save(new Voiture(null, input.marque().trim(), input.matricule().trim(),
            input.model().trim(), clientId, client));
    }

    public Voiture update(Long id, VoitureInput input) {
        Voiture voiture = findById(id);
        if (present(input.marque())) voiture.setMarque(input.marque().trim());
        if (present(input.matricule())) voiture.setMatricule(input.matricule().trim());
        if (present(input.model())) voiture.setModel(input.model().trim());
        Voiture saved = repository.save(voiture);
        saved.setClient(voiture.getClient());
        return saved;
    }

    private Voiture stored(Long id) {
        return repository.findById(id).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Voiture introuvable"));
    }

    private Client owner(Long id) {
        try {
            return clients.clientById(id);
        } catch (FeignException exception) {
            HttpStatus status = switch (exception.status()) {
                case 404 -> HttpStatus.NOT_FOUND;
                case 503 -> HttpStatus.SERVICE_UNAVAILABLE;
                default -> exception.status() < 0 ? HttpStatus.SERVICE_UNAVAILABLE : HttpStatus.BAD_GATEWAY;
            };
            throw new ResponseStatusException(status, "Impossible de récupérer le client auprès de SERVICE-CLIENT", exception);
        }
    }

    private static boolean present(String value) { return value != null && !value.isBlank(); }
}
