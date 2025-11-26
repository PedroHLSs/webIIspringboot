package com.example.First_String_App.controller;

import java.time.LocalDate;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.example.First_String_App.model.Emprestimo;
import com.example.First_String_App.model.Multas;
import com.example.First_String_App.model.Usuario;
import com.example.First_String_App.service.EmprestimoService;
import com.example.First_String_App.service.MultaService;
import com.example.First_String_App.service.UsuarioService;

@Controller
public class MultasController {
    private final UsuarioService usuarioService;
    private final MultaService multasService;
    private final EmprestimoService emprestimoService;

    public MultasController(UsuarioService usuarioService, MultaService multasService,
            EmprestimoService emprestimoService) {
        this.usuarioService = usuarioService;
        this.multasService = multasService;
        this.emprestimoService = emprestimoService;
    }

    @GetMapping("/multas")
    public String index(Model model) {
        model.addAttribute("listaMultas", multasService.listarMultas());
        return "multas/listarMultas";
    } 

    @GetMapping("/multas/cadastrarMulta")
    public String cadastrarMultas(Model model) {
        Multas multa = new Multas();
        multa.setDataMulta(LocalDate.now());
        model.addAttribute("multa", multa);
        model.addAttribute("listaUsuarios", usuarioService.listarUsuarios());
        model.addAttribute("listaEmprestimos", emprestimoService.listarEmprestimos());
        return "multas/cadastrarMulta";
    }

    @PostMapping("/multas/salvarMulta")
    public String salvarMulta(Multas multa) {
        if (multa.getEmprestimo() != null && multa.getEmprestimo().getId() != null) {
            Emprestimo emprestimo = emprestimoService.buscarEmprestimoPorId(multa.getEmprestimo().getId());
            multa.setEmprestimo(emprestimo);
        }
        if (multa.getUsuario() != null && multa.getUsuario().getId() != null) {
            Usuario usuario = usuarioService.buscarPorId(multa.getUsuario().getId());
            multa.setUsuario(usuario);
        }
        multasService.salvarMulta(multa);
        return "redirect:/multas";
    }

    @GetMapping("/multas/deletarMulta/{id}")
    public String deletarMulta(@PathVariable("id") Long id) {
        this.multasService.deletarMulta(id);
        return "redirect:/multas";
    }

    @GetMapping("/multas/editarMulta/{id}")
    public String abrirEditarMulta(@PathVariable("id") Long id, Model model) {
        Multas multa = multasService.buscarMultaPorId(id);
        if (multa == null) {
            return "redirect:/multas";
        }
        model.addAttribute("multa", multa);
        model.addAttribute("listaUsuarios", usuarioService.listarUsuarios());
        model.addAttribute("listaEmprestimos", emprestimoService.listarEmprestimos());
        return "multas/editarMulta";
    }
}
