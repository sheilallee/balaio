package com.balaio.controller;

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
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.balaio.model.Lista;
import com.balaio.model.Usuario;
import com.balaio.service.ListaService;
import com.balaio.service.UsuarioService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/listas")
@CrossOrigin(origins = "*")
public class ListaController {

    @Autowired
    private ListaService listaService;

    @Autowired
    private UsuarioService usuarioService;

    @PostMapping
    public ResponseEntity<?> criarLista(@RequestBody Map<String, String> dados) {
        try {
            Long usuarioId = getCurrentUserId();
            
            Lista lista = listaService.criarLista(
                dados.get("titulo"),
                dados.get("descricao"),
                usuarioId
            );

            Map<String, Object> response = new HashMap<>();
            response.put("mensagem", "Lista criada com sucesso");
            response.put("lista", criarMapaLista(lista));

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("erro", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping
    public ResponseEntity<?> listarListas() {
        try {
            Long usuarioId = getCurrentUserId();
            List<Lista> listas = listaService.listarListasDoUsuario(usuarioId);

            List<Map<String, Object>> listasResponse = listas.stream()
                    .map(this::criarMapaLista)
                    .toList();

            return ResponseEntity.ok(listasResponse);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("erro", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/minhas")
    public ResponseEntity<?> listarMinhasListas() {
        try {
            Long usuarioId = getCurrentUserId();
            List<Lista> listas = listaService.listarListasProprietario(usuarioId);

            List<Map<String, Object>> listasResponse = listas.stream()
                    .map(this::criarMapaLista)
                    .toList();

            return ResponseEntity.ok(listasResponse);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("erro", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/compartilhadas")
    public ResponseEntity<?> listarListasCompartilhadas() {
        try {
            Long usuarioId = getCurrentUserId();
            List<Lista> listas = listaService.listarListasCompartilhadas(usuarioId);

            List<Map<String, Object>> listasResponse = listas.stream()
                    .map(this::criarMapaLista)
                    .toList();

            return ResponseEntity.ok(listasResponse);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("erro", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obterLista(@PathVariable Long id) {
        try {
            Long usuarioId = getCurrentUserId();
            
            if (!listaService.usuarioTemAcesso(id, usuarioId)) {
                Map<String, String> error = new HashMap<>();
                error.put("erro", "Acesso negado a esta lista");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }

            Lista lista = listaService.buscarPorId(id)
                    .orElseThrow(() -> new RuntimeException("Lista não encontrada"));

            return ResponseEntity.ok(criarMapaLista(lista));
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("erro", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizarLista(@PathVariable Long id, @RequestBody Map<String, String> dados) {
        try {
            Long usuarioId = getCurrentUserId();
            
            Lista lista = listaService.atualizarLista(
                id,
                dados.get("titulo"),
                dados.get("descricao"),
                usuarioId
            );

            Map<String, Object> response = new HashMap<>();
            response.put("mensagem", "Lista atualizada com sucesso");
            response.put("lista", criarMapaLista(lista));

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("erro", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> excluirLista(@PathVariable Long id) {
        try {
            Long usuarioId = getCurrentUserId();
            
            listaService.excluirLista(id, usuarioId);

            Map<String, String> response = new HashMap<>();
            response.put("mensagem", "Lista excluída com sucesso");

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("erro", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/{id}/compartilhar")
    public ResponseEntity<?> compartilharLista(@PathVariable Long id, @RequestBody Map<String, String> dados) {
        try {
            Long usuarioId = getCurrentUserId();
            
            Lista lista = listaService.compartilharLista(
                id,
                dados.get("emailColaborador"),
                usuarioId
            );

            Map<String, Object> response = new HashMap<>();
            response.put("mensagem", "Lista compartilhada com sucesso");
            response.put("lista", criarMapaLista(lista));

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("erro", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @DeleteMapping("/{listaId}/colaboradores/{colaboradorId}")
    public ResponseEntity<?> removerColaborador(@PathVariable Long listaId, @PathVariable Long colaboradorId) {
        try {
            Long usuarioId = getCurrentUserId();
            
            Lista lista = listaService.removerColaborador(listaId, colaboradorId, usuarioId);

            Map<String, Object> response = new HashMap<>();
            response.put("mensagem", "Colaborador removido com sucesso");
            response.put("lista", criarMapaLista(lista));

            return ResponseEntity.ok(response);
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

    /**
     * Recupera o id do usuário atual.
     * Tenta primeiro pelo SecurityContext (principal autenticado). Se não houver
     * usuário autenticado, faz fallback para a sessão HTTP (atributo "usuarioLogado")
     * para compatibilidade com o fluxo web que usa sessão.
     */
    private Long getCurrentUserId() {
        // tentar pelo SecurityContext
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getName())) {
            String email = authentication.getName();
            Usuario usuario = usuarioService.buscarPorEmail(email).orElse(null);
            if (usuario != null) return usuario.getId();
        }

        // fallback para sessão HTTP (fluxo do controller web)
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            HttpServletRequest request = attrs.getRequest();
            HttpSession session = request.getSession(false);
            if (session != null) {
                Usuario usuarioSess = (Usuario) session.getAttribute("usuarioLogado");
                if (usuarioSess != null) return usuarioSess.getId();
            }
        }

        throw new RuntimeException("Usuário não encontrado");
    }

    private Map<String, Object> criarMapaLista(Lista lista) {
        Map<String, Object> mapa = new HashMap<>();
        mapa.put("id", lista.getId());
        mapa.put("titulo", lista.getTitulo());
        mapa.put("descricao", lista.getDescricao());
        mapa.put("dataCriacao", lista.getDataCriacao());
        mapa.put("dataAtualizacao", lista.getDataAtualizacao());
        
        Map<String, Object> proprietario = new HashMap<>();
        proprietario.put("id", lista.getProprietario().getId());
        proprietario.put("nome", lista.getProprietario().getNomeCompleto());
        proprietario.put("email", lista.getProprietario().getEmail());
        mapa.put("proprietario", proprietario);

        if (lista.getColaboradores() != null) {
            List<Map<String, Object>> colaboradores = lista.getColaboradores().stream()
                    .map(colaborador -> {
                        Map<String, Object> colabMap = new HashMap<>();
                        colabMap.put("id", colaborador.getId());
                        colabMap.put("nome", colaborador.getNomeCompleto());
                        colabMap.put("email", colaborador.getEmail());
                        return colabMap;
                    })
                    .toList();
            mapa.put("colaboradores", colaboradores);
        }

        return mapa;
    }
}