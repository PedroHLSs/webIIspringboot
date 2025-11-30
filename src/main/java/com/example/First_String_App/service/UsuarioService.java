package com.example.First_String_App.service;

import com.example.First_String_App.model.Usuario;

public interface UsuarioService {
    
    public Integer criarUsuario(Usuario usuario);
    public Usuario buscarPorId(Integer id);
    public Usuario buscarPorEmail(String email);
    public Iterable<Usuario> listarUsuarios();
    public void atualizarUsuario(Usuario usuario);
    public void deletarUsuario(Integer id);
    public void atualizarSenha(Integer id, String novaSenha);
    
}
