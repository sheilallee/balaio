package com.balaio.controller;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.balaio.model.Item;
import com.balaio.model.Usuario;
import com.balaio.service.ItemService;
import com.balaio.service.UsuarioService;

@RestController
@RequestMapping("/api/listas/{listaId}/itens")
@CrossOrigin(origins = "*")
public class ItemController {

    @Autowired
    private ItemService itemService;

    @Autowired
    private UsuarioService usuarioService;

    @PostMapping
    public ResponseEntity<?> criarItem(@PathVariable Long listaId, @RequestBody Map<String, Object> dados) {
        try {
            Long usuarioId = getCurrentUserId();
            
            String nomeProduto = (String) dados.get("nomeProduto");
            Integer quantidade = (Integer) dados.get("quantidade");
            BigDecimal valor = dados.get("valor") != null ? 
                new BigDecimal(dados.get("valor").toString()) : null;

            Item item = itemService.criarItem(nomeProduto, quantidade, valor, listaId, usuarioId);

            Map<String, Object> response = new HashMap<>();
            response.put("mensagem", "Item criado com sucesso");
            response.put("item", criarMapaItem(item));

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("erro", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping
    public ResponseEntity<?> listarItens(@PathVariable Long listaId) {
        try {
            Long usuarioId = getCurrentUserId();
            List<Item> itens = itemService.listarItensDaLista(listaId, usuarioId);

            List<Map<String, Object>> itensResponse = itens.stream()
                    .map(this::criarMapaItem)
                    .toList();

            return ResponseEntity.ok(itensResponse);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("erro", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/pendentes")
    public ResponseEntity<?> listarItensPendentes(@PathVariable Long listaId) {
        try {
            Long usuarioId = getCurrentUserId();
            List<Item> itens = itemService.listarItensPorStatus(listaId, Item.StatusItem.PENDENTE, usuarioId);

            List<Map<String, Object>> itensResponse = itens.stream()
                    .map(this::criarMapaItem)
                    .toList();

            return ResponseEntity.ok(itensResponse);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("erro", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/comprados")
    public ResponseEntity<?> listarItensComprados(@PathVariable Long listaId) {
        try {
            Long usuarioId = getCurrentUserId();
            List<Item> itens = itemService.listarItensPorStatus(listaId, Item.StatusItem.COMPRADO, usuarioId);

            List<Map<String, Object>> itensResponse = itens.stream()
                    .map(this::criarMapaItem)
                    .toList();

            return ResponseEntity.ok(itensResponse);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("erro", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<?> obterItem(@PathVariable Long listaId, @PathVariable Long itemId) {
        try {
            Long usuarioId = getCurrentUserId();
            
            Item item = itemService.buscarPorId(itemId)
                    .orElseThrow(() -> new RuntimeException("Item não encontrado"));

            // Verificar se o item pertence à lista especificada
            if (!item.getLista().getId().equals(listaId)) {
                Map<String, String> error = new HashMap<>();
                error.put("erro", "Item não pertence a esta lista");
                return ResponseEntity.badRequest().body(error);
            }

            return ResponseEntity.ok(criarMapaItem(item));
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("erro", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PutMapping("/{itemId}")
    public ResponseEntity<?> atualizarItem(@PathVariable Long listaId, @PathVariable Long itemId, 
                                         @RequestBody Map<String, Object> dados) {
        try {
            Long usuarioId = getCurrentUserId();
            
            String nomeProduto = (String) dados.get("nomeProduto");
            Integer quantidade = (Integer) dados.get("quantidade");
            BigDecimal valor = dados.get("valor") != null ? 
                new BigDecimal(dados.get("valor").toString()) : null;

            Item item = itemService.atualizarItem(itemId, nomeProduto, quantidade, valor, usuarioId);

            Map<String, Object> response = new HashMap<>();
            response.put("mensagem", "Item atualizado com sucesso");
            response.put("item", criarMapaItem(item));

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("erro", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PutMapping("/{itemId}/marcar-comprado")
    public ResponseEntity<?> marcarComoComprado(@PathVariable Long listaId, @PathVariable Long itemId) {
        try {
            Long usuarioId = getCurrentUserId();
            
            Item item = itemService.marcarComoComprado(itemId, usuarioId);

            Map<String, Object> response = new HashMap<>();
            response.put("mensagem", "Item marcado como comprado");
            response.put("item", criarMapaItem(item));

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("erro", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PutMapping("/{itemId}/marcar-pendente")
    public ResponseEntity<?> marcarComoPendente(@PathVariable Long listaId, @PathVariable Long itemId) {
        try {
            Long usuarioId = getCurrentUserId();
            
            Item item = itemService.marcarComoPendente(itemId, usuarioId);

            Map<String, Object> response = new HashMap<>();
            response.put("mensagem", "Item marcado como pendente");
            response.put("item", criarMapaItem(item));

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("erro", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<?> excluirItem(@PathVariable Long listaId, @PathVariable Long itemId) {
        try {
            Long usuarioId = getCurrentUserId();
            
            itemService.excluirItem(itemId, usuarioId);

            Map<String, String> response = new HashMap<>();
            response.put("mensagem", "Item excluído com sucesso");

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("erro", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/estatisticas")
    public ResponseEntity<?> obterEstatisticas(@PathVariable Long listaId) {
        try {
            Long usuarioId = getCurrentUserId();
            
            long totalItens = itemService.contarItensPorStatus(listaId, Item.StatusItem.PENDENTE, usuarioId) +
                             itemService.contarItensPorStatus(listaId, Item.StatusItem.COMPRADO, usuarioId);
            long itensPendentes = itemService.contarItensPorStatus(listaId, Item.StatusItem.PENDENTE, usuarioId);
            long itensComprados = itemService.contarItensPorStatus(listaId, Item.StatusItem.COMPRADO, usuarioId);

            Map<String, Object> estatisticas = new HashMap<>();
            estatisticas.put("total", totalItens);
            estatisticas.put("pendentes", itensPendentes);
            estatisticas.put("comprados", itensComprados);
            estatisticas.put("percentualCompleto", totalItens > 0 ? (double) itensComprados / totalItens * 100 : 0);

            return ResponseEntity.ok(estatisticas);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("erro", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
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

    private Map<String, Object> criarMapaItem(Item item) {
        Map<String, Object> mapa = new HashMap<>();
        mapa.put("id", item.getId());
        mapa.put("nomeProduto", item.getNomeProduto());
        mapa.put("quantidade", item.getQuantidade());
        mapa.put("valor", item.getValor());
        mapa.put("status", item.getStatus().toString());
        mapa.put("dataCriacao", item.getDataCriacao());
        mapa.put("dataAtualizacao", item.getDataAtualizacao());
        
        Map<String, Object> lista = new HashMap<>();
        lista.put("id", item.getLista().getId());
        lista.put("titulo", item.getLista().getTitulo());
        mapa.put("lista", lista);

        return mapa;
    }
}