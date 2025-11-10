package com.balaio.service;

import com.balaio.model.Item;
import com.balaio.model.Lista;
import com.balaio.repository.ItemRepository;
import com.balaio.repository.ListaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ItemService {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ListaRepository listaRepository;

    @Autowired
    private ListaService listaService;

    public Item criarItem(String nomeProduto, Integer quantidade, BigDecimal valor, Long listaId, Long usuarioId) {
        Lista lista = listaRepository.findById(listaId)
                .orElseThrow(() -> new RuntimeException("Lista não encontrada"));

        // Verificar se o usuário tem acesso à lista
        if (!listaService.usuarioTemAcesso(listaId, usuarioId)) {
            throw new RuntimeException("Usuário não tem acesso a esta lista");
        }

        Item item = new Item();
        item.setNomeProduto(nomeProduto);
        item.setQuantidade(quantidade);
        item.setValor(valor);
        item.setLista(lista);
        item.setStatus(Item.StatusItem.PENDENTE);
        item.setDataCriacao(LocalDateTime.now());
        item.setDataAtualizacao(LocalDateTime.now());

        return itemRepository.save(item);
    }

    public List<Item> listarItensDaLista(Long listaId, Long usuarioId) {
        // Verificar se o usuário tem acesso à lista
        if (!listaService.usuarioTemAcesso(listaId, usuarioId)) {
            throw new RuntimeException("Usuário não tem acesso a esta lista");
        }

        return itemRepository.findByListaIdOrderByDataCriacaoDesc(listaId);
    }

    public List<Item> listarItensPorStatus(Long listaId, Item.StatusItem status, Long usuarioId) {
        // Verificar se o usuário tem acesso à lista
        if (!listaService.usuarioTemAcesso(listaId, usuarioId)) {
            throw new RuntimeException("Usuário não tem acesso a esta lista");
        }

        return itemRepository.findByListaIdAndStatus(listaId, status);
    }

    public Optional<Item> buscarPorId(Long id) {
        return itemRepository.findById(id);
    }

    public Item atualizarItem(Long id, String nomeProduto, Integer quantidade, BigDecimal valor, Long usuarioId) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Item não encontrado"));

        // Verificar se o usuário tem acesso à lista do item
        if (!listaService.usuarioTemAcesso(item.getLista().getId(), usuarioId)) {
            throw new RuntimeException("Usuário não tem acesso a esta lista");
        }

        item.setNomeProduto(nomeProduto);
        item.setQuantidade(quantidade);
        item.setValor(valor);
        item.setDataAtualizacao(LocalDateTime.now());

        return itemRepository.save(item);
    }

    public Item marcarComoComprado(Long id, Long usuarioId) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Item não encontrado"));

        // Verificar se o usuário tem acesso à lista do item
        if (!listaService.usuarioTemAcesso(item.getLista().getId(), usuarioId)) {
            throw new RuntimeException("Usuário não tem acesso a esta lista");
        }

        item.setStatus(Item.StatusItem.COMPRADO);
        item.setDataAtualizacao(LocalDateTime.now());

        return itemRepository.save(item);
    }

    public Item marcarComoPendente(Long id, Long usuarioId) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Item não encontrado"));

        // Verificar se o usuário tem acesso à lista do item
        if (!listaService.usuarioTemAcesso(item.getLista().getId(), usuarioId)) {
            throw new RuntimeException("Usuário não tem acesso a esta lista");
        }

        item.setStatus(Item.StatusItem.PENDENTE);
        item.setDataAtualizacao(LocalDateTime.now());

        return itemRepository.save(item);
    }

    public void excluirItem(Long id, Long usuarioId) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Item não encontrado"));

        // Verificar se o usuário tem acesso à lista do item
        if (!listaService.usuarioTemAcesso(item.getLista().getId(), usuarioId)) {
            throw new RuntimeException("Usuário não tem acesso a esta lista");
        }

        itemRepository.delete(item);
    }

    public long contarItensPorStatus(Long listaId, Item.StatusItem status, Long usuarioId) {
        // Verificar se o usuário tem acesso à lista
        if (!listaService.usuarioTemAcesso(listaId, usuarioId)) {
            throw new RuntimeException("Usuário não tem acesso a esta lista");
        }

        return itemRepository.countByListaIdAndStatus(listaId, status);
    }
}