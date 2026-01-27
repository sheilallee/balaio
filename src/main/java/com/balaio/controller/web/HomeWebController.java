package com.balaio.controller.web;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.balaio.model.Item;
import com.balaio.model.Lista;
import com.balaio.model.Usuario;
import com.balaio.service.ItemService;
import com.balaio.service.ListaService;

import jakarta.servlet.http.HttpSession;

@Controller
public class HomeWebController {

    @Autowired
    private ListaService listaService;

    @Autowired
    private ItemService itemService;

    @GetMapping("/")
    public String home() {
        return "redirect:/balaio";
    }

    @GetMapping("/balaio")
    public String webHome(HttpSession session, Model model) {
        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null) {
            return "redirect:/balaio/login";
        }
        
        // Buscar listas do usuário
        List<Lista> listas = listaService.listarListasDoUsuario(usuarioLogado.getId());

        List<com.balaio.dto.ListaResumoDTO> listasResumo = new ArrayList<>();
        long totalCompradoGeral = 0;
        java.math.BigDecimal totalGastoGeral = java.math.BigDecimal.ZERO;
        int totalItensGeral = 0;
        
        for (Lista l : listas) {
            List<Item> itens = itemService.listarItensDaLista(l.getId(), usuarioLogado.getId());
            int totalItens = itens != null ? itens.size() : 0;
            int totalComprados = itens != null ? (int) itens.stream().filter(i -> i.getStatus() == Item.StatusItem.COMPRADO).count() : 0;
            boolean isProprietario = l.getProprietario() != null && l.getProprietario().getId().equals(usuarioLogado.getId());

            listasResumo.add(new com.balaio.dto.ListaResumoDTO(
                    l.getId(), l.getTitulo(), l.getDescricao(), totalItens, totalComprados, isProprietario
            ));
            
            // Acumula totais gerais
            totalItensGeral += totalItens;
            totalCompradoGeral += totalComprados;
            
            // Calcula total gasto em itens comprados
            if (itens != null) {
                for (Item item : itens) {
                    if (item.getStatus() == Item.StatusItem.COMPRADO && 
                        item.getValor() != null && 
                        item.getQuantidade() != null) {
                        java.math.BigDecimal valorTotalItem = item.getValor().multiply(
                            java.math.BigDecimal.valueOf(item.getQuantidade())
                        );
                        totalGastoGeral = totalGastoGeral.add(valorTotalItem);
                    }
                }
            }
        }

        model.addAttribute("listasResumo", listasResumo);
        model.addAttribute("listas", listas);
        model.addAttribute("usuario", usuarioLogado);
        model.addAttribute("lista", new Lista());
        model.addAttribute("totalCompradoGeral", totalCompradoGeral);
        model.addAttribute("totalGastoGeral", totalGastoGeral);
        model.addAttribute("totalItensGeral", totalItensGeral);
        
        return "listas/index";
    }
}