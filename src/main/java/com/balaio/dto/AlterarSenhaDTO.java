package com.balaio.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class AlterarSenhaDTO {

    @NotBlank(message = "Senha atual é obrigatória")
    private String senhaAtual;

    @NotBlank(message = "Nova senha é obrigatória")
    @Size(min = 8, message = "Nova senha deve ter no mínimo 8 caracteres")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d@$!%*#?&]{8,}$", 
             message = "Nova senha deve conter pelo menos uma letra e um número")
    private String novaSenha;

    @NotBlank(message = "Confirmação de senha é obrigatória")
    private String confirmarNovaSenha;

    public AlterarSenhaDTO() {
    }

    public AlterarSenhaDTO(String senhaAtual, String novaSenha, String confirmarNovaSenha) {
        this.senhaAtual = senhaAtual;
        this.novaSenha = novaSenha;
        this.confirmarNovaSenha = confirmarNovaSenha;
    }

    // Getters e Setters
    public String getSenhaAtual() {
        return senhaAtual;
    }

    public void setSenhaAtual(String senhaAtual) {
        this.senhaAtual = senhaAtual;
    }

    public String getNovaSenha() {
        return novaSenha;
    }

    public void setNovaSenha(String novaSenha) {
        this.novaSenha = novaSenha;
    }

    public String getConfirmarNovaSenha() {
        return confirmarNovaSenha;
    }

    public void setConfirmarNovaSenha(String confirmarNovaSenha) {
        this.confirmarNovaSenha = confirmarNovaSenha;
    }
}