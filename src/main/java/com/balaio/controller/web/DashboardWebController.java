package com.balaio.controller.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.balaio.dto.DashboardDTO;
import com.balaio.model.Usuario;
import com.balaio.service.ListaService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/balaio")
public class DashboardWebController {

    @Autowired
    private ListaService listaService;

    /**
     * CA1 (Dashboard) - Dashboard deve ser acessível através de link visível no menu principal
     */
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
        if (usuario == null) {
            return "redirect:/balaio/login";
        }

        // CA2 (Dashboard) - Dados atualizados automaticamente
        DashboardDTO dashboard = listaService.gerarDashboard(usuario.getId());
        
        model.addAttribute("usuario", usuario);
        model.addAttribute("dashboard", dashboard);

        return "dashboard/index";
    }
}
