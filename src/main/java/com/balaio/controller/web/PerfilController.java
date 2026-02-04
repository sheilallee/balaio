package com.balaio.controller.web;

import java.security.Principal;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.balaio.model.Usuario;
import com.balaio.repository.UsuarioRepository;
import com.balaio.service.UsuarioService;

@Controller
@RequestMapping("/balaio/perfil")
public class PerfilController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // =========================
    // GET - Tela de perfil
    // =========================
    @GetMapping
    public String editarPerfil(Model model, Principal principal) {

        Usuario usuario = usuarioRepository
                .findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        model.addAttribute("usuario", usuario);
        return "perfil/perfil";
    }

    // =========================
    // POST - Salvar perfil
    // =========================
    @PostMapping
    public String salvarPerfil(
            @ModelAttribute Usuario formUsuario,
            @RequestParam(required = false) String senhaAtual,
            @RequestParam(required = false) String novaSenha,
            @RequestParam(required = false) String confirmarSenha,
            Principal principal,
            RedirectAttributes redirectAttributes
    ) {

        Usuario usuario = usuarioRepository
                .findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        // Atualiza dados básicos
        usuario.setNomeCompleto(formUsuario.getNomeCompleto());
        usuario.setEmail(formUsuario.getEmail());

        // ===== Alteração de senha (opcional) =====
        if (novaSenha != null && !novaSenha.isBlank()) {

            if (!novaSenha.equals(confirmarSenha)) {
                redirectAttributes.addFlashAttribute("erro", "As senhas não conferem.");
                return "redirect:/balaio/perfil";
            }

            if (novaSenha.length() < 8) {
                redirectAttributes.addFlashAttribute("erro", "A nova senha deve ter no mínimo 8 caracteres.");
                return "redirect:/balaio/perfil";
            }

            if (senhaAtual == null || !passwordEncoder.matches(senhaAtual, usuario.getSenha())) {
                redirectAttributes.addFlashAttribute("erro", "Senha atual incorreta.");
                return "redirect:/balaio/perfil";
            }

            usuario.setSenha(passwordEncoder.encode(novaSenha));
        }

        usuarioRepository.save(usuario);

        redirectAttributes.addFlashAttribute("sucesso", "Perfil atualizado com sucesso!");
        return "redirect:/balaio/listas";
    }

    // =========================
    // POST - Excluir conta
    // =========================
    @PostMapping("/excluir")
    public String excluirConta(
            @RequestParam String senhaConfirmacao,
            Principal principal,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes
    ) {

        Usuario usuario = usuarioRepository
                .findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        // Confirma senha (BCrypt)
        if (!passwordEncoder.matches(senhaConfirmacao, usuario.getSenha())) {
            redirectAttributes.addFlashAttribute("erro", "Senha incorreta.");
            return "redirect:/balaio/perfil";
        }

        // Remove usuário (use service para manter regras)
        usuarioService.excluirUsuario(usuario.getId());

        // Encerra sessão
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();

        return "redirect:/balaio/login?contaExcluida";
    }
}
