package com.example.First_String_App.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.example.First_String_App.model.Livro;
import com.example.First_String_App.service.livroService;

@Controller

public class LivroController {

    @Autowired
    private livroService livroService;

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

}
