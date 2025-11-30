package com.example.First_String_App.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    @GetMapping("/usuarios")
    public String listarUsuarios(Model model) {
        model.addAttribute("listaUsuarios", usuarioService.listarUsuarios());
        return "user/listarUsuarios";
    }

    @GetMapping("/usuarios/editarUsuario/{id}")
    public String editarUsuario(@PathVariable("id") Integer id, Model model) {
        Usuario usuario = usuarioService.buscarPorId(id);
        if (usuario == null) {
            return "redirect:/usuarios";
        }
        model.addAttribute("usuario", usuario);
        return "user/editarUsuario";
    }

    @PostMapping("/usuarios/atualizarUsuario")
    public String atualizarUsuario(@ModelAttribute Usuario usuario, RedirectAttributes redirectAttributes) {
        usuarioService.atualizarUsuario(usuario);
        redirectAttributes.addFlashAttribute("mensagem", "Usuário atualizado com sucesso!");
        return "redirect:/usuarios";
    }

    @PostMapping("/usuarios/atualizarSenha")
    public String atualizarSenha(@RequestParam("id") Integer id, 
                                  @RequestParam("novaSenha") String novaSenha,
                                  RedirectAttributes redirectAttributes) {
        usuarioService.atualizarSenha(id, novaSenha);
        redirectAttributes.addFlashAttribute("mensagem", "Senha atualizada com sucesso!");
        return "redirect:/usuarios";
    }

    @GetMapping("/usuarios/deletarUsuario/{id}")
    public String deletarUsuario(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
        usuarioService.deletarUsuario(id);
        redirectAttributes.addFlashAttribute("mensagem", "Usuário deletado com sucesso!");
        return "redirect:/usuarios";
    }
}
