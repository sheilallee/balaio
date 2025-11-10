package com.balaio.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class LoginDTO {

    @NotBlank(message = "E-mail é obrigatório")
    @Email(message = "E-mail deve ter um formato válido")
    private String email;

    @NotBlank(message = "Senha é obrigatória")
    private String senha;

    public LoginDTO() {
    }

    public LoginDTO(String email, String senha) {
        this.email = email;
        this.senha = senha;
    }

    // Getters e Setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }
}