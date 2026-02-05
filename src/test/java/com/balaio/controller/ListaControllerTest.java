package com.balaio.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.balaio.model.Lista;
import com.balaio.model.Usuario;
import com.balaio.service.ListaService;
import com.balaio.service.UsuarioService;

@WebMvcTest(ListaController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("Testes do Controller de Lista")
class ListaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ListaService listaService;

    @MockBean
    private UsuarioService usuarioService;

    private Usuario usuario;
    private Lista lista;

    @BeforeEach
    void setup() {
        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNomeCompleto("João Silva");
        usuario.setEmail("joao@email.com");

        lista = new Lista();
        lista.setId(1L);
        lista.setTitulo("Lista de Compras");
        lista.setDescricao("Mercado do mês");
        lista.setProprietario(usuario);
        lista.setDataCriacao(LocalDateTime.now());
        lista.setDataAtualizacao(LocalDateTime.now());

        when(usuarioService.buscarPorEmail("joao@email.com"))
                .thenReturn(Optional.of(usuario));
    }

    // ========= LISTAGEM BÁSICA =========

    @Test
    @WithMockUser(username = "joao@email.com")
    @DisplayName("TC036 - Deve listar listas do usuário")
    void deveListarListas() throws Exception {
        when(listaService.listarListasDoUsuario(1L))
                .thenReturn(List.of(lista));

        mockMvc.perform(get("/api/listas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].titulo").value("Lista de Compras"));

        verify(listaService).listarListasDoUsuario(1L);
    }

    // ========= INCLUSÃO =========

    @Test
    @WithMockUser(username = "joao@email.com")
    @DisplayName("TC037 - Deve criar lista com sucesso")
    void deveCriarLista() throws Exception {
        String json = """
            {
              "titulo": "Nova Lista",
              "descricao": "Descrição teste"
            }
        """;

        when(listaService.criarLista(any(), any(), anyLong()))
                .thenReturn(lista);

        mockMvc.perform(post("/api/listas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.mensagem").value("Lista criada com sucesso"))
                .andExpect(jsonPath("$.lista.titulo").value("Lista de Compras"));

        verify(listaService).criarLista(any(), any(), anyLong());
    }

    // ========= EDIÇÃO =========

    @Test
    @WithMockUser(username = "joao@email.com")
    @DisplayName("TC038 - Deve atualizar lista com sucesso")
    void deveAtualizarLista() throws Exception {
        String json = """
            {
              "titulo": "Lista Atualizada",
              "descricao": "Nova descrição"
            }
        """;

        when(listaService.atualizarLista(anyLong(), any(), any(), anyLong()))
                .thenReturn(lista);

        mockMvc.perform(put("/api/listas/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensagem").value("Lista atualizada com sucesso"));

        verify(listaService).atualizarLista(anyLong(), any(), any(), anyLong());
    }

    // ========= REMOÇÃO =========

    @Test
    @WithMockUser(username = "joao@email.com")
    @DisplayName("TC039 - Deve excluir lista com sucesso")
    void deveExcluirLista() throws Exception {
        mockMvc.perform(delete("/api/listas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensagem").value("Lista excluída com sucesso"));

        verify(listaService).excluirLista(1L, 1L);
    }
}
