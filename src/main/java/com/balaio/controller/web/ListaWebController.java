package com.balaio.controller.web;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.balaio.model.Item;
import com.balaio.model.Lista;
import com.balaio.model.Usuario;
import com.balaio.service.ItemService;
import com.balaio.service.ListaService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/balaio/listas")
public class ListaWebController {

    @Autowired
    private ListaService listaService;

    @Autowired
    private ItemService itemService;

    @Autowired
    private com.balaio.service.UsuarioService usuarioService;

    private static final Logger logger = LoggerFactory.getLogger(ListaWebController.class);

    @GetMapping
    public String listarListas(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
        if (usuario == null) {
            return "redirect:/balaio/login";
        }

        List<Lista> listas = listaService.listarListasDoUsuario(usuario.getId());

        List<com.balaio.dto.ListaResumoDTO> listasResumo = new ArrayList<>();
        long totalCompradoGeral = 0;
        java.math.BigDecimal totalGastoGeral = java.math.BigDecimal.ZERO;
        int totalItensGeral = 0;
        
        for (Lista l : listas) {
            List<Item> itens = itemService.listarItensDaLista(l.getId(), usuario.getId());
            int totalItens = itens != null ? itens.size() : 0;
            int totalComprados = itens != null ? (int) itens.stream().filter(i -> i.getStatus() == Item.StatusItem.COMPRADO).count() : 0;

            listasResumo.add(new com.balaio.dto.ListaResumoDTO(
                    l.getId(), l.getTitulo(), l.getDescricao(), totalItens, totalComprados
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
        model.addAttribute("usuario", usuario);
        model.addAttribute("lista", new Lista());
        model.addAttribute("totalCompradoGeral", totalCompradoGeral);
        model.addAttribute("totalGastoGeral", totalGastoGeral);
        model.addAttribute("totalItensGeral", totalItensGeral);
        return "listas/index";
    }

    @GetMapping("/nova")
    public String novaListaPage(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
        if (usuario == null) {
            return "redirect:/balaio/login";
        }

        model.addAttribute("lista", new Lista());
        model.addAttribute("usuario", usuario);
        return "listas/nova";
    }

    @PostMapping("/nova")
    public String criarLista(@RequestParam String titulo,
                             @RequestParam String descricao,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
        if (usuario == null) {
            return "redirect:/balaio/login";
        }

        try {
            Lista criada = listaService.criarLista(titulo, descricao, usuario.getId());
            redirectAttributes.addFlashAttribute("sucesso", "Lista criada com sucesso!");
            return "redirect:/balaio/listas/" + criada.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao criar lista: " + e.getMessage());
            return "redirect:/balaio/listas";
        }
    }

    @GetMapping("/{id}")
    public String verLista(@PathVariable Long id, HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
        if (usuario == null) {
            return "redirect:/balaio/login";
        }

        try {
            Lista lista = listaService.buscarPorId(id).orElseThrow(() -> new RuntimeException("Lista não encontrada"));
            System.out.println("=== CARREGANDO LISTA ===");
            System.out.println("Lista ID: " + lista.getId());
            System.out.println("Lista Título: " + lista.getTitulo());
            System.out.println("Proprietário: " + (lista.getProprietario() != null ? lista.getProprietario().getEmail() : "NULL"));
            System.out.println("Usuário logado: " + usuario.getEmail());
            System.out.println("Número de colaboradores: " + (lista.getColaboradores() != null ? lista.getColaboradores().size() : 0));
                
            if (lista.getColaboradores() != null && !lista.getColaboradores().isEmpty()) {
                for (Usuario colab : lista.getColaboradores()) {
                    System.out.println("Colaborador: " + colab.getEmail() + " - " + colab.getNomeCompleto());
                }
            } else {
                System.out.println("Nenhum colaborador encontrado");
            }

            model.addAttribute("lista", lista);
            model.addAttribute("usuario", usuario);
            model.addAttribute("usuarioLogado", usuario);
            
            List<Item> itens = itemService.listarItensDaLista(id, usuario.getId());
            logger.debug("Recuperados {} itens para a lista {}", itens.size(), id);
            
            java.math.BigDecimal totalEstimado = java.math.BigDecimal.ZERO;
            for (Item item : itens) {
                if (item.getStatus() == Item.StatusItem.COMPRADO && 
                    item.getValor() != null && 
                    item.getQuantidade() != null) {
                    try {
                        java.math.BigDecimal valorTotalItem = item.getValor().multiply(
                            java.math.BigDecimal.valueOf(item.getQuantidade())
                        );
                        totalEstimado = totalEstimado.add(valorTotalItem);
                    } catch (Exception e) {
                        logger.warn("Erro ao calcular valor do item {} ({}): {}", 
                                item.getId(), item.getNomeProduto(), e.getMessage());
                    }
                }
            }
            
            long totalComprados = itens.stream().filter(i -> i.getStatus() == Item.StatusItem.COMPRADO).count();
            long totalPendentes = itens.stream().filter(i -> i.getStatus() == Item.StatusItem.PENDENTE).count();

            
            String progressoWidth;
            String percentualTexto;

            if ((totalComprados + totalPendentes) > 0) {
                double percentual = (totalComprados * 100.0) / (totalComprados + totalPendentes);
                
                // Garantir que não passe de 100%
                percentual = Math.min(percentual, 100.0);
                
                // Remove a casa decimal se for .0
                if (percentual % 1 == 0) {
                    percentualTexto = String.format("%.0f", percentual);
                } else {
                    percentualTexto = String.format("%.1f", percentual).replace(".", ",");
                }
                progressoWidth = percentualTexto + "%";
            } else {
                progressoWidth = "0%";
                percentualTexto = "0";
            }
        
            model.addAttribute("progressoWidth", progressoWidth);
            model.addAttribute("percentualTexto", percentualTexto);
            model.addAttribute("itens", itens);
            model.addAttribute("novoItem", new Item());
            model.addAttribute("totalEstimado", totalEstimado);
            model.addAttribute("totalComprados", totalComprados);
            model.addAttribute("totalPendentes", totalPendentes);
            
            return "listas/detalhes";
        } catch (Exception e) {
            logger.error("Erro ao carregar lista {}: {}", id, e.getMessage());
            return "redirect:/balaio/listas";
        }
    }

    @PostMapping("/{id}/itens")
    public String adicionarItem(@PathVariable Long id,
                                @RequestParam String nomeProduto,
                                @RequestParam Integer quantidade,
                                @RequestParam(required = false) String unidade,  
                                @RequestParam(required = false) java.math.BigDecimal valor,
                               
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
        if (usuario == null) {
            return "redirect:/balaio/login";
        }

        try {
            itemService.criarItem(nomeProduto, quantidade, valor, unidade, id, usuario.getId());
            //itemService.criarItem(novoItem.getNomeProduto(), novoItem.getQuantidade(), novoItem.getValor(), id, usuario.getId());
            redirectAttributes.addFlashAttribute("sucesso", "Item adicionado com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao adicionar item: " + e.getMessage());
        }

        return "redirect:/balaio/listas/" + id;
    }

    @PostMapping("/{listaId}/itens/{itemId}/toggle")
    public String toggleItemStatus(@PathVariable Long listaId,
                                @PathVariable Long itemId,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
        if (usuario == null) {
            return "redirect:/balaio/login";
        }

        try {
            Item item = itemService.buscarPorId(itemId).orElseThrow(() -> new RuntimeException("Item não encontrado"));
            
            // LOG PARA DEBUG
            logger.info("Toggle item - ID: {}, Status atual: {}, Usuário: {}", 
                    itemId, item.getStatus(), usuario.getId());
            
            Item itemAtualizado;
            if (item.getStatus() == Item.StatusItem.COMPRADO) {
                itemAtualizado = itemService.marcarComoPendente(itemId, usuario.getId());
                logger.info("Item {} marcado como PENDENTE", itemId);
            } else {
                itemAtualizado = itemService.marcarComoComprado(itemId, usuario.getId());
                logger.info("Item {} marcado como COMPRADO", itemId);
            }
            
            // LOG para verificar se foi salvo
            logger.info("Item atualizado - Status: {}", itemAtualizado.getStatus());
            
            redirectAttributes.addFlashAttribute("sucesso", "Status do item atualizado!");
        } catch (Exception e) {
            logger.error("Erro ao atualizar item {}: {}", itemId, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("erro", "Erro ao atualizar item: " + e.getMessage());
        }

        return "redirect:/balaio/listas/" + listaId;
    }

    @PostMapping("/{listaId}/itens/{itemId}/deletar")
    public String deletarItem(@PathVariable Long listaId,
                              @PathVariable Long itemId,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
        if (usuario == null) {
            return "redirect:/balaio/login";
        }

        try {
            itemService.excluirItem(itemId, usuario.getId());
            redirectAttributes.addFlashAttribute("sucesso", "Item excluído com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao excluir item: " + e.getMessage());
        }

        return "redirect:/balaio/listas/" + listaId;
    }

    @GetMapping("/{listaId}/itens/{itemId}/editar")
    public String editarItemPage(@PathVariable Long listaId,
                                 @PathVariable Long itemId,
                                 HttpSession session,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
        if (usuario == null) {
            return "redirect:/balaio/login";
        }

        try {
            Item item = itemService.buscarPorId(itemId).orElseThrow(() -> new RuntimeException("Item não encontrado"));
            // Redireciona para a página de detalhes; edição é feita via modal nessa página
            return "redirect:/balaio/listas/" + listaId;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro: " + e.getMessage());
            return "redirect:/balaio/listas/" + listaId;
        }
    }

    @PostMapping("/{listaId}/itens/{itemId}/editar")
    public String editarItem(@PathVariable Long listaId,
                             @PathVariable Long itemId,
                             @RequestParam String nomeProduto,
                             @RequestParam Integer quantidade,
                             @RequestParam(required = false) String unidade, 
                             @RequestParam(required = false) java.math.BigDecimal valor,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
        if (usuario == null) {
            return "redirect:/balaio/login";
        }

        try {
            itemService.atualizarItem(itemId, nomeProduto, quantidade, valor, unidade, usuario.getId());
            redirectAttributes.addFlashAttribute("sucesso", "Item atualizado com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao atualizar item: " + e.getMessage());
        }
        return "redirect:/balaio/listas/" + listaId;
    }

    @PostMapping("/{id}/deletar")
    public String deletarLista(@PathVariable Long id,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
        if (usuario == null) {
            return "redirect:/balaio/login";
        }

        try {
            listaService.excluirLista(id, usuario.getId());
            redirectAttributes.addFlashAttribute("sucesso", "Lista deletada com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao deletar lista: " + e.getMessage());
        }

        return "redirect:/balaio/listas";
    }

    @PostMapping("/{id}/compartilhar")
    public String compartilharLista(@PathVariable Long id,
                                     @RequestParam String email,
                                     HttpSession session,
                                     RedirectAttributes redirectAttributes) {
        System.out.println("=== CONTROLLER COMPARTILHAR ===");
        System.out.println("Lista ID: " + id);
        System.out.println("Email recebido: " + email);
                                            
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
        if (usuario == null) {
            System.out.println("ERRO: Usuário não logado");
            return "redirect:/balaio/login";
        }

        System.out.println("Usuário logado: " + usuario.getEmail());
        System.out.println("Usuário ID: " + usuario.getId());
        
        try {
            listaService.compartilharLista(id, email, usuario.getId());
            redirectAttributes.addFlashAttribute("sucesso", "Lista compartilhada com sucesso com " + email);
            System.out.println("SUCESSO: Redirecionando para lista " + id);
        } catch (Exception e) {
            System.out.println("ERRO no compartilhamento: " + e.getMessage());
            redirectAttributes.addFlashAttribute("erro", "Erro ao compartilhar lista: " + e.getMessage());
        }

            return "redirect:/balaio/listas/" + id;
    }

    // Endpoint adicional para requisições AJAX/JSON do mesmo path
    @PostMapping(path = "/{id}/compartilhar", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> compartilharListaAjax(@PathVariable Long id,
                                                   @RequestBody java.util.Map<String, String> body,
                                                   HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
        // se não houver usuário na sessão, tentar SecurityContext (autenticação)
        if (usuario == null) {
            org.springframework.security.core.Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getName())) {
                String emailAut = authentication.getName();
                usuario = usuarioService.buscarPorEmail(emailAut).orElse(null);
            }
        }

        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(java.util.Map.of("erro", "Usuário não autenticado"));
        }

        String email = body.get("emailColaborador");
        try {
            Lista lista = listaService.compartilharLista(id, email, usuario.getId());
            java.util.Map<String, Object> resp = new java.util.HashMap<>();
            resp.put("mensagem", "Lista compartilhada com sucesso com " + email);
            resp.put("lista", java.util.Map.of("id", lista.getId(), "titulo", lista.getTitulo()));
            return ResponseEntity.ok(resp);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("erro", e.getMessage()));
        }
    }

    @GetMapping("/{id}/editar")
    public String editarListaPage(@PathVariable Long id, HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
        if (usuario == null) {
            return "redirect:/balaio/login";
        }

        try {
            Lista lista = listaService.buscarPorId(id).orElseThrow(() -> new RuntimeException("Lista não encontrada"));
            // verificar permissão
            if (!lista.getProprietario().getId().equals(usuario.getId())) {
                redirectAttributes.addFlashAttribute("erro", "Apenas o proprietário pode editar a lista");
                return "redirect:/balaio/listas";
            }
            model.addAttribute("lista", lista);
            model.addAttribute("usuario", usuario);
            return "listas/editar";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro: " + e.getMessage());
            return "redirect:/balaio/listas";
        }
    }

    @PostMapping("/{id}/editar")
    public String editarLista(@PathVariable Long id,
                              @RequestParam String titulo,
                              @RequestParam String descricao,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
        if (usuario == null) {
            return "redirect:/balaio/login";
        }

        try {
            listaService.atualizarLista(id, titulo, descricao, usuario.getId());
            redirectAttributes.addFlashAttribute("sucesso", "Lista atualizada com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao atualizar lista: " + e.getMessage());
        }

        return "redirect:/balaio/listas";
    }
}