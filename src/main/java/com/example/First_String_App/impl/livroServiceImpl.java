package com.example.First_String_App.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.First_String_App.model.Livro;
import com.example.First_String_App.repository.livroRepository;
import com.example.First_String_App.service.livroService;

@Service
public class livroServiceImpl implements livroService {

    @Autowired
    private livroRepository livroRepository;

    @Override
    public List<Livro> listarLivros() {
        return livroRepository.findAll();
    }

    @Override
    public Livro salvarLivro(Livro livro) {
        return this.livroRepository.save(livro);
    }
    @Override
    public Livro atualizarLivro(Long id, Livro livro){
        return this.livroRepository.save(livro);
    }
    @Override
    public Livro getLivroById(long id) {
        Optional<Livro> optional = livroRepository.findById(id);
        if (optional.isPresent()) {
            return optional.get();
        } else {
            throw new RuntimeException("Livro não foi encontrado no id: " + id);
        }
    }
    @Override
    public void deletarLivro(Long id){
        this.livroRepository.deleteById(id);
    }
}
