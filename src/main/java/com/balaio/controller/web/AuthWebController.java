package com.balaio.controller.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.balaio.dto.LoginDTO;
import com.balaio.dto.UsuarioCadastroDTO;
import com.balaio.model.Usuario;
import com.balaio.repository.ItemRepository;
import com.balaio.service.ListaService;
import com.balaio.service.UsuarioService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/balaio")
public class AuthWebController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Autowired
    private ListaService listaService;

    @Autowired
    private ItemRepository itemRepository;

    private static final Logger logger = LoggerFactory.getLogger(AuthWebController.class);

    @GetMapping("/login")
    public String loginPage(Model model, HttpSession session) {
        // Se já está autenticado, redireciona para a home
        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
        if (usuarioLogado != null) {
            return "redirect:/balaio";
        }
        
        // Verificar se há erro de login na sessão
        String loginError = (String) session.getAttribute("loginError");
        if (loginError != null) {
            model.addAttribute("erro", loginError);
            session.removeAttribute("loginError"); // Remove após exibir
        }
        
        model.addAttribute("loginDTO", new LoginDTO());
        return "auth/login";
    }

    @GetMapping("/cadastro")
    public String cadastroPage(Model model) {
        model.addAttribute("usuarioCadastroDTO", new UsuarioCadastroDTO());
        return "auth/cadastro";
    }

    @PostMapping("/cadastro")
    public String cadastro(@Valid @ModelAttribute UsuarioCadastroDTO usuarioCadastroDTO,
                           BindingResult bindingResult,
                           RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            String msg = bindingResult.getAllErrors().get(0).getDefaultMessage();
            redirectAttributes.addFlashAttribute("erro", msg);
            return "redirect:/balaio/cadastro";
        }
        try {
            usuarioService.cadastrarUsuario(usuarioCadastroDTO);
            // CA8 - Mensagem de sucesso conforme especificado
            redirectAttributes.addFlashAttribute("sucesso", "Cadastro realizado com sucesso");
            return "redirect:/balaio/login";
        } catch (Exception e) {
            logger.error("Erro no cadastro de usuário", e);
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
            return "redirect:/balaio/cadastro";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/balaio/login";
    }
}