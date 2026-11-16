package cl.ipss.restaurante.controller;

import cl.ipss.restaurante.service.ReservaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;

// HomeController muestra el inicio, el login y el atajo al panel del personal
@Controller
public class HomeController {

    // Service del que sacamos los horarios para el buscador del inicio
    private final ReservaService reservaService;

    // Spring entrega el service por el constructor
    public HomeController(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

    // Pagina de inicio con el buscador rapido de mesas
    @GetMapping("/")
    public String inicio(Model model) {
        model.addAttribute("horarios", reservaService.horarios());
        model.addAttribute("hoy", LocalDate.now());
        return "index";
    }

    // Formulario de login, el POST lo procesa Spring Security
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    // Si el personal entra a /admin lo mandamos directo a las reservas
    @GetMapping("/admin")
    public String admin() {
        return "redirect:/admin/reservas";
    }
}
