# Lab 19 — Étape 6: Gateway avec Eureka

With Java 17 and Maven, run `mvn clean package` from this folder. Stop any
previous IntelliJ runs and reload Maven so the Gateway picks up its new dependencies.
Start these commands in separate terminals, in order:

```powershell
java -jar eureka-server/target/eureka-server-1.0-SNAPSHOT.jar
java -jar client/target/client-1.0-SNAPSHOT.jar
java -jar gateway/target/gateway-1.0-SNAPSHOT.jar
```

1. Open http://localhost:8761/ and look for SERVICE-CLIENT and GATEWAY.
2. Open http://localhost:8888/clients and http://localhost:8888/client/1.
   The Gateway resolves SERVICE-CLIENT in Eureka and forwards the original path.
   If you just launched the services, give the Gateway up to a minute to refresh its registry and retry.
3. For the report, capture the Eureka dashboard and the Gateway's client list.

The Gateway uses the MVC equivalent of the handout's WebFlux route bean:
`lb("SERVICE-CLIENT")`. The former static routes are archived in `app.yml`,
which Spring Boot does not load by default. No MySQL or Docker is needed.

With the applications stopped, run
`powershell -ExecutionPolicy Bypass -File ./verify.ps1` after packaging.
The check runs Client on port 18088 to prove the Gateway uses discovery.

## Étape 7: SERVICE-VOITURE

After `mvn clean package`, start Eureka and Client, then start Voiture:

```powershell
java -jar service-voiture/target/service-voiture-1.0-SNAPSHOT.jar
```

Open http://localhost:8089/voitures. Expect three cars with nested client
details: Toyota and Renault belong to Rabab SELIMANI, and Peugeot belongs
to Amal RAMI. Take screenshots of that JSON and SERVICE-VOITURE as UP on
the Eureka dashboard. Both services use separate in-memory H2 databases.

```mermaid
flowchart LR
    V[SERVICE-VOITURE] -->|Resolves SERVICE-CLIENT| E[Eureka]
    V -->|OpenFeign: GET /client/id| C[SERVICE-CLIENT]
    V --> VH[(H2 voitures)]
    C --> CH[(H2 clients)]
```

```mermaid
classDiagram
    VoitureController --> VoitureRepository
    VoitureController --> ClientService
    VoitureRepository --> Voiture
    ClientService --> Client : returns
    Voiture --> Client : transient
```

The stored relationship is `clientId`; Client is a remote POJO, so the
enriched field is `@Transient`. Feign calls the existing singular
`/client/{id}` endpoint. The seed does not depend on remote calls.

## Étapes 8–10: Voiture API and final checks

Rebuild with `mvn clean package` after stopping old runs, then launch
Eureka, Client, Voiture and Gateway in separate terminals.

| Screenshot | URL | Expected result |
|---|---|---|
| Eureka | http://localhost:8761/ | SERVICE-CLIENT, SERVICE-VOITURE and GATEWAY are UP |
| Clients through Gateway | http://localhost:8888/clients | Three clients |
| Cars through Gateway | http://localhost:8888/voitures | Three cars, with their owners |
| Car detail | http://localhost:8089/voitures/1 | Toyota Corolla, owned by Rabab SELIMANI |
| Cars by client | http://localhost:8089/voitures/client/1 | Toyota and Renault |

The Gateway also forwards the detail and owner-filter URLs under port 8888.
To demonstrate writes, send JSON with an HTTP client such as PowerShell:

```powershell
$car = Invoke-RestMethod http://localhost:8888/voitures/2 -Method Post -ContentType 'application/json' -Body '{"marque":"Dacia","matricule":"LAB-19","model":"Logan"}'
Invoke-RestMethod "http://localhost:8888/voitures/$($car.id)" -Method Put -ContentType 'application/json' -Body '{"model":"Sandero"}'
```

POST uses the client ID in the URL. PUT changes only supplied, nonblank
vehicle fields and leaves the owner unchanged. H2 resets when Voiture restarts.

## Screenshots

### Eureka dashboard

[http://localhost:8761/](http://localhost:8761/)

<img width="2557" height="1597" alt="Eureka dashboard" src="https://github.com/user-attachments/assets/ec37d410-51ca-446e-81a6-a6570b15d2f4" />

### Client list

[http://localhost:8088/clients](http://localhost:8088/clients)

<img width="867" height="480" alt="Client list" src="https://github.com/user-attachments/assets/202cda98-2d18-4bed-b695-19b24b680ad1" />

### Client detail

[http://localhost:8088/client/1](http://localhost:8088/client/1)

<img width="450" height="262" alt="Client detail" src="https://github.com/user-attachments/assets/3212aa0d-8e3e-41a3-8383-e16158bcdc23" />

### Client detail through Gateway

[http://localhost:8888/client/1](http://localhost:8888/client/1)

<img width="523" height="267" alt="Client detail through Gateway" src="https://github.com/user-attachments/assets/714ba788-11e0-4551-9a91-98ae02fac984" />

### Client list through Gateway

[http://localhost:8888/clients](http://localhost:8888/clients)

<img width="507" height="422" alt="Client list through Gateway" src="https://github.com/user-attachments/assets/ccdd9905-7cf4-4dd8-bc27-749606660115" />

### Voiture list

[http://localhost:8089/voitures](http://localhost:8089/voitures)

<img width="686" height="838" alt="Voiture list" src="https://github.com/user-attachments/assets/9737efdd-a6a6-4750-8942-6633bf8013e9" />

### Voiture list through Gateway

[http://localhost:8888/voitures](http://localhost:8888/voitures)

<img width="695" height="877" alt="Voiture list through Gateway" src="https://github.com/user-attachments/assets/9955c0b5-5d01-419d-9e53-a401ddad869f" />

### Voiture detail

[http://localhost:8089/voitures/1](http://localhost:8089/voitures/1)

<img width="675" height="378" alt="Voiture detail" src="https://github.com/user-attachments/assets/254d2096-1549-4735-af70-96d24329f4bd" />

### Voitures by client

[http://localhost:8089/voitures/client/1](http://localhost:8089/voitures/client/1)

<img width="727" height="660" alt="Voitures by client" src="https://github.com/user-attachments/assets/0e38ec0e-7833-4e38-a957-cc30bb72ecde" />

### Additional screenshots

<img width="447" height="297" alt="Additional lab screenshot 1" src="https://github.com/user-attachments/assets/d18c71fc-4338-45ca-9779-f073c780f86b" />

<img width="610" height="1038" alt="Additional lab screenshot 2" src="https://github.com/user-attachments/assets/349fd85a-0232-45cf-8f0e-3271569afec8" />

<img width="643" height="937" alt="Additional lab screenshot 3" src="https://github.com/user-attachments/assets/6c639e88-cce8-4293-b7d4-aa9a45b2c3db" />
