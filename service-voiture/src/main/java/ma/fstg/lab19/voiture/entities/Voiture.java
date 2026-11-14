package ma.fstg.lab19.voiture.entities;

import jakarta.persistence.*;
import lombok.*;
import ma.fstg.lab19.voiture.clients.Client;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Voiture {
    @Id @GeneratedValue
    private Long id;
    private String marque;
    private String matricule;
    private String model;
    private Long clientId;
    @Transient
    private Client client;
}
