package com.balaio.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.balaio.dto.AlterarSenhaDTO;
import com.balaio.dto.UsuarioCadastroDTO;
import com.balaio.model.Usuario;
import com.balaio.service.UsuarioService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "*")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @PostMapping("/cadastro")
    public ResponseEntity<?> cadastrarUsuario(@Valid @RequestBody UsuarioCadastroDTO dto) {
        try {
            Usuario usuario = usuarioService.cadastrarUsuario(dto);
            
            Map<String, Object> response = new HashMap<>();
            response.put("mensagem", "Usuário cadastrado com sucesso");
            response.put("usuario", Map.of(
                "id", usuario.getId(),
                "nome", usuario.getNomeCompleto(),
                "email", usuario.getEmail()
            ));
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("erro", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/perfil")
    public ResponseEntity<?> obterPerfil() {
        try {
            String email = getCurrentUserEmail();
            Usuario usuario = usuarioService.buscarPorEmail(email)
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

            Map<String, Object> response = new HashMap<>();
            response.put("id", usuario.getId());
            response.put("nome", usuario.getNomeCompleto());
            response.put("email", usuario.getEmail());
            response.put("dataCriacao", usuario.getDataCriacao());

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("erro", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PutMapping("/perfil")
    public ResponseEntity<?> atualizarPerfil(@RequestBody Map<String, String> dados) {
        try {
            String email = getCurrentUserEmail();
            Usuario usuario = usuarioService.buscarPorEmail(email)
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

            Usuario usuarioAtualizado = new Usuario();
            usuarioAtualizado.setNomeCompleto(dados.get("nome"));
            usuarioAtualizado.setEmail(dados.get("email"));

            Usuario usuarioSalvo = usuarioService.atualizarUsuario(usuario.getId(), usuarioAtualizado);

            Map<String, Object> response = new HashMap<>();
            response.put("mensagem", "Perfil atualizado com sucesso");
            response.put("usuario", Map.of(
                "id", usuarioSalvo.getId(),
                "nome", usuarioSalvo.getNomeCompleto(),
                "email", usuarioSalvo.getEmail()
            ));

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("erro", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PutMapping("/alterar-senha")
    public ResponseEntity<?> alterarSenha(@Valid @RequestBody AlterarSenhaDTO dto) {
        try {
            String email = getCurrentUserEmail();
            Usuario usuario = usuarioService.buscarPorEmail(email)
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

            usuarioService.alterarSenha(usuario.getId(), dto);

            Map<String, String> response = new HashMap<>();
            response.put("mensagem", "Senha alterada com sucesso");

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("erro", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/buscar")
    public ResponseEntity<?> buscarUsuarios(@RequestParam String email) {
        try {
            Usuario usuario = usuarioService.buscarPorEmail(email)
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

            Map<String, Object> response = new HashMap<>();
            response.put("id", usuario.getId());
            response.put("nome", usuario.getNomeCompleto());
            response.put("email", usuario.getEmail());

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("erro", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/listar")
    public ResponseEntity<?> listarUsuarios() {
        return ResponseEntity.ok(
                usuarioService.listarTodos().stream().map(usuario -> Map.of(
                        "id", usuario.getId(),
                        "nome", usuario.getNomeCompleto(),
                        "email", usuario.getEmail()
                )).toList()
        );
    }

    private String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }

    private Long getCurrentUserId() {
        String email = getCurrentUserEmail();
        Usuario usuario = usuarioService.buscarPorEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        return usuario.getId();
    }
}