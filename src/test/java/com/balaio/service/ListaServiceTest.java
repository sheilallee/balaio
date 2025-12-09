package com.balaio.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.balaio.model.Lista;
import com.balaio.model.Usuario;
import com.balaio.repository.ItemRepository;
import com.balaio.repository.ListaRepository;
import com.balaio.repository.UsuarioRepository;

//Testes unitários para ListaService
//Funcionalidade de inclusão e remoção de colaboradores
 
@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários - ListaService (Colaboradores)")
class ListaServiceTest {

    @Mock
    private ListaRepository listaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private ListaService listaService;

    private Usuario proprietario;
    private Usuario colaborador;
    private Lista lista;

    @BeforeEach
    void setUp() {
        // Criar proprietário
        proprietario = new Usuario();
        proprietario.setId(1L);
        proprietario.setNomeCompleto("João Silva");
        proprietario.setEmail("joao@email.com");

        // Criar colaborador
        colaborador = new Usuario();
        colaborador.setId(2L);
        colaborador.setNomeCompleto("Maria Santos");
        colaborador.setEmail("maria@email.com");

        // Criar lista
        lista = new Lista();
        lista.setId(1L);
        lista.setTitulo("Lista de Compras");
        lista.setDescricao("Supermercado semanal");
        lista.setProprietario(proprietario);
        lista.setColaboradores(new ArrayList<>());
        lista.setDataCriacao(LocalDateTime.now());
        lista.setDataAtualizacao(LocalDateTime.now());
    }

    // TESTES DE COMPARTILHAR LISTA

    @Test
    @DisplayName("TC001 - Deve compartilhar lista com sucesso")
    void testCompartilharLista_Sucesso() {
        // Arrange
        when(listaRepository.findById(1L)).thenReturn(Optional.of(lista));
        when(usuarioRepository.findByEmail("maria@email.com")).thenReturn(Optional.of(colaborador));
        when(listaRepository.save(any(Lista.class))).thenReturn(lista);

        // Act
        Lista resultado = listaService.compartilharLista(1L, "maria@email.com", 1L);

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.getColaboradores().contains(colaborador));
        verify(listaRepository, times(1)).save(any(Lista.class));
    }

    @Test
    @DisplayName("TC002 - Deve falhar ao compartilhar com email inválido (null)")
    void testCompartilharLista_EmailNull() {
        // Arrange
        when(listaRepository.findById(1L)).thenReturn(Optional.of(lista));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            listaService.compartilharLista(1L, null, 1L);
        });

        assertEquals("E-mail inválido", exception.getMessage());
        verify(listaRepository, never()).save(any(Lista.class));
    }

    @Test
    @DisplayName("TC003 - Deve falhar ao compartilhar com email vazio")
    void testCompartilharLista_EmailVazio() {
        // Arrange
        when(listaRepository.findById(1L)).thenReturn(Optional.of(lista));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            listaService.compartilharLista(1L, "   ", 1L);
        });

        assertEquals("E-mail inválido", exception.getMessage());
        verify(listaRepository, never()).save(any(Lista.class));
    }

    @Test
    @DisplayName("TC004 - Deve falhar ao compartilhar com usuário não encontrado")
    void testCompartilharLista_UsuarioNaoEncontrado() {
        // Arrange
        when(listaRepository.findById(1L)).thenReturn(Optional.of(lista));
        when(usuarioRepository.findByEmail("inexistente@email.com")).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            listaService.compartilharLista(1L, "inexistente@email.com", 1L);
        });

        assertEquals("Usuário colaborador não encontrado", exception.getMessage());
        verify(listaRepository, never()).save(any(Lista.class));
    }

    @Test
    @DisplayName("TC005 - Deve falhar ao compartilhar com o próprio proprietário")
    void testCompartilharLista_ProprioProprietario() {
        // Arrange
        when(listaRepository.findById(1L)).thenReturn(Optional.of(lista));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            listaService.compartilharLista(1L, "joao@email.com", 1L);
        });

        assertEquals("Não é possível compartilhar a lista com o proprietário", exception.getMessage());
        verify(listaRepository, never()).save(any(Lista.class));
    }

    @Test
    @DisplayName("TC006 - Deve falhar ao compartilhar com colaborador já existente")
    void testCompartilharLista_ColaboradorDuplicado() {
        // Arrange
        lista.getColaboradores().add(colaborador);
        when(listaRepository.findById(1L)).thenReturn(Optional.of(lista));
        when(usuarioRepository.findByEmail("maria@email.com")).thenReturn(Optional.of(colaborador));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            listaService.compartilharLista(1L, "maria@email.com", 1L);
        });

        assertEquals("Lista já foi compartilhada para este e-mail", exception.getMessage());
        verify(listaRepository, never()).save(any(Lista.class));
    }

    @Test
    @DisplayName("TC007 - Deve falhar ao compartilhar se não for proprietário")
    void testCompartilharLista_SemPermissao() {
        // Arrange
        when(listaRepository.findById(1L)).thenReturn(Optional.of(lista));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            listaService.compartilharLista(1L, "maria@email.com", 999L);
        });

        assertEquals("Apenas o proprietário pode compartilhar a lista", exception.getMessage());
        verify(listaRepository, never()).save(any(Lista.class));
    }

    @Test
    @DisplayName("TC008 - Deve normalizar email ao compartilhar (case insensitive)")
    void testCompartilharLista_EmailCaseInsensitive() {
        // Arrange
        when(listaRepository.findById(1L)).thenReturn(Optional.of(lista));
        when(usuarioRepository.findByEmail("maria@email.com")).thenReturn(Optional.of(colaborador));
        when(listaRepository.save(any(Lista.class))).thenReturn(lista);

        // Act
        Lista resultado = listaService.compartilharLista(1L, "MARIA@EMAIL.COM", 1L);

        // Assert
        assertNotNull(resultado);
        verify(usuarioRepository).findByEmail("maria@email.com");
        verify(listaRepository, times(1)).save(any(Lista.class));
    }

    // TESTES DE REMOVER COLABORADOR 

    @Test
    @DisplayName("TC009 - Deve remover colaborador com sucesso por ID")
    void testRemoverColaborador_Sucesso() {
        // Arrange
        lista.getColaboradores().add(colaborador);
        when(listaRepository.findById(1L)).thenReturn(Optional.of(lista));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(colaborador));
        when(listaRepository.save(any(Lista.class))).thenReturn(lista);

        // Act
        Lista resultado = listaService.removerColaborador(1L, 2L, 1L);

        // Assert
        assertNotNull(resultado);
        assertFalse(resultado.getColaboradores().contains(colaborador));
        verify(listaRepository, times(1)).save(any(Lista.class));
    }

    @Test
    @DisplayName("TC010 - Deve falhar ao remover colaborador se não for proprietário")
    void testRemoverColaborador_SemPermissao() {
        // Arrange
        lista.getColaboradores().add(colaborador);
        when(listaRepository.findById(1L)).thenReturn(Optional.of(lista));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            listaService.removerColaborador(1L, 2L, 999L);
        });

        assertEquals("Apenas o proprietário pode remover colaboradores", exception.getMessage());
        verify(listaRepository, never()).save(any(Lista.class));
    }

    @Test
    @DisplayName("TC011 - Deve falhar ao remover colaborador inexistente")
    void testRemoverColaborador_ColaboradorNaoEncontrado() {
        // Arrange
        when(listaRepository.findById(1L)).thenReturn(Optional.of(lista));
        when(usuarioRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            listaService.removerColaborador(1L, 999L, 1L);
        });

        assertEquals("Colaborador não encontrado", exception.getMessage());
        verify(listaRepository, never()).save(any(Lista.class));
    }

    @Test
    @DisplayName("TC012 - Deve remover colaborador com sucesso por email")
    void testRemoverColaboradorPorEmail_Sucesso() {
        // Arrange
        lista.getColaboradores().add(colaborador);
        when(listaRepository.findById(1L)).thenReturn(Optional.of(lista));
        when(listaRepository.save(any(Lista.class))).thenReturn(lista);

        // Act
        Lista resultado = listaService.removerColaboradorPorEmail(1L, "maria@email.com", 1L);

        // Assert
        assertNotNull(resultado);
        assertFalse(resultado.getColaboradores().contains(colaborador));
        verify(listaRepository, times(1)).save(any(Lista.class));
    }

    @Test
    @DisplayName("TC013 - Deve falhar ao remover por email não presente na lista")
    void testRemoverColaboradorPorEmail_EmailNaoNaLista() {
        // Arrange
        when(listaRepository.findById(1L)).thenReturn(Optional.of(lista));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            listaService.removerColaboradorPorEmail(1L, "ausente@email.com", 1L);
        });

        assertEquals("Colaborador não encontrado nesta lista", exception.getMessage());
        verify(listaRepository, never()).save(any(Lista.class));
    }

    @Test
    @DisplayName("TC014 - Deve falhar ao remover por email se não for proprietário")
    void testRemoverColaboradorPorEmail_SemPermissao() {
        // Arrange
        lista.getColaboradores().add(colaborador);
        when(listaRepository.findById(1L)).thenReturn(Optional.of(lista));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            listaService.removerColaboradorPorEmail(1L, "maria@email.com", 999L);
        });

        assertEquals("Apenas o proprietário pode remover colaboradores", exception.getMessage());
        verify(listaRepository, never()).save(any(Lista.class));
    }

    // TESTES DE ACESSO 

    @Test
    @DisplayName("TC015 - Proprietário deve ter acesso à lista")
    void testUsuarioTemAcesso_Proprietario() {
        // Arrange
        when(listaRepository.findById(1L)).thenReturn(Optional.of(lista));

        // Act
        boolean temAcesso = listaService.usuarioTemAcesso(1L, 1L);

        // Assert
        assertTrue(temAcesso);
    }

    @Test
    @DisplayName("TC016 - Colaborador deve ter acesso à lista")
    void testUsuarioTemAcesso_Colaborador() {
        // Arrange
        lista.getColaboradores().add(colaborador);
        when(listaRepository.findById(1L)).thenReturn(Optional.of(lista));

        // Act
        boolean temAcesso = listaService.usuarioTemAcesso(1L, 2L);

        // Assert
        assertTrue(temAcesso);
    }

    @Test
    @DisplayName("TC017 - Usuário sem relação não deve ter acesso à lista")
    void testUsuarioTemAcesso_SemAcesso() {
        // Arrange
        when(listaRepository.findById(1L)).thenReturn(Optional.of(lista));

        // Act
        boolean temAcesso = listaService.usuarioTemAcesso(1L, 999L);

        // Assert
        assertFalse(temAcesso);
    }
}
