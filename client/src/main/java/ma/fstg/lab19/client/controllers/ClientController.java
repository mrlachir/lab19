package ma.fstg.lab19.client.controllers;

import java.util.List;
import ma.fstg.lab19.client.entities.Client;
import ma.fstg.lab19.client.services.ClientService;
import org.springframework.web.bind.annotation.*;

@RestController
public class ClientController {
    private final ClientService service;

    public ClientController(ClientService service) { this.service = service; }

    @GetMapping("/clients")
    public List<Client> findAll() { return service.findAll(); }

    @GetMapping("/client/{id}")
    public Client findById(@PathVariable Long id) { return service.findById(id); }
}
