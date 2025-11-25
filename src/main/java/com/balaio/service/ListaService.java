package com.balaio.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.balaio.model.Item;
import com.balaio.model.Lista;
import com.balaio.model.Usuario;
import com.balaio.repository.ItemRepository;
import com.balaio.repository.ListaRepository;
import com.balaio.repository.UsuarioRepository;

@Service
@Transactional
public class ListaService {

    @Autowired
    private ListaRepository listaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ItemRepository itemRepository;

    public Lista criarLista(String titulo, String descricao, Long proprietarioId) {
        Usuario proprietario = usuarioRepository.findById(proprietarioId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Lista lista = new Lista();
        lista.setTitulo(titulo);
        lista.setDescricao(descricao);
        lista.setProprietario(proprietario);
        lista.setDataCriacao(LocalDateTime.now());
        lista.setDataAtualizacao(LocalDateTime.now());

        return listaRepository.save(lista);
    }

    public List<Lista> listarListasDoUsuario(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        return listaRepository.findListasAcessiveisPorUsuario(usuario);
    }

    public List<Lista> listarListasProprietario(Long usuarioId) {
        return listaRepository.findByProprietarioId(usuarioId);
    }

    public List<Lista> listarListasCompartilhadas(Long usuarioId) {
        return listaRepository.findListasCompartilhadasComUsuario(usuarioId);
    }

    public Optional<Lista> buscarPorId(Long id) {
        return listaRepository.findById(id);
    }

    public Lista atualizarLista(Long id, String titulo, String descricao, Long usuarioId) {
        Lista lista = listaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lista não encontrada"));

        // Verificar se o usuário tem permissão para editar
        if (!lista.getProprietario().getId().equals(usuarioId) &&
            !lista.getColaboradores().stream().anyMatch(u -> u.getId().equals(usuarioId))) {
            throw new RuntimeException("Usuário não tem permissão para editar esta lista");
        }

        lista.setTitulo(titulo);
        lista.setDescricao(descricao);
        lista.setDataAtualizacao(LocalDateTime.now());

        return listaRepository.save(lista);
    }

    public void excluirLista(Long id, Long usuarioId) {
        Lista lista = listaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lista não encontrada"));

        // Apenas o proprietário pode excluir a lista
        if (!lista.getProprietario().getId().equals(usuarioId)) {
            throw new RuntimeException("Apenas o proprietário pode excluir a lista");
        }

        listaRepository.delete(lista);
    }

    public Lista compartilharLista(Long listaId, String emailColaborador, Long proprietarioId) {
        Lista lista = listaRepository.findById(listaId)
                .orElseThrow(() -> new RuntimeException("Lista não encontrada"));

        // Verificar se o usuário é o proprietário
        if (!lista.getProprietario().getId().equals(proprietarioId)) {
            throw new RuntimeException("Apenas o proprietário pode compartilhar a lista");
        }

        if (emailColaborador == null || emailColaborador.trim().isEmpty()) {
            throw new RuntimeException("E-mail inválido");
        }

        String emailNormalized = emailColaborador.trim().toLowerCase();

        // Não permitir compartilhar com o próprio proprietário
        if (lista.getProprietario().getEmail() != null && lista.getProprietario().getEmail().equalsIgnoreCase(emailNormalized)) {
            throw new RuntimeException("Não é possível compartilhar a lista com o proprietário");
        }

        Usuario colaborador = usuarioRepository.findByEmail(emailNormalized)
                .orElseThrow(() -> new RuntimeException("Usuário colaborador não encontrado"));

        // Verificar se já não é colaborador
        if (lista.getColaboradores().stream().anyMatch(u -> u.getId().equals(colaborador.getId()))) {
            throw new RuntimeException("Lista já foi compartilhada para este e-mail");
        }

        lista.getColaboradores().add(colaborador);
        lista.setDataAtualizacao(LocalDateTime.now());

        return listaRepository.save(lista);
    }

    public Lista removerColaborador(Long listaId, Long colaboradorId, Long proprietarioId) {
        Lista lista = listaRepository.findById(listaId)
                .orElseThrow(() -> new RuntimeException("Lista não encontrada"));

        // Verificar se o usuário é o proprietário
        if (!lista.getProprietario().getId().equals(proprietarioId)) {
            throw new RuntimeException("Apenas o proprietário pode remover colaboradores");
        }

        Usuario colaborador = usuarioRepository.findById(colaboradorId)
                .orElseThrow(() -> new RuntimeException("Colaborador não encontrado"));

        lista.getColaboradores().remove(colaborador);
        lista.setDataAtualizacao(LocalDateTime.now());

        return listaRepository.save(lista);
    }

    public Lista removerColaboradorPorEmail(Long listaId, String emailColaborador, Long proprietarioId) {
        Lista lista = listaRepository.findById(listaId)
                .orElseThrow(() -> new RuntimeException("Lista não encontrada"));

        // Verificar se o usuário é o proprietário
        if (!lista.getProprietario().getId().equals(proprietarioId)) {
            throw new RuntimeException("Apenas o proprietário pode remover colaboradores");
        }

        String emailNormalized = emailColaborador.trim().toLowerCase();
        
        Usuario colaborador = lista.getColaboradores().stream()
                .filter(u -> u.getEmail().equalsIgnoreCase(emailNormalized))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Colaborador não encontrado nesta lista"));

        lista.getColaboradores().remove(colaborador);
        lista.setDataAtualizacao(LocalDateTime.now());

        return listaRepository.save(lista);
    }


    public boolean usuarioTemAcesso(Long listaId, Long usuarioId) {
        Lista lista = listaRepository.findById(listaId)
                .orElseThrow(() -> new RuntimeException("Lista não encontrada"));

        return lista.getProprietario().getId().equals(usuarioId) ||
               lista.getColaboradores().stream().anyMatch(u -> u.getId().equals(usuarioId));
    }

    public long contarListasCompartilhadasPorProprietario(Long proprietarioId) {
        return listaRepository.countListasCompartilhadasPorProprietario(proprietarioId);
    }

    public List<Item> listarItensDaLista(Long listaId) {
        Lista lista = listaRepository.findById(listaId)
                .orElseThrow(() -> new RuntimeException("Lista não encontrada"));

        return itemRepository.findByLista(lista);
    }
}