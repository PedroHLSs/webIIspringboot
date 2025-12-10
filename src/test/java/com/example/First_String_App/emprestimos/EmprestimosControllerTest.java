package com.example.First_String_App.emprestimos;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.time.LocalDate;
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
import com.example.First_String_App.controller.EmprestimoController;
import com.example.First_String_App.model.Emprestimo;
import com.example.First_String_App.model.Livro;
import com.example.First_String_App.model.Usuario;
import com.example.First_String_App.service.EmprestimoService;
import com.example.First_String_App.service.LivroService;
import com.example.First_String_App.service.UsuarioService;

@WebMvcTest(EmprestimoController.class)
@Import(TestConfig.class)
public class EmprestimosControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EmprestimoService emprestimoService;

    @Autowired
    private LivroService livroService;

    @Autowired
    private UsuarioService usuarioService;

    @AfterEach
    void resetMocks() {
        reset(emprestimoService, livroService, usuarioService);
    }

    private List<Emprestimo> testCreateEmprestimoList() {
        Usuario usuario1 = new Usuario();
        usuario1.setId(1);
        usuario1.setNome("João Silva");
        usuario1.setEmail("joao@email.com");

        Livro livro1 = new Livro();
        livro1.setId(1L);
        livro1.setTitulo("Clean Code");

        Emprestimo emprestimo1 = new Emprestimo();
        emprestimo1.setId(1L);
        emprestimo1.setUsuario(usuario1);
        emprestimo1.setLivro(livro1);
        emprestimo1.setDataEmprestimo(LocalDate.now());
        emprestimo1.setDataPrevistaDevolucao(LocalDate.now().plusDays(7));
        emprestimo1.setStatus(true);

        Usuario usuario2 = new Usuario();
        usuario2.setId(2);
        usuario2.setNome("Maria Santos");

        Livro livro2 = new Livro();
        livro2.setId(2L);
        livro2.setTitulo("Design Patterns");

        Emprestimo emprestimo2 = new Emprestimo();
        emprestimo2.setId(2L);
        emprestimo2.setUsuario(usuario2);
        emprestimo2.setLivro(livro2);
        emprestimo2.setDataEmprestimo(LocalDate.now());
        emprestimo2.setDataPrevistaDevolucao(LocalDate.now().plusDays(14));
        emprestimo2.setStatus(true);

        return List.of(emprestimo1, emprestimo2);
    }

    @Test
    @DisplayName("GET /emprestimo - Listar empréstimos sem usuário autenticado")
    void testIndexNotAuthenticatedUser() throws Exception {
        mockMvc.perform(get("/emprestimo"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "admin@biblioteca.com", authorities = { "Admin" })
    @DisplayName("GET /emprestimo - Admin visualiza todos os empréstimos")
    void testIndexAuthenticatedAdmin() throws Exception {
        when(emprestimoService.listarEmprestimos()).thenReturn(testCreateEmprestimoList());

        mockMvc.perform(get("/emprestimo"))
                .andExpect(status().isOk())
                .andExpect(view().name("emprestimo/listarEmprestimos"))
                .andExpect(model().attributeExists("listaEmprestimos"))
                .andExpect(content().string(containsString("João Silva")))
                .andExpect(content().string(containsString("Maria Santos")));

        verify(emprestimoService).listarEmprestimos();
    }

    @Test
    @WithMockUser(username = "professor@biblioteca.com", authorities = { "Professor" })
    @DisplayName("GET /emprestimo - Professor visualiza todos os empréstimos")
    void testIndexAuthenticatedProfessor() throws Exception {
        when(emprestimoService.listarEmprestimos()).thenReturn(testCreateEmprestimoList());

        mockMvc.perform(get("/emprestimo"))
                .andExpect(status().isOk())
                .andExpect(view().name("emprestimo/listarEmprestimos"))
                .andExpect(model().attributeExists("listaEmprestimos"));

        verify(emprestimoService).listarEmprestimos();
    }

    @Test
    @DisplayName("GET /emprestimo - Aluno visualiza apenas seus empréstimos")
    void testIndexAuthenticatedAluno() throws Exception {
        Usuario aluno = new Usuario();
        aluno.setId(1);
        aluno.setEmail("aluno@biblioteca.com");
        aluno.setNome("João Silva");

        List<Emprestimo> emprestimosAluno = List.of(testCreateEmprestimoList().get(0));

        when(usuarioService.buscarPorEmail("aluno@biblioteca.com")).thenReturn(aluno);
        when(emprestimoService.listarEmprestimosPorUsuario(1)).thenReturn(emprestimosAluno);

        mockMvc.perform(get("/emprestimo")
                .with(user("aluno@biblioteca.com").authorities(() -> "Aluno")))
                .andExpect(status().isOk())
                .andExpect(view().name("emprestimo/listarEmprestimos"))
                .andExpect(model().attributeExists("listaEmprestimos"))
                .andExpect(content().string(containsString("João Silva")))
                .andExpect(content().string(not(containsString("Maria Santos"))));

        verify(emprestimoService).listarEmprestimosPorUsuario(1);
        verify(emprestimoService, never()).listarEmprestimos();
    }

    @Test
    @WithMockUser(authorities = { "Admin" })
    @DisplayName("GET /emprestimo - Admin visualiza link de cadastro")
    void testCadastroLinkVisibleForAdmin() throws Exception {
        when(emprestimoService.listarEmprestimos()).thenReturn(testCreateEmprestimoList());

        mockMvc.perform(get("/emprestimo"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Cadastrar Novo Empréstimo")));
    }

    @Test
    @WithMockUser(authorities = { "Aluno" })
    @DisplayName("GET /emprestimo/cadastrarEmprestimo - Aluno acessa formulário de cadastro")
    void testCadastrarEmprestimoFormAluno() throws Exception {
        mockMvc.perform(get("/emprestimo/cadastrarEmprestimo"))
                .andExpect(status().isOk())
                .andExpect(view().name("emprestimo/cadastrarEmprestimo"))
                .andExpect(model().attributeExists("emprestimo"))
                .andExpect(model().attributeExists("listaLivros"))
                .andExpect(model().attributeExists("listaUsuarios"));
    }

    @Test
    @WithMockUser(authorities = { "Admin" })
    @DisplayName("GET /emprestimo/cadastrarEmprestimo - Admin acessa formulário de cadastro")
    void testCadastrarEmprestimoFormAdmin() throws Exception {
        mockMvc.perform(get("/emprestimo/cadastrarEmprestimo"))
                .andExpect(status().isOk())
                .andExpect(view().name("emprestimo/cadastrarEmprestimo"))
                .andExpect(model().attributeExists("emprestimo"));
    }

    @Test
    @WithMockUser(authorities = { "Professor" })
    @DisplayName("POST /emprestimo/salvarEmprestimo - Professor salva empréstimo")
    void testSalvarEmprestimoValido() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setId(1);

        Livro livro = new Livro();
        livro.setId(1L);

        Emprestimo emprestimo = new Emprestimo();
        emprestimo.setUsuario(usuario);
        emprestimo.setLivro(livro);
        emprestimo.setDataEmprestimo(LocalDate.now());
        emprestimo.setDataPrevistaDevolucao(LocalDate.now().plusDays(7));

        when(usuarioService.buscarPorId(1)).thenReturn(usuario);
        when(livroService.buscarLivroPorId(1L)).thenReturn(livro);

        mockMvc.perform(post("/emprestimo/salvarEmprestimo")
                        .with(csrf())
                        .flashAttr("emprestimo", emprestimo))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/emprestimo"));

        verify(emprestimoService).salvarEmprestimo(any(Emprestimo.class));
    }

    @Test
    @WithMockUser(authorities = { "Professor" })
    @DisplayName("GET /emprestimo/editarEmprestimo/{id} - Professor acessa edição")
    void testEditarEmprestimoAuthorized() throws Exception {
        Emprestimo emprestimo = testCreateEmprestimoList().get(0);

        when(emprestimoService.buscarEmprestimoPorId(1L)).thenReturn(emprestimo);

        mockMvc.perform(get("/emprestimo/editarEmprestimo/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("emprestimo/editarEmprestimo"))
                .andExpect(model().attributeExists("emprestimo"))
                .andExpect(model().attributeExists("listaLivros"))
                .andExpect(model().attributeExists("listaUsuarios"));
    }

    @Test
    @WithMockUser(authorities = { "Aluno" })
    @DisplayName("GET /emprestimo/editarEmprestimo/{id} - Aluno não tem acesso à edição")
    void testEditarEmprestimoNotAuthorized() throws Exception {
        mockMvc.perform(get("/emprestimo/editarEmprestimo/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = { "Admin" })
    @DisplayName("GET /emprestimo/deletarEmprestimo/{id} - Admin deleta empréstimo")
    void testDeletarEmprestimo() throws Exception {
        mockMvc.perform(get("/emprestimo/deletarEmprestimo/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/emprestimo"));

        verify(emprestimoService).deletarEmprestimo(1L);
    }

    @Test
    @WithMockUser(authorities = { "Aluno" })
    @DisplayName("GET /emprestimo/deletarEmprestimo/{id} - Aluno não pode deletar empréstimo")
    void testDeletarEmprestimoNotAuthorized() throws Exception {
        mockMvc.perform(get("/emprestimo/deletarEmprestimo/1"))
                .andExpect(status().isForbidden());

        verify(emprestimoService, never()).deletarEmprestimo(1L);
    }

    @Test
    @WithMockUser(authorities = { "Admin" })
    @DisplayName("GET /emprestimo/editarEmprestimo/{id} - Redireciona se empréstimo não existe")
    void testEditarEmprestimoInexistente() throws Exception {
        when(emprestimoService.buscarEmprestimoPorId(999L)).thenReturn(null);

        mockMvc.perform(get("/emprestimo/editarEmprestimo/999"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/emprestimo"));
    }

    @Test
    @WithMockUser(authorities = { "Aluno" })
    @DisplayName("POST /emprestimo/salvarEmprestimo - Aluno pode criar empréstimo")
    void testAlunoCriarEmprestimo() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setId(2);

        Livro livro = new Livro();
        livro.setId(2L);

        Emprestimo emprestimo = new Emprestimo();
        emprestimo.setUsuario(usuario);
        emprestimo.setLivro(livro);
        emprestimo.setDataEmprestimo(LocalDate.now());

        when(usuarioService.buscarPorId(2)).thenReturn(usuario);
        when(livroService.buscarLivroPorId(2L)).thenReturn(livro);

        mockMvc.perform(post("/emprestimo/salvarEmprestimo")
                        .with(csrf())
                        .flashAttr("emprestimo", emprestimo))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/emprestimo"));

        verify(emprestimoService).salvarEmprestimo(any(Emprestimo.class));
    }

    @Test
    @WithMockUser(authorities = { "Professor" })
    @DisplayName("GET /emprestimo/cadastrarEmprestimo - Cadastro com parâmetro de livro")
    void testCadastrarEmprestimoComLivroId() throws Exception {
        Livro livro = new Livro();
        livro.setId(1L);
        livro.setTitulo("Clean Code");

        when(livroService.buscarLivroPorId(1L)).thenReturn(livro);

        mockMvc.perform(get("/emprestimo/cadastrarEmprestimo")
                        .param("id", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("emprestimo/cadastrarEmprestimo"))
                .andExpect(model().attributeExists("emprestimo"));

        verify(livroService).buscarLivroPorId(1L);
    }
}
