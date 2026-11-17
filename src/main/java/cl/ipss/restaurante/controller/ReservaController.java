package cl.ipss.restaurante.controller;

import cl.ipss.restaurante.dto.MesaResponse;
import cl.ipss.restaurante.dto.ReservaForm;
import cl.ipss.restaurante.dto.ReservaResponse;
import cl.ipss.restaurante.exception.ReglaNegocioException;
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
import java.util.List;
import java.util.Set;

// ReservaController maneja todo lo del cliente, buscar mesas, reservar, ver sus reservas, modificarlas y cancelarlas
@Controller
@RequestMapping("/reservas")
public class ReservaController {

    // Formato de fecha para los mensajes de confirmacion
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    // Datos del cliente que al modificar una reserva vienen bloqueados y no se vuelven a validar
    private static final Set<String> CAMPOS_CLIENTE = Set.of("nombre", "email", "telefono");

    // Service que usa el controller
    private final ReservaService reservaService;

    // Spring entrega el service por el constructor
    public ReservaController(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

    // Antes de validar se quitan los espacios del inicio y del final de todos los textos
    @InitBinder
    public void recortarTextos(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(false));
    }

    // Buscador de mesas libres para una reserva nueva
    @GetMapping("/disponibilidad")
    public String disponibilidad(
            @RequestParam(name = "fecha", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam(name = "hora", required = false) @DateTimeFormat(pattern = "HH:mm") LocalTime hora,
            @RequestParam(name = "personas", required = false) Integer personas,
            Model model) {
        model.addAttribute("horarios", reservaService.horarios());
        model.addAttribute("hoy", LocalDate.now());
        model.addAttribute("fecha", fecha);
        model.addAttribute("hora", hora);
        model.addAttribute("personas", personas == null ? 2 : personas);

        // solo buscamos cuando vienen los tres datos, la primera vez la pagina sale vacia
        if (fecha != null && hora != null && personas != null) {
            try {
                model.addAttribute("mesas",
                        reservaService.mesasDisponibles(fecha, hora, personas, null));
            } catch (ReglaNegocioException ex) {
                model.addAttribute("error", ex.getMessage());
            }
        }
        return "reservas/disponibilidad";
    }

    // Formulario para confirmar la reserva, parte con lo elegido en el buscador y si el cliente cambia el dia,
    // la hora o las personas la pagina se recarga con esos valores sin perder lo que ya escribio
    @GetMapping("/nueva")
    public String nueva(
            @RequestParam(name = "mesaId", required = false) Long mesaId,
            @RequestParam(name = "fecha", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam(name = "hora", required = false) @DateTimeFormat(pattern = "HH:mm") LocalTime hora,
            @RequestParam(name = "personas", required = false) Integer personas,
            @RequestParam(name = "nombre", required = false) String nombre,
            @RequestParam(name = "email", required = false) String email,
            @RequestParam(name = "telefono", required = false) String telefono,
            @RequestParam(name = "comentario", required = false) String comentario,
            Model model) {
        // si falta el turno es porque no vino del buscador, asi que lo devolvemos ahi
        if (fecha == null || hora == null || personas == null) {
            return "redirect:/reservas/disponibilidad";
        }
        ReservaForm form = new ReservaForm();
        form.setMesaId(mesaId);
        form.setFecha(fecha);
        form.setHora(hora);
        form.setPersonas(personas);
        form.setNombre(nombre);
        form.setEmail(email);
        form.setTelefono(telefono);
        form.setComentario(comentario);
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

    // Formulario para modificar una reserva, cuando el cliente cambia el dia, la hora o las personas
    // la pagina se recarga con esos valores para mostrar las mesas libres
    @GetMapping("/{id}/editar")
    public String editar(@PathVariable("id") Long id, @RequestParam(name = "email") String email,
                         @RequestParam(name = "mesaId", required = false) Long mesaId,
                         @RequestParam(name = "fecha", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
                         @RequestParam(name = "hora", required = false) @DateTimeFormat(pattern = "HH:mm") LocalTime hora,
                         @RequestParam(name = "personas", required = false) Integer personas,
                         @RequestParam(name = "comentario", required = false) String comentario,
                         Model model, RedirectAttributes redirect) {
        try {
            ReservaForm form = reservaService.formularioEdicion(id, email);
            if (fecha != null) form.setFecha(fecha);
            if (hora != null) form.setHora(hora);
            if (personas != null) form.setPersonas(personas);
            if (mesaId != null) form.setMesaId(mesaId);
            if (comentario != null) form.setComentario(comentario);
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
        cargarOpciones(model, form, reservaId);
        model.addAttribute("reserva", form);
        model.addAttribute("reservaId", reservaId);
        model.addAttribute("editando", reservaId != null);
        return "reservas/formulario";
    }

    // Horarios y solo las mesas libres para el dia, hora y personas elegidos,
    // al modificar la mesa de la propia reserva cuenta como libre
    private void cargarOpciones(Model model, ReservaForm form, Long reservaId) {
        model.addAttribute("horarios", reservaService.horarios());
        model.addAttribute("hoy", LocalDate.now());
        List<MesaResponse> mesas = List.of();
        if (form.getFecha() != null && form.getHora() != null && form.getPersonas() != null) {
            try {
                mesas = reservaService.mesasDisponibles(form.getFecha(), form.getHora(), form.getPersonas(), reservaId);
            } catch (ReglaNegocioException ex) {
                // si ya hay un error de guardar no lo tapamos
                if (!model.containsAttribute("error")) {
                    model.addAttribute("error", ex.getMessage());
                }
            }
        }
        // si la mesa elegida ya no sirve para el turno nuevo el cliente tiene que elegir otra
        Long mesaId = form.getMesaId();
        if (mesaId != null && mesas.stream().noneMatch(m -> m.id().equals(mesaId))) {
            form.setMesaId(null);
        }
        model.addAttribute("mesas", mesas);
    }

    // Arma el texto con el resumen de la reserva para el mensaje de exito
    private String resumen(ReservaResponse r) {
        return "Mesa " + r.numeroMesa() + " el " + r.fecha().format(FORMATO_FECHA) + " a las " + r.hora()
                + " para " + r.personas() + (r.personas() == 1 ? " persona." : " personas.");
    }
}
