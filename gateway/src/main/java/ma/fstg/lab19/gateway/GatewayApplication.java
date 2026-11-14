package ma.fstg.lab19.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import static org.springframework.cloud.gateway.server.mvc.filter.LoadBalancerFilterFunctions.lb;
import static org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions.route;
import static org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions.http;
import static org.springframework.web.servlet.function.RequestPredicates.path;

@SpringBootApplication
public class GatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }

    @Bean
    RouterFunction<ServerResponse> clientRoutes() {
        return route("service-client")
            .route(path("/clients").or(path("/clients/**")).or(path("/client/*")), http())
            .filter(lb("SERVICE-CLIENT"))
            .build();
    }

    @Bean
    RouterFunction<ServerResponse> voitureRoutes() {
        return route("service-voiture")
            .route(path("/voitures").or(path("/voitures/**")), http())
            .filter(lb("SERVICE-VOITURE"))
            .build();
    }
}
