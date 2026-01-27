package com.balaio.service;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.balaio.dto.UsuarioCadastroDTO;
import com.balaio.model.Usuario;
import com.balaio.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do Serviço de Usuário - Sprint 03")
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    private UsuarioCadastroDTO cadastroDTO;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        cadastroDTO = new UsuarioCadastroDTO();
        cadastroDTO.setNomeCompleto("João Silva");
        cadastroDTO.setEmail("joao@example.com");
        cadastroDTO.setSenha("Senha123");
        cadastroDTO.setConfirmarSenha("Senha123");

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
    @DisplayName("CA1, CA2, CA3, CA6, CA8 - Deve cadastrar usuário com dados válidos")
    void deveCadastrarUsuarioComDadosValidos() {
        // Arrange
        when(usuarioRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$encodedPassword");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        // Act
        Usuario resultado = usuarioService.cadastrarUsuario(cadastroDTO);

        // Assert
        assertNotNull(resultado);
        assertEquals("João Silva", resultado.getNomeCompleto());
        assertEquals("joao@example.com", resultado.getEmail());
        verify(usuarioRepository).existsByEmail("joao@example.com");
        verify(passwordEncoder).encode("Senha123");
        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    @DisplayName("CA4, CA7 - Deve lançar exceção quando email já está cadastrado")
    void deveLancarExcecaoQuandoEmailJaCadastrado() {
        // Arrange
        when(usuarioRepository.existsByEmail(anyString())).thenReturn(true);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            usuarioService.cadastrarUsuario(cadastroDTO);
        });

        assertEquals("E-mail já cadastrado", exception.getMessage());
        verify(usuarioRepository).existsByEmail("joao@example.com");
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    @DisplayName("CA5 - Deve lançar exceção quando senhas não coincidem")
    void deveLancarExcecaoQuandoSenhasNaoCoincidem() {
        // Arrange
        cadastroDTO.setConfirmarSenha("SenhaErrada123");
        when(usuarioRepository.existsByEmail(anyString())).thenReturn(false);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            usuarioService.cadastrarUsuario(cadastroDTO);
        });

        assertEquals("Senhas não coincidem", exception.getMessage());
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    // ========== TESTES DE EDIÇÃO (CA-21994) ==========

    @Test
    @DisplayName("CA2, CA3, CA4 - Deve atualizar usuário com dados válidos")
    void deveAtualizarUsuarioComDadosValidos() {
        // Arrange
        Usuario usuarioAtualizado = new Usuario();
        usuarioAtualizado.setNomeCompleto("João da Silva");
        usuarioAtualizado.setEmail("joao.silva@example.com");

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.findByEmail("joao.silva@example.com")).thenReturn(Optional.empty());
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        // Act
        Usuario resultado = usuarioService.atualizarUsuario(1L, usuarioAtualizado);

        // Assert
        assertNotNull(resultado);
        verify(usuarioRepository).findById(1L);
        verify(usuarioRepository).findByEmail("joao.silva@example.com");
        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    @DisplayName("CA2 - Deve lançar exceção quando nome está vazio")
    void deveLancarExcecaoQuandoNomeVazio() {
        // Arrange
        Usuario usuarioAtualizado = new Usuario();
        usuarioAtualizado.setNomeCompleto("");
        usuarioAtualizado.setEmail("joao@example.com");

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            usuarioService.atualizarUsuario(1L, usuarioAtualizado);
        });

        assertEquals("Nome é obrigatório", exception.getMessage());
    }

    @Test
    @DisplayName("CA2 - Deve lançar exceção quando nome tem menos de 2 caracteres")
    void deveLancarExcecaoQuandoNomeMuitoCurto() {
        // Arrange
        Usuario usuarioAtualizado = new Usuario();
        usuarioAtualizado.setNomeCompleto("A");
        usuarioAtualizado.setEmail("joao@example.com");

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            usuarioService.atualizarUsuario(1L, usuarioAtualizado);
        });

        assertEquals("Nome deve ter entre 2 e 100 caracteres", exception.getMessage());
    }

    @Test
    @DisplayName("CA2 - Deve lançar exceção quando nome tem mais de 100 caracteres")
    void deveLancarExcecaoQuandoNomeMuitoLongo() {
        // Arrange
        Usuario usuarioAtualizado = new Usuario();
        usuarioAtualizado.setNomeCompleto("A".repeat(101));
        usuarioAtualizado.setEmail("joao@example.com");

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            usuarioService.atualizarUsuario(1L, usuarioAtualizado);
        });

        assertEquals("Nome deve ter entre 2 e 100 caracteres", exception.getMessage());
    }

    @Test
    @DisplayName("CA3 - Deve lançar exceção quando email tem formato inválido")
    void deveLancarExcecaoQuandoEmailFormatoInvalido() {
        // Arrange
        Usuario usuarioAtualizado = new Usuario();
        usuarioAtualizado.setNomeCompleto("João Silva");
        usuarioAtualizado.setEmail("email-invalido");

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            usuarioService.atualizarUsuario(1L, usuarioAtualizado);
        });

        assertEquals("E-mail deve ter um formato válido", exception.getMessage());
    }

    @Test
    @DisplayName("CA6 - Deve lançar exceção quando email já está em uso por outro usuário")
    void deveLancarExcecaoQuandoEmailJaEmUso() {
        // Arrange
        Usuario usuarioAtualizado = new Usuario();
        usuarioAtualizado.setNomeCompleto("João Silva");
        usuarioAtualizado.setEmail("outro@example.com");

        Usuario outroUsuario = new Usuario();
        outroUsuario.setId(2L);
        outroUsuario.setEmail("outro@example.com");

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.findByEmail("outro@example.com")).thenReturn(Optional.of(outroUsuario));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            usuarioService.atualizarUsuario(1L, usuarioAtualizado);
        });

        assertEquals("E-mail já está em uso", exception.getMessage());
    }

    @Test
    @DisplayName("CA6 - Deve permitir atualizar com mesmo email do próprio usuário")
    void devePermitirAtualizarComMesmoEmail() {
        // Arrange
        Usuario usuarioAtualizado = new Usuario();
        usuarioAtualizado.setNomeCompleto("João da Silva");
        usuarioAtualizado.setEmail("joao@example.com"); // Mesmo email

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.findByEmail("joao@example.com")).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        // Act
        Usuario resultado = usuarioService.atualizarUsuario(1L, usuarioAtualizado);

        // Assert
        assertNotNull(resultado);
        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando usuário não encontrado")
    void deveLancarExcecaoQuandoUsuarioNaoEncontrado() {
        // Arrange
        Usuario usuarioAtualizado = new Usuario();
        usuarioAtualizado.setNomeCompleto("João Silva");
        usuarioAtualizado.setEmail("joao@example.com");

        when(usuarioRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            usuarioService.atualizarUsuario(999L, usuarioAtualizado);
        });

        assertEquals("Usuário não encontrado", exception.getMessage());
    }

    @Test
    @DisplayName("Deve buscar usuário por email")
    void deveBuscarUsuarioPorEmail() {
        // Arrange
        when(usuarioRepository.findByEmail("joao@example.com")).thenReturn(Optional.of(usuario));

        // Act
        Optional<Usuario> resultado = usuarioService.buscarPorEmail("joao@example.com");

        // Assert
        assertTrue(resultado.isPresent());
        assertEquals("joao@example.com", resultado.get().getEmail());
        verify(usuarioRepository).findByEmail("joao@example.com");
    }
}
