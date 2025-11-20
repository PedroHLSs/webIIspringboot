package com.example.First_String_App.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.First_String_App.model.Livro;
import com.example.First_String_App.repository.LivroRepository;
import com.example.First_String_App.service.LivroService;

@Service
public class LivroServiceImpl implements LivroService {

    @Autowired
    private LivroRepository livroRepository;

    @Override
    public List<Livro> listarLivros() {
        return livroRepository.findAll();
    }
    @Override
    public Livro buscarLivroPorId(Long id) {
        Optional<Livro> livro = livroRepository.findById(id);
        if (livro.isPresent()) {
            return livro.get();
        } else {
            throw new RuntimeException("Livro não encontrado para o id: " + id);
        }
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
