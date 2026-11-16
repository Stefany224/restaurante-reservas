package cl.ipss.restaurante.controller.admin;

import cl.ipss.restaurante.dto.ReservaResponse;
import cl.ipss.restaurante.enums.FiltroReserva;
import cl.ipss.restaurante.exception.ReglaNegocioException;
import cl.ipss.restaurante.service.ReservaService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

// ReservaAdminController le muestra al personal todas las reservas y le deja cancelarlas
@Controller
@RequestMapping("/admin/reservas")
public class ReservaAdminController {

    // Service de reservas
    private final ReservaService reservaService;

    // Spring entrega el service por el constructor
    public ReservaAdminController(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

    // Lista las reservas, se puede filtrar por dia y por tipo y si no viene nada muestra todas
    @GetMapping
    public String listar(
            @RequestParam(name = "fecha", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam(name = "filtro", required = false) FiltroReserva filtro,
            Model model) {
        List<ReservaResponse> reservas = reservaService.listar(fecha, filtro);
        model.addAttribute("fecha", fecha);
        model.addAttribute("filtro", filtro);
        model.addAttribute("filtros", FiltroReserva.values());
        model.addAttribute("reservas", reservas);
        // contamos solo las que todavia se pueden atender
        model.addAttribute("activas", reservas.stream().filter(ReservaResponse::modificable).count());
        return "admin/reservas";
    }

    // Cancela una reserva y vuelve al listado manteniendo los filtros que habia
    @PostMapping("/{id}/cancelar")
    public String cancelar(@PathVariable("id") Long id,
                           @RequestParam(name = "fecha", required = false) String fecha,
                           @RequestParam(name = "filtro", required = false) String filtro,
                           RedirectAttributes redirect) {
        try {
            reservaService.cancelarComoAdmin(id);
            redirect.addFlashAttribute("exito", "La reserva " + id + " fue cancelada.");
        } catch (ReglaNegocioException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        if (fecha != null && !fecha.isBlank()) {
            redirect.addAttribute("fecha", fecha);
        }
        if (filtro != null && !filtro.isBlank()) {
            redirect.addAttribute("filtro", filtro);
        }
        return "redirect:/admin/reservas";
    }
}
