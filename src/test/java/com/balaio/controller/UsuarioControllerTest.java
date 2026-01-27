package com.balaio.controller;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.balaio.config.SecurityConfig;
import com.balaio.model.Usuario;
import com.balaio.service.UsuarioService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(UsuarioController.class)
@Import(SecurityConfig.class)
@DisplayName("Testes do Controller de Usuário - Sprint 03")
class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UsuarioService usuarioService;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNomeCompleto("João Silva");
        usuario.setEmail("joao@example.com");
        usuario.setSenha("$2a$10$encodedPassword");
        usuario.setDataCriacao(LocalDateTime.now());
        usuario.setDataAtualizacao(LocalDateTime.now());
    }

    // ========== TESTES DE CADASTRO (CA-22024) ==========

    @Test
    @DisplayName("CA8 - Deve cadastrar usuário com sucesso e retornar mensagem")
    void deveCadastrarUsuarioComSucesso() throws Exception {
        // Arrange
        String cadastroJson = "{"
                + "\"nomeCompleto\":\"João Silva\","
                + "\"email\":\"joao@example.com\","
                + "\"senha\":\"Senha123\","
                + "\"confirmarSenha\":\"Senha123\""
                + "}";

        when(usuarioService.cadastrarUsuario(any())).thenReturn(usuario);

        // Act & Assert
        mockMvc.perform(post("/api/usuarios/cadastro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(cadastroJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.mensagem").value("Usuário cadastrado com sucesso"))
                .andExpect(jsonPath("$.usuario.id").value(1))
                .andExpect(jsonPath("$.usuario.nome").value("João Silva"))
                .andExpect(jsonPath("$.usuario.email").value("joao@example.com"));

        verify(usuarioService).cadastrarUsuario(any());
    }

    @Test
    @DisplayName("CA2 - Deve retornar erro quando nome está vazio")
    void deveRetornarErroQuandoNomeVazio() throws Exception {
        // Arrange
        String cadastroJson = "{"
                + "\"nomeCompleto\":\"\","
                + "\"email\":\"joao@example.com\","
                + "\"senha\":\"Senha123\","
                + "\"confirmarSenha\":\"Senha123\""
                + "}";

        // Act & Assert
        mockMvc.perform(post("/api/usuarios/cadastro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(cadastroJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("CA3 - Deve retornar erro quando nome tem menos de 2 caracteres")
    void deveRetornarErroQuandoNomeMuitoCurto() throws Exception {
        // Arrange
        String cadastroJson = "{"
                + "\"nomeCompleto\":\"A\","
                + "\"email\":\"joao@example.com\","
                + "\"senha\":\"Senha123\","
                + "\"confirmarSenha\":\"Senha123\""
                + "}";

        // Act & Assert
        mockMvc.perform(post("/api/usuarios/cadastro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(cadastroJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("CA4 - Deve retornar erro quando email tem formato inválido")
    void deveRetornarErroQuandoEmailInvalido() throws Exception {
        // Arrange
        String cadastroJson = "{"
                + "\"nomeCompleto\":\"João Silva\","
                + "\"email\":\"email-invalido\","
                + "\"senha\":\"Senha123\","
                + "\"confirmarSenha\":\"Senha123\""
                + "}";

        // Act & Assert
        mockMvc.perform(post("/api/usuarios/cadastro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(cadastroJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("CA6 - Deve retornar erro quando senha tem menos de 8 caracteres")
    void deveRetornarErroQuandoSenhaMuitoCurta() throws Exception {
        // Arrange
        String cadastroJson = "{"
                + "\"nomeCompleto\":\"João Silva\","
                + "\"email\":\"joao@example.com\","
                + "\"senha\":\"Sen123\","
                + "\"confirmarSenha\":\"Sen123\""
                + "}";

        // Act & Assert
        mockMvc.perform(post("/api/usuarios/cadastro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(cadastroJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("CA6 - Deve retornar erro quando senha não tem letra e número")
    void deveRetornarErroQuandoSenhaSemLetraOuNumero() throws Exception {
        // Arrange
        String cadastroJson = "{"
                + "\"nomeCompleto\":\"João Silva\","
                + "\"email\":\"joao@example.com\","
                + "\"senha\":\"senhasenha\","
                + "\"confirmarSenha\":\"senhasenha\""
                + "}";

        // Act & Assert
        mockMvc.perform(post("/api/usuarios/cadastro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(cadastroJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("CA7 - Deve retornar erro quando email já está cadastrado")
    void deveRetornarErroQuandoEmailJaCadastrado() throws Exception {
        // Arrange
        String cadastroJson = "{"
                + "\"nomeCompleto\":\"João Silva\","
                + "\"email\":\"joao@example.com\","
                + "\"senha\":\"Senha123\","
                + "\"confirmarSenha\":\"Senha123\""
                + "}";

        when(usuarioService.cadastrarUsuario(any()))
                .thenThrow(new RuntimeException("E-mail já cadastrado"));

        // Act & Assert
        mockMvc.perform(post("/api/usuarios/cadastro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(cadastroJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("E-mail já cadastrado"));
    }

    // ========== TESTES DE EDIÇÃO (CA-21994) ==========

    @Test
    @WithMockUser(username = "joao@example.com")
    @DisplayName("CA1 - Deve obter perfil do usuário logado")
    void deveObterPerfilUsuario() throws Exception {
        // Arrange
        when(usuarioService.buscarPorEmail("joao@example.com")).thenReturn(Optional.of(usuario));

        // Act & Assert
        mockMvc.perform(get("/api/usuarios/perfil"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("João Silva"))
                .andExpect(jsonPath("$.email").value("joao@example.com"));

        verify(usuarioService).buscarPorEmail("joao@example.com");
    }

    @Test
    @WithMockUser(username = "joao@example.com")
    @DisplayName("CA4 - Deve atualizar perfil com sucesso e retornar mensagem")
    void deveAtualizarPerfilComSucesso() throws Exception {
        // Arrange
        String atualizacaoJson = "{"
                + "\"nome\":\"João da Silva\","
                + "\"email\":\"joao.silva@example.com\""
                + "}";

        Usuario usuarioAtualizado = new Usuario();
        usuarioAtualizado.setId(1L);
        usuarioAtualizado.setNomeCompleto("João da Silva");
        usuarioAtualizado.setEmail("joao.silva@example.com");

        when(usuarioService.buscarPorEmail("joao@example.com")).thenReturn(Optional.of(usuario));
        when(usuarioService.atualizarUsuario(anyLong(), any())).thenReturn(usuarioAtualizado);

        // Act & Assert
        mockMvc.perform(put("/api/usuarios/perfil")
                .contentType(MediaType.APPLICATION_JSON)
                .content(atualizacaoJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensagem").value("Perfil atualizado com sucesso"))
                .andExpect(jsonPath("$.usuario.nome").value("João da Silva"))
                .andExpect(jsonPath("$.usuario.email").value("joao.silva@example.com"));

        verify(usuarioService).atualizarUsuario(anyLong(), any());
    }

    @Test
    @WithMockUser(username = "joao@example.com")
    @DisplayName("CA2 - Deve retornar erro quando nome não é fornecido na atualização")
    void deveRetornarErroQuandoNomeNaoFornecido() throws Exception {
        // Arrange
        String atualizacaoJson = "{"
                + "\"email\":\"joao.silva@example.com\""
                + "}";

        when(usuarioService.buscarPorEmail("joao@example.com")).thenReturn(Optional.of(usuario));

        // Act & Assert
        mockMvc.perform(put("/api/usuarios/perfil")
                .contentType(MediaType.APPLICATION_JSON)
                .content(atualizacaoJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Nome e email são obrigatórios"));
    }

    @Test
    @WithMockUser(username = "joao@example.com")
    @DisplayName("CA6 - Deve retornar erro quando email já está em uso")
    void deveRetornarErroQuandoEmailJaEmUso() throws Exception {
        // Arrange
        String atualizacaoJson = "{"
                + "\"nome\":\"João Silva\","
                + "\"email\":\"outro@example.com\""
                + "}";

        when(usuarioService.buscarPorEmail("joao@example.com")).thenReturn(Optional.of(usuario));
        when(usuarioService.atualizarUsuario(anyLong(), any()))
                .thenThrow(new RuntimeException("E-mail já está em uso"));

        // Act & Assert
        mockMvc.perform(put("/api/usuarios/perfil")
                .contentType(MediaType.APPLICATION_JSON)
                .content(atualizacaoJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("E-mail já está em uso"));
    }

    // ========== TESTES DE PROTEÇÃO DE ROTAS (CA-22045) ==========

    @Test
    @DisplayName("CA1, CA2 - Rotas protegidas devem redirecionar para login (Spring Security)")
    void rotasProtegidasDevemRedirecionarParaLogin() throws Exception {
        // Act & Assert - GET perfil sem autenticação (Spring Security redireciona)
        mockMvc.perform(get("/api/usuarios/perfil"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", "http://localhost/balaio/login"));

        // Act & Assert - PUT perfil sem autenticação (Spring Security redireciona)
        mockMvc.perform(put("/api/usuarios/perfil")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"João\",\"email\":\"joao@example.com\"}"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @DisplayName("CA1 - Rota de cadastro deve ser pública")
    void rotaCadastroDeveSerPublica() throws Exception {
        // Arrange
        String cadastroJson = "{"
                + "\"nomeCompleto\":\"João Silva\","
                + "\"email\":\"joao@example.com\","
                + "\"senha\":\"Senha123\","
                + "\"confirmarSenha\":\"Senha123\""
                + "}";

        when(usuarioService.cadastrarUsuario(any())).thenReturn(usuario);

        // Act & Assert
        mockMvc.perform(post("/api/usuarios/cadastro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(cadastroJson))
                .andExpect(status().isCreated());
    }
}
