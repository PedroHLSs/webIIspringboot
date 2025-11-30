package com.example.First_String_App.controller;

import java.time.LocalDate;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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

    @GetMapping("/emprestimo")
    public String index(Model model, Authentication authentication) {
        // Check if user is Aluno
        if (authentication != null && 
            authentication.getAuthorities().contains(new SimpleGrantedAuthority("Aluno"))) {
            // Filter emprestimos by logged-in user
            String email = authentication.getName();
            Usuario usuario = usuarioService.buscarPorEmail(email);
            if (usuario != null) {
                model.addAttribute("listaEmprestimos", 
                    emprestimoService.listarEmprestimosPorUsuario(usuario.getId()));
            } else {
                model.addAttribute("listaEmprestimos", emprestimoService.listarEmprestimos());
            }
        } else {
            // Admin or Professor sees all
            model.addAttribute("listaEmprestimos", emprestimoService.listarEmprestimos());
        }
        return "emprestimo/listarEmprestimos";
    }

    @GetMapping("/emprestimo/cadastrarEmprestimo")
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

    @PostMapping("/emprestimo/salvarEmprestimo")
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

    @GetMapping("/emprestimo/deletarEmprestimo/{id}")
    public String deletarEmprestimo(@PathVariable("id") Long id) {
        this.emprestimoService.deletarEmprestimo(id);
        return "redirect:/emprestimo";
    }

    @GetMapping("/emprestimo/editarEmprestimo/{id}")
    public String abrirEditarEmprestimo(@PathVariable("id") Long id, Model model) {
        Emprestimo emprestimo = emprestimoService.buscarEmprestimoPorId(id);
        if (emprestimo == null) {
            return "redirect:/emprestimo";
        }
        model.addAttribute("emprestimo", emprestimo);
        model.addAttribute("listaLivros", livroService.listarLivros());
        model.addAttribute("listaUsuarios", usuarioService.listarUsuarios());
        return "emprestimo/editarEmprestimo";
    }
}
