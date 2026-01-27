package com.balaio.dto;

public class ListaResumoDTO {
    private Long id;
    private String titulo;
    private String descricao;
    private int totalItens;
    private int totalComprados;
    private boolean isProprietario;

    public ListaResumoDTO() {}

    public ListaResumoDTO(Long id, String titulo, String descricao, int totalItens, int totalComprados) {
        this.id = id;
        this.titulo = titulo;
        this.descricao = descricao;
        this.totalItens = totalItens;
        this.totalComprados = totalComprados;
        this.isProprietario = false;
    }

    public ListaResumoDTO(Long id, String titulo, String descricao, int totalItens, int totalComprados, boolean isProprietario) {
        this.id = id;
        this.titulo = titulo;
        this.descricao = descricao;
        this.totalItens = totalItens;
        this.totalComprados = totalComprados;
        this.isProprietario = isProprietario;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public int getTotalItens() { return totalItens; }
    public void setTotalItens(int totalItens) { this.totalItens = totalItens; }
    public int getTotalComprados() { return totalComprados; }
    public void setTotalComprados(int totalComprados) { this.totalComprados = totalComprados; }
    public boolean isProprietario() { return isProprietario; }
    public void setProprietario(boolean proprietario) { isProprietario = proprietario; }
}