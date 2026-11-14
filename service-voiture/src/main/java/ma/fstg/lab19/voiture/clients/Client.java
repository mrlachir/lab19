package ma.fstg.lab19.voiture.clients;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Client {
    private Long id;
    private String nom;
    private Float age;
}
