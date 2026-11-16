package cl.ipss.restaurante.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

// GlobalExceptionHandler atrapa los errores que no resolvio el controller y muestra la pagina de error con un mensaje claro
@ControllerAdvice
public class GlobalExceptionHandler {

    // Logger para dejar registro de los errores
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // La mesa o la reserva no existe, error 404
    @ExceptionHandler(RecursoNoEncontradoException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String noEncontrado(RecursoNoEncontradoException ex, Model model) {
        log.debug("--- response 404: {} ---", ex.getMessage());
        return error(model, 404, "No encontramos lo que buscas", ex.getMessage());
    }

    // Una regla de negocio que no atrapo el controller, error 409
    @ExceptionHandler(ReglaNegocioException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String reglaNegocio(ReglaNegocioException ex, Model model) {
        log.debug("--- response 409: {} ---", ex.getMessage());
        return error(model, 409, "No se pudo completar la operación", ex.getMessage());
    }

    // Una direccion que no existe, error 404
    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String rutaInexistente(NoResourceFoundException ex, Model model) {
        return error(model, 404, "Página no encontrada", "La dirección que ingresaste no existe");
    }

    // Un parametro mal escrito o que falta en la direccion, error 400
    @ExceptionHandler({MethodArgumentTypeMismatchException.class, MissingServletRequestParameterException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String solicitudInvalida(Exception ex, Model model) {
        log.warn("--- malformed request: {} ---", ex.getMessage());
        return error(model, 400, "Solicitud inválida", "Los datos de la dirección no son válidos");
    }

    // Cualquier otro error, queda en el log y el usuario ve un mensaje generico
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String inesperado(Exception ex, Model model) {
        log.error("Unexpected error processing request", ex);
        return error(model, 500, "Algo salió mal", "Ocurrió un error inesperado; inténtalo nuevamente");
    }

    // Carga los datos que muestra error.html
    private String error(Model model, int status, String titulo, String mensaje) {
        model.addAttribute("status", status);
        model.addAttribute("titulo", titulo);
        model.addAttribute("mensaje", mensaje);
        return "error";
    }
}
