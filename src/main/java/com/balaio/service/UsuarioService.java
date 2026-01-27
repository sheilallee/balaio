package com.balaio.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.balaio.dto.AlterarSenhaDTO;
import com.balaio.dto.UsuarioCadastroDTO;
import com.balaio.model.Usuario;
import com.balaio.repository.UsuarioRepository;

@Service
@Transactional
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Usuario cadastrarUsuario(UsuarioCadastroDTO dto) {
        if (usuarioRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("E-mail já cadastrado");
        }

        if (!dto.getSenha().equals(dto.getConfirmarSenha())) {
            throw new RuntimeException("Senhas não coincidem");
        }

        Usuario usuario = new Usuario();
        usuario.setNomeCompleto(dto.getNomeCompleto());
        usuario.setEmail(dto.getEmail());
        usuario.setSenha(passwordEncoder.encode(dto.getSenha()));
        usuario.setDataCriacao(LocalDateTime.now());
        usuario.setDataAtualizacao(LocalDateTime.now());

        return usuarioRepository.save(usuario);
    }

    public Optional<Usuario> buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    public Usuario buscarUsuarioPorEmail(String email) {
        return usuarioRepository.findByEmail(email).orElse(null);
    }

    public Optional<Usuario> buscarPorId(Long id) {
        return usuarioRepository.findById(id);
    }

    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    public Usuario atualizarUsuario(Long id, Usuario usuarioAtualizado) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        // CA2 - Validar nome (2-100 caracteres)
        if (usuarioAtualizado.getNomeCompleto() == null || usuarioAtualizado.getNomeCompleto().trim().isEmpty()) {
            throw new RuntimeException("Nome é obrigatório");
        }
        if (usuarioAtualizado.getNomeCompleto().length() < 2 || usuarioAtualizado.getNomeCompleto().length() > 100) {
            throw new RuntimeException("Nome deve ter entre 2 e 100 caracteres");
        }

        // CA3 e CA6 - Validar email formato válido e se já está em uso por outro usuário
        if (usuarioAtualizado.getEmail() == null || usuarioAtualizado.getEmail().trim().isEmpty()) {
            throw new RuntimeException("E-mail é obrigatório");
        }
        if (!usuarioAtualizado.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new RuntimeException("E-mail deve ter um formato válido");
        }
        // Verificar se o email já está em uso por outro usuário
        Optional<Usuario> usuarioComEmail = usuarioRepository.findByEmail(usuarioAtualizado.getEmail());
        if (usuarioComEmail.isPresent() && !usuarioComEmail.get().getId().equals(id)) {
            throw new RuntimeException("E-mail já está em uso");
        }

        usuario.setNomeCompleto(usuarioAtualizado.getNomeCompleto());
        usuario.setEmail(usuarioAtualizado.getEmail());
        usuario.setDataAtualizacao(LocalDateTime.now());

        return usuarioRepository.save(usuario);
    }

    public void alterarSenha(Long usuarioId, AlterarSenhaDTO dto) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if (!passwordEncoder.matches(dto.getSenhaAtual(), usuario.getSenha())) {
            throw new RuntimeException("Senha atual incorreta");
        }

        if (!dto.getNovaSenha().equals(dto.getConfirmarNovaSenha())) {
            throw new RuntimeException("Senhas não coincidem");
        }

        usuario.setSenha(passwordEncoder.encode(dto.getNovaSenha()));
        usuario.setDataAtualizacao(LocalDateTime.now());

        usuarioRepository.save(usuario);
    }

    public void excluirUsuario(Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw new RuntimeException("Usuário não encontrado");
        }
        usuarioRepository.deleteById(id);
    }
}