package com.balaio.dto;

import java.math.BigDecimal;
import java.util.List;

public class DashboardDTO {
    
    private int quantidadeListas;
    private BigDecimal totalGastoGeral;
    private List<ListaGastoDTO> listasComGastos;
    private int totalItens;
    private int totalItensComprados;
    
    public DashboardDTO() {
        this.totalGastoGeral = BigDecimal.ZERO;
    }

    // Getters e Setters
    public int getQuantidadeListas() {
        return quantidadeListas;
    }

    public void setQuantidadeListas(int quantidadeListas) {
        this.quantidadeListas = quantidadeListas;
    }

    public BigDecimal getTotalGastoGeral() {
        return totalGastoGeral;
    }

    public void setTotalGastoGeral(BigDecimal totalGastoGeral) {
        this.totalGastoGeral = totalGastoGeral;
    }

    public List<ListaGastoDTO> getListasComGastos() {
        return listasComGastos;
    }

    public void setListasComGastos(List<ListaGastoDTO> listasComGastos) {
        this.listasComGastos = listasComGastos;
    }

    public int getTotalItens() {
        return totalItens;
    }

    public void setTotalItens(int totalItens) {
        this.totalItens = totalItens;
    }

    public int getTotalItensComprados() {
        return totalItensComprados;
    }

    public void setTotalItensComprados(int totalItensComprados) {
        this.totalItensComprados = totalItensComprados;
    }
}
