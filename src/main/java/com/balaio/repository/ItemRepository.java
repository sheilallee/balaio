package com.balaio.repository;

import com.balaio.model.Item;
import com.balaio.model.Lista;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    
    List<Item> findByLista(Lista lista);
    
    List<Item> findByListaId(Long listaId);
    
    @Query("SELECT i FROM Item i WHERE i.lista.id = :listaId AND i.status = :status")
    List<Item> findByListaIdAndStatus(@Param("listaId") Long listaId, @Param("status") Item.StatusItem status);
    
    @Query("SELECT i FROM Item i WHERE i.lista.id = :listaId ORDER BY i.dataCriacao DESC")
    List<Item> findByListaIdOrderByDataCriacaoDesc(@Param("listaId") Long listaId);
    
    long countByListaIdAndStatus(Long listaId, Item.StatusItem status);
}