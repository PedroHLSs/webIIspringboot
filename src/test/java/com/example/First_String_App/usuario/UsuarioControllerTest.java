package com.example.First_String_App.usuario;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.example.First_String_App.config.TestConfig;
import com.example.First_String_App.controller.UsuarioController;
import com.example.First_String_App.model.Usuario;
import com.example.First_String_App.service.UsuarioService;

@WebMvcTest(UsuarioController.class)
@Import(TestConfig.class)
public class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioService usuarioService;

    @AfterEach
    void resetMocks() {
        reset(usuarioService);
    }

    private List<Usuario> testCreateUsuariosList() {
        Usuario usuario1 = new Usuario();
        usuario1.setId(1);
        usuario1.setNome("João Silva");
        usuario1.setEmail("joao@email.com");
        usuario1.setSenha("senha123");
        usuario1.setRoles(List.of("Aluno"));

        Usuario usuario2 = new Usuario();
        usuario2.setId(2);
        usuario2.setNome("Maria Santos");
        usuario2.setEmail("maria@email.com");
        usuario2.setSenha("senha456");
        usuario2.setRoles(List.of("Professor"));

        return List.of(usuario1, usuario2);
    }

    @Test
    @DisplayName("GET /registro - Acesso ao formulário de registro (público)")
    void testRegistroFormPublico() throws Exception {
        mockMvc.perform(get("/registro"))
                .andExpect(status().isOk())
                .andExpect(view().name("user/registro"))
                .andExpect(model().attributeExists("usuario"));
    }

    @Test
    @DisplayName("GET /login - Acesso à página de login (público)")
    void testLoginPagePublico() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("user/login"));
    }

    @Test
    @DisplayName("POST /criarUsuario - Criar novo usuário")
    void testCriarUsuario() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setNome("Pedro Alves");
        usuario.setEmail("pedro@email.com");
        usuario.setSenha("senha789");

        when(usuarioService.criarUsuario(any(Usuario.class))).thenReturn(1);

        mockMvc.perform(post("/criarUsuario")
                        .with(csrf())
                        .flashAttr("usuario", usuario))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/registro"));

        verify(usuarioService).criarUsuario(any(Usuario.class));
    }

    @Test
    @WithMockUser(authorities = { "Admin" })
    @DisplayName("GET /usuarios - Admin visualiza lista de usuários")
    void testListarUsuariosAdmin() throws Exception {
        when(usuarioService.listarUsuarios()).thenReturn(testCreateUsuariosList());

        mockMvc.perform(get("/usuarios"))
                .andExpect(status().isOk())
                .andExpect(view().name("user/listarUsuarios"))
                .andExpect(model().attributeExists("listaUsuarios"))
                .andExpect(content().string(containsString("João Silva")))
                .andExpect(content().string(containsString("Maria Santos")));

        verify(usuarioService).listarUsuarios();
    }

    @Test
    @WithMockUser(authorities = { "Professor" })
    @DisplayName("GET /usuarios - Professor não pode acessar lista de usuários")
    void testListarUsuariosProfessorForbidden() throws Exception {
        mockMvc.perform(get("/usuarios"))
                .andExpect(status().isForbidden());

        verify(usuarioService, never()).listarUsuarios();
    }

    @Test
    @WithMockUser(authorities = { "Aluno" })
    @DisplayName("GET /usuarios - Aluno não pode acessar lista de usuários")
    void testListarUsuariosAlunoForbidden() throws Exception {
        mockMvc.perform(get("/usuarios"))
                .andExpect(status().isForbidden());

        verify(usuarioService, never()).listarUsuarios();
    }

    @Test
    @DisplayName("GET /usuarios - Usuário não autenticado é redirecionado para login")
    void testListarUsuariosNotAuthenticated() throws Exception {
        mockMvc.perform(get("/usuarios"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = { "Admin" })
    @DisplayName("GET /usuarios/editarUsuario/{id} - Admin acessa formulário de edição")
    void testEditarUsuarioForm() throws Exception {
        Usuario usuario = testCreateUsuariosList().get(0);

        when(usuarioService.buscarPorId(1)).thenReturn(usuario);

        mockMvc.perform(get("/usuarios/editarUsuario/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("user/editarUsuario"))
                .andExpect(model().attributeExists("usuario"));

        verify(usuarioService).buscarPorId(1);
    }

    @Test
    @WithMockUser(authorities = { "Admin" })
    @DisplayName("GET /usuarios/editarUsuario/{id} - Usuário inexistente redireciona")
    void testEditarUsuarioNaoExistente() throws Exception {
        when(usuarioService.buscarPorId(999)).thenReturn(null);

        mockMvc.perform(get("/usuarios/editarUsuario/999"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/usuarios"));

        verify(usuarioService).buscarPorId(999);
    }

    @Test
    @WithMockUser(authorities = { "Admin" })
    @DisplayName("POST /usuarios/atualizarUsuario - Admin atualiza usuário")
    void testAtualizarUsuario() throws Exception {
        Usuario usuario = testCreateUsuariosList().get(0);
        usuario.setNome("João Silva Atualizado");

        mockMvc.perform(post("/usuarios/atualizarUsuario")
                        .with(csrf())
                        .flashAttr("usuario", usuario))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/usuarios"));

        verify(usuarioService).atualizarUsuario(any(Usuario.class));
    }

    @Test
    @WithMockUser(authorities = { "Professor" })
    @DisplayName("POST /usuarios/atualizarUsuario - Professor não pode atualizar usuário")
    void testAtualizarUsuarioProfessorForbidden() throws Exception {
        Usuario usuario = testCreateUsuariosList().get(0);

        mockMvc.perform(post("/usuarios/atualizarUsuario")
                        .with(csrf())
                        .flashAttr("usuario", usuario))
                .andExpect(status().isForbidden());

        verify(usuarioService, never()).atualizarUsuario(any(Usuario.class));
    }

    @Test
    @WithMockUser(authorities = { "Admin" })
    @DisplayName("POST /usuarios/atualizarSenha - Admin atualiza senha")
    void testAtualizarSenha() throws Exception {
        mockMvc.perform(post("/usuarios/atualizarSenha")
                        .with(csrf())
                        .param("id", "1")
                        .param("novaSenha", "novaSenha123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/usuarios"));

        verify(usuarioService).atualizarSenha(1, "novaSenha123");
    }

    @Test
    @WithMockUser(authorities = { "Admin" })
    @DisplayName("GET /usuarios/deletarUsuario/{id} - Admin deleta usuário")
    void testDeletarUsuario() throws Exception {
        mockMvc.perform(get("/usuarios/deletarUsuario/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/usuarios"));

        verify(usuarioService).deletarUsuario(1);
    }

    @Test
    @WithMockUser(authorities = { "Professor" })
    @DisplayName("GET /usuarios/deletarUsuario/{id} - Professor não pode deletar usuário")
    void testDeletarUsuarioProfessorForbidden() throws Exception {
        mockMvc.perform(get("/usuarios/deletarUsuario/1"))
                .andExpect(status().isForbidden());

        verify(usuarioService, never()).deletarUsuario(anyInt());
    }

    @Test
    @WithMockUser(authorities = { "Aluno" })
    @DisplayName("GET /usuarios/deletarUsuario/{id} - Aluno não pode deletar usuário")
    void testDeletarUsuarioAlunoForbidden() throws Exception {
        mockMvc.perform(get("/usuarios/deletarUsuario/1"))
                .andExpect(status().isForbidden());

        verify(usuarioService, never()).deletarUsuario(anyInt());
    }

    @Test
    @DisplayName("GET /acessoNegado - Página de acesso negado")
    void testAcessoNegado() throws Exception {
        mockMvc.perform(get("/acessoNegado"))
                .andExpect(status().isOk())
                .andExpect(view().name("user/acessoNegado"));
    }

    @Test
    @WithMockUser(authorities = { "Admin" })
    @DisplayName("GET /usuarios - Verifica link de cadastro visível para Admin")
    void testCadastroLinkVisibleForAdmin() throws Exception {
        when(usuarioService.listarUsuarios()).thenReturn(testCreateUsuariosList());

        mockMvc.perform(get("/usuarios"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Cadastrar")));
    }

    @Test
    @WithMockUser(authorities = { "Admin" })
    @DisplayName("POST /criarUsuario - Criar usuário com roles específicos")
    void testCriarUsuarioComRoles() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setNome("Ana Costa");
        usuario.setEmail("ana@email.com");
        usuario.setSenha("senha123");
        usuario.setRoles(List.of("Admin"));

        when(usuarioService.criarUsuario(any(Usuario.class))).thenReturn(3);

        mockMvc.perform(post("/criarUsuario")
                        .with(csrf())
                        .flashAttr("usuario", usuario))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/registro"));

        verify(usuarioService).criarUsuario(any(Usuario.class));
    }

    @Test
    @WithMockUser(authorities = { "Admin" })
    @DisplayName("POST /usuarios/atualizarSenha - Validar atualização com senha vazia")
    void testAtualizarSenhaVazia() throws Exception {
        mockMvc.perform(post("/usuarios/atualizarSenha")
                        .with(csrf())
                        .param("id", "1")
                        .param("novaSenha", ""))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/usuarios"));

        verify(usuarioService).atualizarSenha(1, "");
    }
}
