package com.example.First_String_App.service;

import com.example.First_String_App.model.Usuario;

public interface UsuarioService {
    
    public Integer criarUsuario(Usuario usuario);
    public Usuario buscarPorId(Integer id);
    public Iterable<Usuario> listarUsuarios();
    
}
