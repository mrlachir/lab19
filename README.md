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
