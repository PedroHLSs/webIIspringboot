package com.example.First_String_App.controller;

import java.time.LocalDate;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.First_String_App.model.Emprestimo;
import com.example.First_String_App.model.Livro;
import com.example.First_String_App.model.Usuario;
import com.example.First_String_App.service.EmprestimoService;
import com.example.First_String_App.service.LivroService;
import com.example.First_String_App.service.UsuarioService;

@Controller
public class EmprestimoController {

    private final LivroService livroService;
    private final UsuarioService usuarioService;
    private final EmprestimoService emprestimoService;

    public EmprestimoController(LivroService livroService, UsuarioService usuarioService, EmprestimoService emprestimoService) {
        this.livroService = livroService;
        this.usuarioService = usuarioService;
        this.emprestimoService = emprestimoService;
    }

    @GetMapping("/emprestimos")
    public String index(Model model) {
        model.addAttribute("listaEmprestimos", emprestimoService.listarEmprestimos());
        return "emprestimo/listarEmprestimos";
    }

    @GetMapping("/emprestimos/cadastrarEmprestimo")
    public String abrirFormCadastrar(@RequestParam(value = "id", required = false) Long id, Model model) {
        model.addAttribute("listaLivros", livroService.listarLivros());
        model.addAttribute("listaUsuarios", usuarioService.listarUsuarios());
        Emprestimo emprestimo = new Emprestimo();
        if (id != null) {
            Livro livro = livroService.buscarLivroPorId(id);
            if (livro != null) emprestimo.setLivro(livro);
        }
        emprestimo.setDataEmprestimo(LocalDate.now());
        model.addAttribute("emprestimo", emprestimo);
        return "emprestimo/cadastrarEmprestimo";
    }

    @PostMapping("/emprestimos/salvarEmprestimo")
    public String salvarEmprestimo(Emprestimo emprestimo) {
        if (emprestimo.getLivro() != null && emprestimo.getLivro().getId() != null) {
            Livro livro = livroService.buscarLivroPorId(emprestimo.getLivro().getId());
            emprestimo.setLivro(livro);
        }
        if (emprestimo.getUsuario() != null && emprestimo.getUsuario().getId() != null) {
            Usuario usuario = usuarioService.buscarPorId(emprestimo.getUsuario().getId());
            emprestimo.setUsuario(usuario);
        }
        emprestimoService.salvarEmprestimo(emprestimo);
        return "redirect:/emprestimo";
    }

    @GetMapping("/emprestimos/deletarEmprestimo/{id}")
    public String deletarEmprestimo(@PathVariable("id") Long id) {
        this.emprestimoService.deletarEmprestimo(id);
        return "redirect:/emprestimo";
    }

    @GetMapping("/emprestimos/editarEmprestimo/{id}")
    public String editarEmprestimo(@PathVariable("id") Long id, Model model) {
        Emprestimo emprestimo = emprestimoService.getEmprestimoById(id);
        model.addAttribute("emprestimo", emprestimo);
        return "emprestimo/editarEmprestimo";
    }

    @GetMapping("/emprestimos/detalhesEmprestimo/{id}")
    public String detalhesEmprestimo(@PathVariable Long id, Model model) {
        Emprestimo emprestimo = emprestimoService.buscarEmprestimoPorId(id);
        model.addAttribute("emprestimo", emprestimo);
        return "emprestimo/detalhesEmprestimo";
    }
}
