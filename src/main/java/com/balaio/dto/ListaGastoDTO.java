package com.balaio.dto;

import java.math.BigDecimal;

public class ListaGastoDTO {
    
    private Long listaId;
    private String titulo;
    private String descricao;
    private BigDecimal totalGasto;
    private int quantidadeItens;
    private int quantidadeItensComprados;
    
    public ListaGastoDTO() {
        this.totalGasto = BigDecimal.ZERO;
    }

    public ListaGastoDTO(Long listaId, String titulo, String descricao) {
        this.listaId = listaId;
        this.titulo = titulo;
        this.descricao = descricao;
        this.totalGasto = BigDecimal.ZERO;
    }

    // Getters e Setters
    public Long getListaId() {
        return listaId;
    }

    public void setListaId(Long listaId) {
        this.listaId = listaId;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public BigDecimal getTotalGasto() {
        return totalGasto;
    }

    public void setTotalGasto(BigDecimal totalGasto) {
        this.totalGasto = totalGasto;
    }

    public int getQuantidadeItens() {
        return quantidadeItens;
    }

    public void setQuantidadeItens(int quantidadeItens) {
        this.quantidadeItens = quantidadeItens;
    }

    public int getQuantidadeItensComprados() {
        return quantidadeItensComprados;
    }

    public void setQuantidadeItensComprados(int quantidadeItensComprados) {
        this.quantidadeItensComprados = quantidadeItensComprados;
    }
}
