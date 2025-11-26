package com.example.First_String_App.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.First_String_App.model.Autor;
import com.example.First_String_App.repository.AutorRepository;
import com.example.First_String_App.service.AutorService;

@Service
public class AutorServiceImpl implements AutorService {
    
    @Autowired
    private AutorRepository autorRepository;

    @Override
    public java.util.List<Autor> listarAutores() {
        return autorRepository.findAll();
    }
    @Override
    public Autor getAutorById(Long id) {
        return autorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Autor não encontrado para o id: " + id));
    }
    @Override
    public Autor salvarAutor(Autor autor) {
        return this.autorRepository.save(autor);
    }

    @Override
    public Autor atualizarAutor(Long id, Autor autor) {
        return this.autorRepository.save(autor);
    }
    @Override
    public void deletarAutor(Long id) {
        this.autorRepository.deleteById(id);
    }
    @Override
    public Autor buscarAutorPorId(Long id) {
        return autorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Autor não encontrado para o id: " + id));
    }
}
