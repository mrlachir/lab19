package ma.fstg.lab19.voiture.controllers;

import java.util.List;
import ma.fstg.lab19.voiture.entities.Voiture;
import ma.fstg.lab19.voiture.services.VoitureService;
import ma.fstg.lab19.voiture.services.VoitureService.VoitureInput;
import org.springframework.web.bind.annotation.*;

@RestController
public class VoitureController {
    private final VoitureService service;

    public VoitureController(VoitureService service) { this.service = service; }

    @GetMapping("/voitures")
    public List<Voiture> findAll() {
        return service.findAll();
    }

    @GetMapping("/voitures/{id}")
    public Voiture findById(@PathVariable Long id) { return service.findById(id); }

    @GetMapping("/voitures/client/{id}")
    public List<Voiture> findByClient(@PathVariable Long id) { return service.findByClient(id); }

    @PostMapping("/voitures/{clientId}")
    public Voiture save(@PathVariable Long clientId, @RequestBody VoitureInput input) {
        return service.enregistrerVoiture(clientId, input);
    }

    @PutMapping("/voitures/{id}")
    public Voiture update(@PathVariable Long id, @RequestBody VoitureInput input) {
        return service.update(id, input);
    }
}
