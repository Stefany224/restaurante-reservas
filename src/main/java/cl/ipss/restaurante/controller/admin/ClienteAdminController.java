package cl.ipss.restaurante.controller.admin;

import cl.ipss.restaurante.service.ClienteService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

// ClienteAdminController muestra al personal el listado de clientes
@Controller
@RequestMapping("/admin/clientes")
public class ClienteAdminController {

    // Service de clientes
    private final ClienteService clienteService;

    // Spring entrega el service por el constructor
    public ClienteAdminController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    // Lista los clientes con sus reservas activas
    @GetMapping
    public String listar(Model model) {
        model.addAttribute("clientes", clienteService.listar());
        return "admin/clientes";
    }
}
