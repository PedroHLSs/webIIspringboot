package com.example.First_String_App.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.First_String_App.model.Autor;
import com.example.First_String_App.model.Livro;
import com.example.First_String_App.model.LivroAutor;
import com.example.First_String_App.repository.LivroAutorRepository;
import com.example.First_String_App.service.AutorService;
import com.example.First_String_App.service.LivroService;

@Controller
public class LivroController {

    private final LivroService livroService;
    private final AutorService autorService;
    private final LivroAutorRepository livroAutorRepository;

    public LivroController(LivroService livroService, AutorService autorService, LivroAutorRepository livroAutorRepository) {
        this.livroService = livroService;
        this.autorService = autorService;
        this.livroAutorRepository = livroAutorRepository;
    }

    @GetMapping("/livros")
    public String index(Model model) {
        model.addAttribute("listaLivros", livroService.listarLivros());
        return "livros/listarLivros";
    }

    @GetMapping("/livros/cadastrarLivro")
    public String cadastrarLivros(Model model) {
        model.addAttribute("livro", new Livro());
        model.addAttribute("listaAutores", autorService.listarAutores());
        return "livros/cadastrarLivro";
    }

    @PostMapping("/livros/salvarLivro")
    public String salvarLivro(@ModelAttribute Livro livro, 
                              @RequestParam(value = "autoresIds", required = false) List<Long> autoresIds) {
        // Salvar o livro primeiro
        livroService.salvarLivro(livro);
        
        // Associar os autores selecionados
        if (autoresIds != null && !autoresIds.isEmpty()) {
            for (Long autorId : autoresIds) {
                Autor autor = autorService.getAutorById(autorId);
                if (autor != null) {
                    LivroAutor livroAutor = new LivroAutor(livro, autor);
                    livroAutorRepository.save(livroAutor);
                }
            }
        }
        
        return "redirect:/livros";
    }

    @GetMapping("/livros/deletarLivro/{id}")
    public String deletarLivro(@PathVariable("id") Long id) {
        this.livroService.deletarLivro(id);
        return "redirect:/livros";
    }

    @GetMapping("/livros/editarLivro/{id}")
    public String editarLivro(@PathVariable("id") Long id, Model model) {
        Livro livro = livroService.getLivroById(id);
        if (livro == null) {
            return "redirect:/livros";
        }
        model.addAttribute("livro", livro);
        model.addAttribute("listaAutores", autorService.listarAutores());
        return "livros/editarLivro";
    }

    @GetMapping("/livros/detalhesLivro/{id}")
    public String detalhesLivro(@PathVariable Long id, Model model) {
        Livro livro = livroService.buscarLivroPorId(id);
        if (livro == null) {
            return "redirect:/livros";
        }
        model.addAttribute("livro", livro);
        return "livros/detalhesLivro";
    }
}
