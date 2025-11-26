package com.example.First_String_App.service;

import java.util.List;

import com.example.First_String_App.model.Autor;

public interface AutorService {
    List<Autor> listarAutores();

    Autor buscarAutorPorId(Long id);

    Autor getAutorById(Long id);

    Autor salvarAutor(Autor autor);

    Autor atualizarAutor(Long id, Autor autor);

    void deletarAutor(Long id);

}
