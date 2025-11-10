package com.balaio.config;

import java.time.LocalDateTime;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.balaio.model.Usuario;
import com.balaio.repository.UsuarioRepository;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner createDefaultAdmin(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            String adminEmail = "admin@email.com";
            if (!usuarioRepository.existsByEmail(adminEmail)) {
                Usuario admin = new Usuario();
                admin.setEmail(adminEmail);
                admin.setNomeCompleto("Administrador");
                admin.setSenha(passwordEncoder.encode("admin"));
                admin.setDataCriacao(LocalDateTime.now());
                admin.setDataAtualizacao(LocalDateTime.now());
                usuarioRepository.save(admin);
                System.out.println("Usuário admin criado: " + adminEmail);
            } else {
                System.out.println("Usuário admin já existe: " + adminEmail);
            }
        };
    }
}
