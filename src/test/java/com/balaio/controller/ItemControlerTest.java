package com.balaio.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.balaio.model.Item;
import com.balaio.model.Lista;
import com.balaio.model.Usuario;
import com.balaio.service.ItemService;
import com.balaio.service.UsuarioService;

@WebMvcTest(ItemController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("Testes do Controller de Item - Listagem Básica")
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemService itemService;

    @MockBean
    private UsuarioService usuarioService;

    @Test
    @WithMockUser(username = "joao@example.com")
    @DisplayName("TC035 - Deve listar itens da lista com sucesso")
    void deveListarItensDaListaComSucesso() throws Exception {
        // Arrange
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setEmail("joao@example.com");

        Lista lista = new Lista();
        lista.setId(10L);
        lista.setTitulo("Lista de Compras");

        Item item = new Item();
        item.setId(1L);
        item.setNomeProduto("Arroz");
        item.setQuantidade(2);
        item.setStatus(Item.StatusItem.PENDENTE);
        item.setDataCriacao(LocalDateTime.now());
        item.setDataAtualizacao(LocalDateTime.now());
        item.setLista(lista);

        when(usuarioService.buscarPorEmail("joao@example.com"))
                .thenReturn(Optional.of(usuario));

        when(itemService.listarItensDaLista(10L, 1L))
                .thenReturn(List.of(item));

        // Act & Assert
        mockMvc.perform(get("/api/listas/{listaId}/itens", 10L)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].nomeProduto").value("Arroz"))
                .andExpect(jsonPath("$[0].status").value("PENDENTE"))
                .andExpect(jsonPath("$[0].lista.id").value(10));

        verify(itemService).listarItensDaLista(10L, 1L);
    }
}
