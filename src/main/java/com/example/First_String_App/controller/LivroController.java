package com.example.First_String_App.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.example.First_String_App.model.Livro;
import com.example.First_String_App.service.LivroService;

@Controller

public class LivroController {

    @Autowired
    private LivroService livroService;

    @GetMapping("/livros")
    public String index(Model model) {
        model.addAttribute("listaLivros", livroService.listarLivros());
        return "livros/listarLivros";
    }

    @GetMapping("/livros/cadastrarLivro")
    public String cadastrarLivros(Model model) {
        model.addAttribute("livro", new Livro());
        return "livros/cadastrarLivro";
    }

    @PostMapping("/livros/salvarLivro")
    public String postMethodName(@ModelAttribute("livro") Livro livro) {
        livroService.salvarLivro(livro);
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
        model.addAttribute("livro", livro);
        return "livros/editarLivro";
    }

    @GetMapping("/livros/detalhesLivro/{id}")
    public String detalhesLivro(@PathVariable Long id, Model model) {
        Livro livro = livroService.buscarLivroPorId(id); // buscar no banco
        model.addAttribute("livro", livro); // envia o livro para a view
        return "livros/detalhesLivro"; // nome da página HTML
    }

}
