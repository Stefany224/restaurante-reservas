package cl.ipss.restaurante;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Sistema de reservas del restaurante Sabor Gourmet, los clientes reservan mesas en linea y el personal las administra
@SpringBootApplication
public class RestauranteApplication {

    // Punto de partida, levanta el servidor y todo lo que Spring encuentra dentro de cl.ipss.restaurante
    public static void main(String[] args) {
        SpringApplication.run(RestauranteApplication.class, args);
    }
}
