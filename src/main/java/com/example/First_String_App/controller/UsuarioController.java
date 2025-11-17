package com.example.First_String_App.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.First_String_App.model.Usuario;
import com.example.First_String_App.service.UsuarioService;

@Controller
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping("/registro")
    public String registro(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "user/registro";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "user/login";
    }

    @PostMapping("/criarUsuario")
    public String criarUsuario(@ModelAttribute Usuario usuario, RedirectAttributes redirectAttributes) {
        Integer id = usuarioService.criarUsuario(usuario);
        String mensagem = "Usuário com o id " + id + " criado com sucesso!";
        redirectAttributes.addFlashAttribute("mensagem", mensagem);
        return "redirect:/registro"; // <--- agora redireciona (GET)
    }

    @GetMapping("/acessoNegado")
    public String getAccessDeniedPage() {
        return "user/acessoNegado";
    }
}
