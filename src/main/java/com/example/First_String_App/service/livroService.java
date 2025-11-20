package com.example.First_String_App.service;

import java.util.List;

import com.example.First_String_App.model.Livro;

public interface LivroService {
    List<Livro> listarLivros();
    Livro buscarLivroPorId(Long id);
    Livro getLivroById(long id);
    Livro salvarLivro(Livro livro);
    Livro atualizarLivro(Long id, Livro livro);
    void deletarLivro(Long id);
}
