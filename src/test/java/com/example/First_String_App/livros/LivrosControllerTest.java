package com.example.First_String_App.livros;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
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
import com.example.First_String_App.controller.LivroController;
import com.example.First_String_App.model.Livro;
import com.example.First_String_App.service.AutorService;
import com.example.First_String_App.service.LivroService;

@WebMvcTest(LivroController.class)
@Import(TestConfig.class)
public class LivrosControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LivroService livroService;

    @Autowired
    private AutorService autorService;

    @AfterEach
    void resetMocks() {
        reset(livroService, autorService);
    }

    private List<Livro> testCreateLivroList() {
        Livro livro1 = new Livro();
        livro1.setId(1L);
        livro1.setTitulo("Clean Code");
        livro1.setIsbn("978-0132350884");
        livro1.setDisponivel(true);

        Livro livro2 = new Livro();
        livro2.setId(2L);
        livro2.setTitulo("Design Patterns");
        livro2.setIsbn("978-0201633612");
        livro2.setDisponivel(true);

        return List.of(livro1, livro2);
    }

    @Test
    @DisplayName("GET /livros - Listar livros sem usuário autenticado")
    void testIndexNotAuthenticatedUser() throws Exception {
        mockMvc.perform(get("/livros"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    @DisplayName("GET /livros - Listar livros com usuário logado")
    void testIndexAuthenticatedUser() throws Exception {
        when(livroService.listarLivros()).thenReturn(testCreateLivroList());

        mockMvc.perform(get("/livros"))
                .andExpect(status().isOk())
                .andExpect(view().name("livros/listarLivros"))
                .andExpect(model().attributeExists("listaLivros"))
                .andExpect(content().string(containsString("Clean Code")))
                .andExpect(content().string(containsString("Design Patterns")));
    }

    @Test
    @WithMockUser(username = "admin@biblioteca.com", authorities = { "Admin" })
    @DisplayName("GET /livros - Admin visualiza link de cadastro de livro")
    void testCreateFormAuthorizedAdmin() throws Exception {
        when(livroService.listarLivros()).thenReturn(testCreateLivroList());

        mockMvc.perform(get("/livros"))
                .andExpect(status().isOk())
                .andExpect(view().name("livros/listarLivros"))
                .andExpect(content().string(containsString("Cadastrar Novo Livro")));
    }

    @Test
    @WithMockUser(username = "professor@biblioteca.com", authorities = { "Professor" })
    @DisplayName("GET /livros - Professor visualiza link de cadastro de livro")
    void testCreateFormAuthorizedProfessor() throws Exception {
        when(livroService.listarLivros()).thenReturn(testCreateLivroList());

        mockMvc.perform(get("/livros"))
                .andExpect(status().isOk())
                .andExpect(view().name("livros/listarLivros"))
                .andExpect(content().string(containsString("Cadastrar Novo Livro")));
    }

    @Test
    @WithMockUser(username = "aluno@biblioteca.com", authorities = { "Aluno" })
    @DisplayName("GET /livros - Aluno não visualiza link de cadastro de livro")
    void testCreateFormNotAuthorizedAluno() throws Exception {
        when(livroService.listarLivros()).thenReturn(testCreateLivroList());

        mockMvc.perform(get("/livros"))
                .andExpect(status().isOk())
                .andExpect(view().name("livros/listarLivros"))
                .andExpect(content().string(not(containsString("Cadastrar Novo Livro"))));
    }

    @Test
    @WithMockUser(authorities = { "Admin" })
    @DisplayName("GET /livros/cadastrarLivro - Admin acessa formulário de cadastro")
    void testCadastrarLivroForm() throws Exception {
        mockMvc.perform(get("/livros/cadastrarLivro"))
                .andExpect(status().isOk())
                .andExpect(view().name("livros/cadastrarLivro"))
                .andExpect(model().attributeExists("livro"))
                .andExpect(model().attributeExists("listaAutores"));
    }

    @Test
    @WithMockUser(authorities = { "Aluno" })
    @DisplayName("GET /livros/cadastrarLivro - Aluno não tem acesso ao formulário de cadastro")
    void testCadastrarLivroFormNotAuthorized() throws Exception {
        mockMvc.perform(get("/livros/cadastrarLivro"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = { "Admin" })
    @DisplayName("POST /livros/salvarLivro - Salva livro válido com sucesso")
    void testSalvarLivroValido() throws Exception {
        Livro livro = new Livro();
        livro.setTitulo("Refactoring");
        livro.setIsbn("978-0201485677");
        livro.setDisponivel(true);

        mockMvc.perform(post("/livros/salvarLivro")
                        .with(csrf())
                        .flashAttr("livro", livro))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/livros"));

        verify(livroService).salvarLivro(any(Livro.class));
    }

    @Test
    @WithMockUser(authorities = { "Professor" })
    @DisplayName("GET /livros/editarLivro/{id} - Professor acessa edição de livro")
    void testEditarLivroAuthorized() throws Exception {
        Livro livro = new Livro();
        livro.setId(1L);
        livro.setTitulo("Clean Code");

        when(livroService.getLivroById(1L)).thenReturn(livro);

        mockMvc.perform(get("/livros/editarLivro/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("livros/editarLivro"))
                .andExpect(model().attributeExists("livro"))
                .andExpect(model().attributeExists("listaAutores"));
    }

    @Test
    @WithMockUser(authorities = { "Aluno" })
    @DisplayName("GET /livros/editarLivro/{id} - Aluno não tem acesso à edição")
    void testEditarLivroNotAuthorized() throws Exception {
        mockMvc.perform(get("/livros/editarLivro/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = { "Admin" })
    @DisplayName("GET /livros/deletarLivro/{id} - Admin deleta livro")
    void testDeletarLivro() throws Exception {
        mockMvc.perform(get("/livros/deletarLivro/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/livros"));

        verify(livroService).deletarLivro(1L);
    }

    @Test
    @WithMockUser(authorities = { "Aluno" })
    @DisplayName("GET /livros/deletarLivro/{id} - Aluno não pode deletar livro")
    void testDeletarLivroNotAuthorized() throws Exception {
        mockMvc.perform(get("/livros/deletarLivro/1"))
                .andExpect(status().isForbidden());

        verify(livroService, never()).deletarLivro(1L);
    }

    @Test
    @WithMockUser
    @DisplayName("GET /livros/detalhesLivro/{id} - Todos usuários podem ver detalhes")
    void testDetalhesLivro() throws Exception {
        Livro livro = new Livro();
        livro.setId(1L);
        livro.setTitulo("Clean Code");
        livro.setIsbn("978-0132350884");

        when(livroService.buscarLivroPorId(1L)).thenReturn(livro);

        mockMvc.perform(get("/livros/detalhesLivro/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("livros/detalhesLivro"))
                .andExpect(model().attributeExists("livro"))
                .andExpect(content().string(containsString("Clean Code")));
    }

    @Test
    @WithMockUser(authorities = { "Admin" })
    @DisplayName("GET /livros/editarLivro/{id} - Redireciona se livro não existe")
    void testEditarLivroInexistente() throws Exception {
        when(livroService.getLivroById(999L)).thenReturn(null);

        mockMvc.perform(get("/livros/editarLivro/999"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/livros"));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /livros/detalhesLivro/{id} - Redireciona se livro não existe")
    void testDetalhesLivroInexistente() throws Exception {
        when(livroService.buscarLivroPorId(999L)).thenReturn(null);

        mockMvc.perform(get("/livros/detalhesLivro/999"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/livros"));
    }
}
