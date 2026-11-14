package ma.fstg.lab19.client.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Client {
    @Id @GeneratedValue
    private Long id;
    private String nom;
    private Float age;
}
