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
import com.balaio.model.Item;
import com.balaio.model.Lista;
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
    public String loginPage(Model model) {
        model.addAttribute("loginDTO", new LoginDTO());
        return "auth/login";
    }

    @PostMapping("/login")
    public String login(@Valid @ModelAttribute LoginDTO loginDTO, 
                       HttpSession session, 
                       RedirectAttributes redirectAttributes) {
        try {
            // Autenticação web: verificar existência do usuário e validar senha
            Usuario usuario = usuarioService.buscarUsuarioPorEmail(loginDTO.getEmail());
            if (usuario != null) {
                // senha no banco está criptografada; usar PasswordEncoder.matches
                if (passwordEncoder.matches(loginDTO.getSenha(), usuario.getSenha())) {
                    session.setAttribute("usuarioLogado", usuario);
                    return "redirect:/balaio/dashboard";
                } else {
                    redirectAttributes.addFlashAttribute("erro", "Email ou senha inválidos");
                    return "redirect:/balaio/login";
                }
            } else {
                redirectAttributes.addFlashAttribute("erro", "Email ou senha inválidos");
                return "redirect:/balaio/login";
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro no login: " + e.getMessage());
            return "redirect:/balaio/login";
        }
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
            redirectAttributes.addFlashAttribute("sucesso", "Usuário cadastrado com sucesso! Faça login.");
            return "redirect:/balaio/login";
        } catch (Exception e) {
            logger.error("Erro no cadastro de usuário", e);
            redirectAttributes.addFlashAttribute("erro", "Erro no cadastro: " + e.getMessage());
            return "redirect:/balaio/cadastro";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/balaio/login";
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
        if (usuario == null) {
            return "redirect:/balaio/login";
        }
        model.addAttribute("usuario", usuario);

        // Estatísticas: listas criadas, itens adicionados/comprados, listas compartilhadas
        try {
            java.util.List<Lista> listasCriadas = listaService.listarListasProprietario(usuario.getId());
            int listasCriadasCount = listasCriadas != null ? listasCriadas.size() : 0;

            long itensAdicionadosTotal = 0L;
            long itensCompradosTotal = 0L;
            if (listasCriadas != null) {
                for (Lista l : listasCriadas) {
                    if (l != null && l.getId() != null) {
                        java.util.List<Item> itens = itemRepository.findByListaId(l.getId());
                        itensAdicionadosTotal += itens != null ? itens.size() : 0;
                        itensCompradosTotal += itemRepository.countByListaIdAndStatus(l.getId(), Item.StatusItem.COMPRADO);
                    }
                }
            }

            // Contar quantas listas deste usuário (como proprietário) foram compartilhadas com outros
            long listasCompartilhadasCountLong = listaService.contarListasCompartilhadasPorProprietario(usuario.getId());
            int listasCompartilhadasCount = (int) listasCompartilhadasCountLong;

            model.addAttribute("listasCriadasCount", listasCriadasCount);
            model.addAttribute("itensAdicionadosTotal", itensAdicionadosTotal);
            model.addAttribute("itensCompradosTotal", itensCompradosTotal);
            model.addAttribute("listasCompartilhadasCount", listasCompartilhadasCount);
        } catch (Exception e) {
            // Em caso de erro, expor zeros (não impedir a renderização do dashboard)
            model.addAttribute("listasCriadasCount", 0);
            model.addAttribute("itensAdicionadosTotal", 0);
            model.addAttribute("itensCompradosTotal", 0);
            model.addAttribute("listasCompartilhadasCount", 0);
        }
        return "redirect:/balaio/listas";
    }
}