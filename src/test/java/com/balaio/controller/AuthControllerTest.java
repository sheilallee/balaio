package com.balaio.controller;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.balaio.config.SecurityConfig;
import com.balaio.model.Usuario;
import com.balaio.service.UsuarioService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
@DisplayName("Testes do Controller de Autenticação - Sprint 03")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UsuarioService usuarioService;

    @MockBean
    private PasswordEncoder passwordEncoder;

    private Usuario usuario;
    private MockHttpSession session;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNomeCompleto("João Silva");
        usuario.setEmail("joao@example.com");
        usuario.setSenha("$2a$10$encodedPassword");
        usuario.setDataCriacao(LocalDateTime.now());
        usuario.setDataAtualizacao(LocalDateTime.now());

        session = new MockHttpSession();
    }

    // ========== TESTES DE LOGIN (CA-22037) ==========

    @Test
    @DisplayName("CA1, CA2, CA3, CA4, CA6 - Deve fazer login com sucesso")
    void deveFazerLoginComSucesso() throws Exception {
        // Arrange
        String loginJson = "{\"email\":\"joao@example.com\",\"senha\":\"Senha123\"}";
        
        when(usuarioService.buscarPorEmail("joao@example.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("Senha123", usuario.getSenha())).thenReturn(true);

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson)
                .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensagem").value("Login realizado com sucesso"))
                .andExpect(jsonPath("$.usuario.id").value(1))
                .andExpect(jsonPath("$.usuario.nome").value("João Silva"))
                .andExpect(jsonPath("$.usuario.email").value("joao@example.com"));

        verify(usuarioService).buscarPorEmail("joao@example.com");
        verify(passwordEncoder).matches("Senha123", usuario.getSenha());
    }

    @Test
    @DisplayName("CA5 - Deve retornar erro genérico quando email não existe")
    void deveRetornarErroQuandoEmailNaoExiste() throws Exception {
        // Arrange
        String loginJson = "{\"email\":\"naoexiste@example.com\",\"senha\":\"Senha123\"}";
        
        when(usuarioService.buscarPorEmail("naoexiste@example.com")).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.erro").value("Email ou senha inválidos"));

        verify(usuarioService).buscarPorEmail("naoexiste@example.com");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    @DisplayName("CA5 - Deve retornar erro genérico quando senha está incorreta")
    void deveRetornarErroQuandoSenhaIncorreta() throws Exception {
        // Arrange
        String loginJson = "{\"email\":\"joao@example.com\",\"senha\":\"SenhaErrada123\"}";
        
        when(usuarioService.buscarPorEmail("joao@example.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("SenhaErrada123", usuario.getSenha())).thenReturn(false);

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.erro").value("Email ou senha inválidos"));

        verify(usuarioService).buscarPorEmail("joao@example.com");
        verify(passwordEncoder).matches("SenhaErrada123", usuario.getSenha());
    }

    @Test
    @DisplayName("CA2 - Deve retornar erro de validação quando email está vazio")
    void deveRetornarErroQuandoEmailVazio() throws Exception {
        // Arrange
        String loginJson = "{\"email\":\"\",\"senha\":\"Senha123\"}";

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("CA2 - Deve retornar erro de validação quando senha está vazia")
    void deveRetornarErroQuandoSenhaVazia() throws Exception {
        // Arrange
        String loginJson = "{\"email\":\"joao@example.com\",\"senha\":\"\"}";

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("CA1 - Deve retornar erro quando email tem formato inválido")
    void deveRetornarErroQuandoEmailFormatoInvalido() throws Exception {
        // Arrange
        String loginJson = "{\"email\":\"email-invalido\",\"senha\":\"Senha123\"}";

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson))
                .andExpect(status().isBadRequest());
    }

    // ========== TESTES DE LOGOUT (CA-22045) ==========

    @Test
    @DisplayName("CA5, CA6 - Deve fazer logout com sucesso")
    void deveFazerLogoutComSucesso() throws Exception {
        // Arrange
        session.setAttribute("usuarioLogado", usuario);
        session.setAttribute("usuarioId", usuario.getId());
        session.setAttribute("usuarioEmail", usuario.getEmail());

        // Act & Assert
        mockMvc.perform(post("/api/auth/logout")
                .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensagem").value("Logout realizado com sucesso"));
    }

    @Test
    @WithMockUser(username = "joao@example.com")
    @DisplayName("CA3 - Deve verificar sessão ativa")
    void deveVerificarSessaoAtiva() throws Exception {
        // Arrange
        session.setAttribute("usuarioLogado", usuario);

        // Act & Assert
        mockMvc.perform(post("/api/auth/verificar-sessao")
                .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.autenticado").value(true))
                .andExpect(jsonPath("$.usuario.id").value(1))
                .andExpect(jsonPath("$.usuario.nome").value("João Silva"))
                .andExpect(jsonPath("$.usuario.email").value("joao@example.com"));
    }

    @Test
    @DisplayName("CA2 - Deve redirecionar para login quando sessão inválida (Spring Security)")
    void deveRedirecionarParaLoginQuandoSessaoInvalida() throws Exception {
        // Act & Assert - Spring Security redireciona para login ao invés de retornar 401
        mockMvc.perform(post("/api/auth/verificar-sessao"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", "http://localhost/balaio/login"));
    }
}
