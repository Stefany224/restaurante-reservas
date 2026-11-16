package cl.ipss.restaurante.controller;

import cl.ipss.restaurante.dto.ReservaForm;
import cl.ipss.restaurante.dto.ReservaResponse;
import cl.ipss.restaurante.exception.ReglaNegocioException;
import cl.ipss.restaurante.service.MesaService;
import cl.ipss.restaurante.service.ReservaService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;

// ReservaController maneja todo lo del cliente, buscar mesas, reservar, ver sus reservas, modificarlas y cancelarlas
@Controller
@RequestMapping("/reservas")
public class ReservaController {

    // Formato de fecha para los mensajes de confirmacion
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    // Datos del cliente que al modificar una reserva vienen bloqueados y no se vuelven a validar
    private static final Set<String> CAMPOS_CLIENTE = Set.of("nombre", "email", "telefono");

    // Services que usa el controller
    private final ReservaService reservaService;
    private final MesaService mesaService;

    // Spring entrega los services por el constructor
    public ReservaController(ReservaService reservaService, MesaService mesaService) {
        this.reservaService = reservaService;
        this.mesaService = mesaService;
    }

    // Antes de validar se quitan los espacios del inicio y del final de todos los textos
    @InitBinder
    public void recortarTextos(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(false));
    }

    // Buscador de mesas libres, si trae reservaId y email es porque el cliente esta modificando una reserva
    @GetMapping("/disponibilidad")
    public String disponibilidad(
            @RequestParam(name = "fecha", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam(name = "hora", required = false) @DateTimeFormat(pattern = "HH:mm") LocalTime hora,
            @RequestParam(name = "personas", required = false) Integer personas,
            @RequestParam(name = "reservaId", required = false) Long reservaId,
            @RequestParam(name = "email", required = false) String email,
            Model model) {
        boolean modificando = reservaId != null && email != null && !email.isBlank();
        model.addAttribute("horarios", reservaService.horarios());
        model.addAttribute("hoy", LocalDate.now());
        model.addAttribute("fecha", fecha);
        model.addAttribute("hora", hora);
        model.addAttribute("personas", personas == null ? 2 : personas);
        model.addAttribute("modificando", modificando);
        model.addAttribute("reservaId", modificando ? reservaId : null);
        model.addAttribute("email", modificando ? email : null);

        // solo buscamos cuando vienen los tres datos, la primera vez la pagina sale vacia
        if (fecha != null && hora != null && personas != null) {
            try {
                model.addAttribute("mesas",
                        reservaService.mesasDisponibles(fecha, hora, personas, modificando ? reservaId : null));
            } catch (ReglaNegocioException ex) {
                model.addAttribute("error", ex.getMessage());
            }
        }
        return "reservas/disponibilidad";
    }

    // Formulario para confirmar la reserva con la mesa que el cliente eligio en el buscador
    @GetMapping("/nueva")
    public String nueva(
            @RequestParam(name = "mesaId", required = false) Long mesaId,
            @RequestParam(name = "fecha", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam(name = "hora", required = false) @DateTimeFormat(pattern = "HH:mm") LocalTime hora,
            @RequestParam(name = "personas", required = false) Integer personas,
            Model model) {
        // si falta algun dato es porque no vino del buscador, asi que lo devolvemos ahi
        if (mesaId == null || fecha == null || hora == null || personas == null) {
            return "redirect:/reservas/disponibilidad";
        }
        ReservaForm form = new ReservaForm();
        form.setMesaId(mesaId);
        form.setFecha(fecha);
        form.setHora(hora);
        form.setPersonas(personas);
        return mostrarFormulario(model, form, null);
    }

    // Guarda la reserva nueva y redirige a mis reservas para que al recargar no se repita
    @PostMapping
    public String crear(@Valid @ModelAttribute("reserva") ReservaForm form, BindingResult resultado,
                        Model model, RedirectAttributes redirect) {
        if (resultado.hasErrors()) {
            return mostrarFormulario(model, form, null);
        }
        try {
            ReservaResponse r = reservaService.crear(form);
            redirect.addFlashAttribute("exito", "¡Reserva confirmada! " + resumen(r));
            redirect.addAttribute("email", r.emailCliente());
            return "redirect:/reservas/mis-reservas";
        } catch (ReglaNegocioException ex) {
            model.addAttribute("error", ex.getMessage());
            return mostrarFormulario(model, form, null);
        }
    }

    // El cliente ve sus reservas buscandolas con su correo
    @GetMapping("/mis-reservas")
    public String misReservas(@RequestParam(name = "email", required = false) String email, Model model) {
        model.addAttribute("email", email);
        if (email != null && !email.isBlank()) {
            model.addAttribute("reservas", reservaService.listarPorCliente(email));
        }
        return "reservas/mis-reservas";
    }

    // Formulario para modificar una reserva, si viene del buscador trae la nueva mesa y turno
    @GetMapping("/{id}/editar")
    public String editar(@PathVariable("id") Long id, @RequestParam(name = "email") String email,
                         @RequestParam(name = "mesaId", required = false) Long mesaId,
                         @RequestParam(name = "fecha", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
                         @RequestParam(name = "hora", required = false) @DateTimeFormat(pattern = "HH:mm") LocalTime hora,
                         @RequestParam(name = "personas", required = false) Integer personas,
                         Model model, RedirectAttributes redirect) {
        try {
            ReservaForm form = reservaService.formularioEdicion(id, email);
            if (mesaId != null && fecha != null && hora != null && personas != null) {
                form.setMesaId(mesaId);
                form.setFecha(fecha);
                form.setHora(hora);
                form.setPersonas(personas);
            }
            return mostrarFormulario(model, form, id);
        } catch (ReglaNegocioException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
            redirect.addAttribute("email", email);
            return "redirect:/reservas/mis-reservas";
        }
    }

    // Guarda los cambios de la reserva
    @PostMapping("/{id}/editar")
    public String actualizar(@PathVariable("id") Long id,
                             @Valid @ModelAttribute("reserva") ReservaForm form, BindingResult resultado,
                             Model model, RedirectAttributes redirect) {
        // los errores de los datos del cliente no cuentan porque esos campos no se pueden cambiar aqui
        boolean hayErroresDeReserva = resultado.getFieldErrors().stream()
                .anyMatch(error -> !CAMPOS_CLIENTE.contains(error.getField()));
        if (hayErroresDeReserva) {
            return mostrarFormulario(model, form, id);
        }
        try {
            ReservaResponse r = reservaService.actualizar(id, form.getEmail(), form);
            redirect.addFlashAttribute("exito", "Reserva modificada. " + resumen(r));
            redirect.addAttribute("email", r.emailCliente());
            return "redirect:/reservas/mis-reservas";
        } catch (ReglaNegocioException ex) {
            model.addAttribute("error", ex.getMessage());
            return mostrarFormulario(model, form, id);
        }
    }

    // El cliente cancela su reserva y la mesa queda libre
    @PostMapping("/{id}/cancelar")
    public String cancelar(@PathVariable("id") Long id, @RequestParam(name = "email") String email,
                           RedirectAttributes redirect) {
        try {
            reservaService.cancelar(id, email);
            redirect.addFlashAttribute("exito", "La reserva " + id + " fue cancelada.");
        } catch (ReglaNegocioException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        redirect.addAttribute("email", email);
        return "redirect:/reservas/mis-reservas";
    }

    // Carga lo que necesita la vista del formulario, la usan crear y modificar
    private String mostrarFormulario(Model model, ReservaForm form, Long reservaId) {
        model.addAttribute("reserva", form);
        model.addAttribute("reservaId", reservaId);
        model.addAttribute("editando", reservaId != null);
        model.addAttribute("mesa", form.getMesaId() == null ? null : mesaService.obtener(form.getMesaId()));
        return "reservas/formulario";
    }

    // Arma el texto con el resumen de la reserva para el mensaje de exito
    private String resumen(ReservaResponse r) {
        return "Mesa " + r.numeroMesa() + " el " + r.fecha().format(FORMATO_FECHA) + " a las " + r.hora()
                + " para " + r.personas() + (r.personas() == 1 ? " persona." : " personas.");
    }
}
