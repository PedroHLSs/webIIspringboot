package com.example.First_String_App.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.First_String_App.model.Usuario;
import com.example.First_String_App.repository.UsuarioRepository;
import com.example.First_String_App.service.UsuarioService;

@Service
public class UsuarioServiceImpl implements UsuarioService, UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Override
    public Iterable<Usuario> listarUsuarios() {
        return usuarioRepository.findAll();
    }
    @Override
    public Usuario buscarPorId(Integer id) {
        Optional<Usuario> usuario = usuarioRepository.findById(id);
        if (usuario.isPresent()) {
            return usuario.get();
        } else {
            throw new RuntimeException("Usuário não encontrado para o id: " + id);
        }
    }
    @Override
    public Integer criarUsuario(Usuario usuario) {

        String senha = usuario.getSenha();
        String senhaCriptografada = passwordEncoder.encode(senha);
        usuario.setSenha(senhaCriptografada);
        usuario = usuarioRepository.save(usuario);

        return usuario.getId();
    }

    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<Usuario> optional = usuarioRepository.findByEmail(email);

        org.springframework.security.core.userdetails.User springUser = null;

        if (optional.isEmpty()) {
            throw new UsernameNotFoundException("User with email: " + email + " not found");
        } else {
            Usuario user = optional.get();
            List<String> roles = user.getRoles();
            Set<GrantedAuthority> ga = new HashSet<>();
            for (String role : roles) {
                ga.add(new SimpleGrantedAuthority(role));
            }

            springUser = new org.springframework.security.core.userdetails.User(
                    email,
                    user.getSenha(),
                    ga);

        }

        return springUser;
    }
}
