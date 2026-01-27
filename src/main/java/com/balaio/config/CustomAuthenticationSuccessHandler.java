package com.balaio.config;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.balaio.model.Usuario;
import com.balaio.service.UsuarioService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final UsuarioService usuarioService;

    @Autowired
    public CustomAuthenticationSuccessHandler(@Lazy UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        
        // Obter o email do usuário autenticado
        String email = ((UserDetails) authentication.getPrincipal()).getUsername();
        
        // Buscar o usuário completo no banco de dados
        Usuario usuario = usuarioService.buscarUsuarioPorEmail(email);
        
        // Armazenar o usuário na sessão HTTP
        HttpSession session = request.getSession();
        session.setAttribute("usuarioLogado", usuario);
        session.setAttribute("usuarioId", usuario.getId());
        session.setAttribute("usuarioEmail", usuario.getEmail());
        
        // Redirecionar para a home
        response.sendRedirect("/balaio");
    }
}
