package com.example.First_String_App.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.example.First_String_App.model.Autor;
import com.example.First_String_App.service.AutorService;

@Controller
public class AutorController {
    
    @Autowired
    private AutorService autorService;

    @GetMapping("/autores")
    public String index(Model model) {
        model.addAttribute("listaAutores", autorService.listarAutores());
        return "autores/listarAutores";
    }

    @GetMapping("/autores/cadastrarAutor")
    public String cadastrarAutor(Model model) {
        model.addAttribute("autor", new Autor());
        return "autores/cadastrarAutor";
    }

    @PostMapping("/autores/salvarAutor")
    public String salvarAutor(@ModelAttribute("autor") Autor autor) {
        autorService.salvarAutor(autor);
        return "redirect:/autores";
    }

    @GetMapping("/autores/editarAutor/{id}")
    public String editarAutor(@PathVariable("id") Long id, Model model) {
        Autor autor = autorService.getAutorById(id);
        model.addAttribute("autor", autor);
        return "autores/editarAutor";
    }

    @GetMapping("/autores/deletarAutor/{id}")
    public String deletarAutor(@PathVariable("id") Long id) {
        autorService.deletarAutor(id);
        return "redirect:/autores";
    }
}
