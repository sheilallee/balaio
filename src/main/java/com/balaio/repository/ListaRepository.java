package com.balaio.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.balaio.model.Lista;
import com.balaio.model.Usuario;

@Repository
public interface ListaRepository extends JpaRepository<Lista, Long> {
    
    List<Lista> findByProprietario(Usuario proprietario);
    
    @Query("SELECT l FROM Lista l WHERE l.proprietario = :usuario OR :usuario MEMBER OF l.colaboradores")
    List<Lista> findListasAcessiveisPorUsuario(@Param("usuario") Usuario usuario);
    
    @Query("SELECT l FROM Lista l WHERE l.proprietario.id = :usuarioId")
    List<Lista> findByProprietarioId(@Param("usuarioId") Long usuarioId);
    
    @Query("SELECT l FROM Lista l JOIN l.colaboradores c WHERE c.id = :usuarioId")
    List<Lista> findListasCompartilhadasComUsuario(@Param("usuarioId") Long usuarioId);

    @Query("SELECT COUNT(DISTINCT l) FROM Lista l JOIN l.colaboradores c WHERE l.proprietario.id = :ownerId AND c.id <> :ownerId")
    long countListasCompartilhadasPorProprietario(@Param("ownerId") Long ownerId);
}