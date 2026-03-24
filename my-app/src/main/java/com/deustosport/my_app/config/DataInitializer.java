package com.deustosport.my_app.config;

import com.deustosport.my_app.entity.Credencial;
import com.deustosport.my_app.entity.Usuario;
import com.deustosport.my_app.repository.CredencialRepository;
import com.deustosport.my_app.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private CredencialRepository credencialRepository;
    
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // Asegurar que la contraseña de Juan es correcta y válida con el encoder actual
        actualizarPassword("juan@deustosport.com", "password123");
        actualizarPassword("maria@deustosport.com", "password123");
        actualizarPassword("carlos@deustosport.com", "password123");
    }

    private void actualizarPassword(String email, String rawPassword) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            Optional<Credencial> credencialOpt = credencialRepository.findByUsuarioId(usuario.getId());
            
            Credencial credencil = credencialOpt.orElseGet(() -> {
                Credencial c = new Credencial();
                c.setUsuario(usuario);
                c.setActivo(true);
                c.setFechaCreacion(LocalDateTime.now());
                return c;
            });
            
            credencil.setPasswordHash(passwordEncoder.encode(rawPassword));
            credencialRepository.save(credencil);
            System.out.println("Contraseña actualizada para: " + email);
        }
    }
}
 