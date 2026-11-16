package cl.ipss.restaurante.config;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.expression.WebExpressionAuthorizationManager;
import org.springframework.security.web.csrf.CsrfException;

// SecurityConfig define quien puede entrar a cada parte del sistema y como inicia sesion el personal
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // Reglas de acceso, todo lo de /admin es solo para el personal y el resto es publico
    @Bean
    public SecurityFilterChain filtroSeguridad(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        // el personal con sesion iniciada no puede usar las paginas de clientes porque no reserva mesas
                        .requestMatchers("/", "/reservas/**")
                        .access(new WebExpressionAuthorizationManager("!hasRole('ADMIN')"))
                        .anyRequest().permitAll())
                // si el personal intenta entrar igual lo mandamos a su panel, salvo que el error sea por el token CSRF
                .exceptionHandling(ex -> ex
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            if (accessDeniedException instanceof CsrfException) {
                                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                            } else {
                                response.sendRedirect(request.getContextPath() + "/admin/reservas");
                            }
                        }))
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/admin/reservas", true)
                        .permitAll())
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?salir")
                        .permitAll());
        return http.build();
    }

    // Las claves se guardan cifradas con BCrypt y nunca en texto plano
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Usuario del personal en memoria, su usuario y clave salen del application.yml
    @Bean
    public UserDetailsService usuarios(PasswordEncoder encoder,
                                       @Value("${app.admin.usuario}") String usuario,
                                       @Value("${app.admin.clave}") String clave) {
        UserDetails admin = User.withUsername(usuario)
                .password(encoder.encode(clave))
                .roles("ADMIN")
                .build();
        return new InMemoryUserDetailsManager(admin);
    }
}
