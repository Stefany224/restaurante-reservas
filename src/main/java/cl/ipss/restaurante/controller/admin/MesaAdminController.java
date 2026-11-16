package cl.ipss.restaurante.controller.admin;

import cl.ipss.restaurante.dto.MesaForm;
import cl.ipss.restaurante.enums.UbicacionMesa;
import cl.ipss.restaurante.exception.ReglaNegocioException;
import cl.ipss.restaurante.service.MesaService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// MesaAdminController es el CRUD de mesas para el personal
@Controller
@RequestMapping("/admin/mesas")
public class MesaAdminController {

    // Service de mesas
    private final MesaService mesaService;

    // Spring entrega el service por el constructor
    public MesaAdminController(MesaService mesaService) {
        this.mesaService = mesaService;
    }

    // Lista todas las mesas
    @GetMapping
    public String listar(Model model) {
        model.addAttribute("mesas", mesaService.listar());
        return "admin/mesas";
    }

    // Formulario vacio para crear una mesa
    @GetMapping("/nueva")
    public String nueva(Model model) {
        return mostrarFormulario(model, new MesaForm(), null);
    }

    // Crea la mesa, si algo falla vuelve al formulario con el error
    @PostMapping
    public String crear(@Valid @ModelAttribute("mesa") MesaForm form, BindingResult resultado,
                        Model model, RedirectAttributes redirect) {
        if (resultado.hasErrors()) {
            return mostrarFormulario(model, form, null);
        }
        try {
            mesaService.crear(form);
            redirect.addFlashAttribute("exito", "Mesa " + form.getNumero() + " creada.");
            return "redirect:/admin/mesas";
        } catch (ReglaNegocioException ex) {
            model.addAttribute("error", ex.getMessage());
            return mostrarFormulario(model, form, null);
        }
    }

    // Formulario con los datos actuales de la mesa
    @GetMapping("/{id}/editar")
    public String editar(@PathVariable("id") Long id, Model model) {
        return mostrarFormulario(model, mesaService.formularioEdicion(id), id);
    }

    // Guarda los cambios de la mesa
    @PostMapping("/{id}/editar")
    public String actualizar(@PathVariable("id") Long id, @Valid @ModelAttribute("mesa") MesaForm form,
                             BindingResult resultado, Model model, RedirectAttributes redirect) {
        if (resultado.hasErrors()) {
            return mostrarFormulario(model, form, id);
        }
        try {
            mesaService.actualizar(id, form);
            redirect.addFlashAttribute("exito", "Mesa " + form.getNumero() + " actualizada.");
            return "redirect:/admin/mesas";
        } catch (ReglaNegocioException ex) {
            model.addAttribute("error", ex.getMessage());
            return mostrarFormulario(model, form, id);
        }
    }

    // Elimina la mesa, el service no deja si tiene reservas
    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable("id") Long id, RedirectAttributes redirect) {
        try {
            mesaService.eliminar(id);
            redirect.addFlashAttribute("exito", "Mesa eliminada.");
        } catch (ReglaNegocioException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/mesas";
    }

    // Carga lo que necesita la vista del formulario, la usan crear y editar
    private String mostrarFormulario(Model model, MesaForm form, Long mesaId) {
        model.addAttribute("mesa", form);
        model.addAttribute("mesaId", mesaId);
        model.addAttribute("editando", mesaId != null);
        model.addAttribute("ubicaciones", UbicacionMesa.values());
        return "admin/mesa-form";
    }
}
