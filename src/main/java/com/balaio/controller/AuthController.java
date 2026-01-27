package com.balaio.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.balaio.dto.LoginDTO;
import com.balaio.model.Usuario;
import com.balaio.service.UsuarioService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginDTO loginDTO, HttpSession session) {
        try {
            // CA3 - Validar se o email existe no banco de dados
            Usuario usuario = usuarioService.buscarPorEmail(loginDTO.getEmail())
                    .orElse(null);

            // CA4 - Validar se a senha informada corresponde à senha criptografada
            if (usuario != null && passwordEncoder.matches(loginDTO.getSenha(), usuario.getSenha())) {
                // CA6 - Criar sessão para o usuário
                session.setAttribute("usuarioLogado", usuario);
                session.setAttribute("usuarioId", usuario.getId());
                session.setAttribute("usuarioEmail", usuario.getEmail());

                Map<String, Object> response = new HashMap<>();
                response.put("mensagem", "Login realizado com sucesso");
                response.put("usuario", Map.of(
                    "id", usuario.getId(),
                    "nome", usuario.getNomeCompleto(),
                    "email", usuario.getEmail()
                ));

                return ResponseEntity.ok(response);
            } else {
                // CA5 - Email ou senha incorretos, exibir erro genérico
                Map<String, String> error = new HashMap<>();
                error.put("erro", "Email ou senha inválidos");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("erro", "Erro no login: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        try {
            // CA6 - Remover todas as informações de autenticação
            session.invalidate();

            Map<String, String> response = new HashMap<>();
            response.put("mensagem", "Logout realizado com sucesso");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("erro", "Erro ao fazer logout: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PostMapping("/verificar-sessao")
    public ResponseEntity<?> verificarSessao(HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");

        if (usuario != null) {
            Map<String, Object> response = new HashMap<>();
            response.put("autenticado", true);
            response.put("usuario", Map.of(
                "id", usuario.getId(),
                "nome", usuario.getNomeCompleto(),
                "email", usuario.getEmail()
            ));
            return ResponseEntity.ok(response);
        } else {
            Map<String, Object> response = new HashMap<>();
            response.put("autenticado", false);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }
}
