package com.example.First_String_App.autor;

import static org.hamcrest.Matchers.containsString;
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
import com.example.First_String_App.controller.AutorController;
import com.example.First_String_App.model.Autor;
import com.example.First_String_App.service.AutorService;

@WebMvcTest(AutorController.class)
@Import(TestConfig.class)
public class AutoresControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AutorService autorService;

    @AfterEach
    void resetMocks() {
        reset(autorService);
    }

    private List<Autor> testCreateAutorList() {
        Autor autor1 = new Autor();
        autor1.setId(1L);
        autor1.setNome("Robert C. Martin");
        autor1.setNacionalidade("Americano");

        Autor autor2 = new Autor();
        autor2.setId(2L);
        autor2.setNome("Martin Fowler");
        autor2.setNacionalidade("Britânico");

        return List.of(autor1, autor2);
    }

    @Test
    @DisplayName("GET /autores - Listar autores sem usuário autenticado")
    void testIndexNotAuthenticatedUser() throws Exception {
        mockMvc.perform(get("/autores"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    @DisplayName("GET /autores - Listar autores com usuário logado")
    void testIndexAuthenticatedUser() throws Exception {
        when(autorService.listarAutores()).thenReturn(testCreateAutorList());

        mockMvc.perform(get("/autores"))
                .andExpect(status().isOk())
                .andExpect(view().name("autores/listarAutores"))
                .andExpect(model().attributeExists("listaAutores"))
                .andExpect(content().string(containsString("Robert C. Martin")))
                .andExpect(content().string(containsString("Martin Fowler")));
    }

    @Test
    @WithMockUser(username = "admin@biblioteca.com", authorities = { "Admin" })
    @DisplayName("GET /autores - Admin visualiza link de cadastro de autor")
    void testCreateFormAuthorizedAdmin() throws Exception {
        when(autorService.listarAutores()).thenReturn(testCreateAutorList());

        mockMvc.perform(get("/autores"))
                .andExpect(status().isOk())
                .andExpect(view().name("autores/listarAutores"))
                .andExpect(content().string(containsString("Cadastrar Novo Autor")));
    }

    @Test
    @WithMockUser(username = "professor@biblioteca.com", authorities = { "Professor" })
    @DisplayName("GET /autores - Professor visualiza link de cadastro de autor")
    void testCreateFormAuthorizedProfessor() throws Exception {
        when(autorService.listarAutores()).thenReturn(testCreateAutorList());

        mockMvc.perform(get("/autores"))
                .andExpect(status().isOk())
                .andExpect(view().name("autores/listarAutores"))
                .andExpect(content().string(containsString("Cadastrar Novo Autor")));
    }

    @Test
    @WithMockUser(username = "aluno@biblioteca.com", authorities = { "Aluno" })
    @DisplayName("GET /autores - Aluno não tem acesso à página de autores")
    void testAlunoNotAuthorizedToAccessAutores() throws Exception {
        mockMvc.perform(get("/autores"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = { "Admin" })
    @DisplayName("GET /autores/cadastrarAutor - Admin acessa formulário de cadastro")
    void testCadastrarAutorForm() throws Exception {
        mockMvc.perform(get("/autores/cadastrarAutor"))
                .andExpect(status().isOk())
                .andExpect(view().name("autores/cadastrarAutor"))
                .andExpect(model().attributeExists("autor"));
    }

    @Test
    @WithMockUser(authorities = { "Aluno" })
    @DisplayName("GET /autores/cadastrarAutor - Aluno não tem acesso ao formulário de cadastro")
    void testCadastrarAutorFormNotAuthorized() throws Exception {
        mockMvc.perform(get("/autores/cadastrarAutor"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = { "Admin" })
    @DisplayName("POST /autores/salvarAutor - Salva autor válido com sucesso")
    void testSalvarAutorValido() throws Exception {
        Autor autor = new Autor();
        autor.setNome("Kent Beck");
        autor.setNacionalidade("Americano");

        mockMvc.perform(post("/autores/salvarAutor")
                        .with(csrf())
                        .flashAttr("autor", autor))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/autores"));

        verify(autorService).salvarAutor(any(Autor.class));
    }

    @Test
    @WithMockUser(authorities = { "Professor" })
    @DisplayName("POST /autores/salvarAutor - Professor pode salvar autor")
    void testSalvarAutorProfessor() throws Exception {
        Autor autor = new Autor();
        autor.setNome("Eric Evans");
        autor.setNacionalidade("Americano");

        mockMvc.perform(post("/autores/salvarAutor")
                        .with(csrf())
                        .flashAttr("autor", autor))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/autores"));

        verify(autorService).salvarAutor(any(Autor.class));
    }

    @Test
    @WithMockUser(authorities = { "Professor" })
    @DisplayName("GET /autores/editarAutor/{id} - Professor acessa edição de autor")
    void testEditarAutorAuthorized() throws Exception {
        Autor autor = new Autor();
        autor.setId(1L);
        autor.setNome("Robert C. Martin");
        autor.setNacionalidade("Americano");

        when(autorService.getAutorById(1L)).thenReturn(autor);

        mockMvc.perform(get("/autores/editarAutor/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("autores/editarAutor"))
                .andExpect(model().attributeExists("autor"));
    }

    @Test
    @WithMockUser(authorities = { "Aluno" })
    @DisplayName("GET /autores/editarAutor/{id} - Aluno não tem acesso à edição")
    void testEditarAutorNotAuthorized() throws Exception {
        mockMvc.perform(get("/autores/editarAutor/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = { "Admin" })
    @DisplayName("GET /autores/deletarAutor/{id} - Admin deleta autor")
    void testDeletarAutor() throws Exception {
        mockMvc.perform(get("/autores/deletarAutor/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/autores"));

        verify(autorService).deletarAutor(1L);
    }

    @Test
    @WithMockUser(authorities = { "Professor" })
    @DisplayName("GET /autores/deletarAutor/{id} - Professor pode deletar autor")
    void testDeletarAutorProfessor() throws Exception {
        mockMvc.perform(get("/autores/deletarAutor/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/autores"));

        verify(autorService).deletarAutor(1L);
    }

    @Test
    @WithMockUser(authorities = { "Aluno" })
    @DisplayName("GET /autores/deletarAutor/{id} - Aluno não pode deletar autor")
    void testDeletarAutorNotAuthorized() throws Exception {
        mockMvc.perform(get("/autores/deletarAutor/1"))
                .andExpect(status().isForbidden());

        verify(autorService, never()).deletarAutor(1L);
    }

    @Test
    @WithMockUser(authorities = { "Admin" })
    @DisplayName("GET /autores/editarAutor/{id} - Admin acessa edição de autor")
    void testEditarAutorAdmin() throws Exception {
        Autor autor = new Autor();
        autor.setId(2L);
        autor.setNome("Martin Fowler");

        when(autorService.getAutorById(2L)).thenReturn(autor);

        mockMvc.perform(get("/autores/editarAutor/2"))
                .andExpect(status().isOk())
                .andExpect(view().name("autores/editarAutor"))
                .andExpect(model().attributeExists("autor"))
                .andExpect(content().string(containsString("Martin Fowler")));
    }

    @Test
    @WithMockUser(authorities = { "Professor" })
    @DisplayName("POST /autores/salvarAutor - Atualiza autor existente")
    void testAtualizarAutor() throws Exception {
        Autor autor = new Autor();
        autor.setId(1L);
        autor.setNome("Robert C. Martin - Atualizado");
        autor.setNacionalidade("Americano");

        mockMvc.perform(post("/autores/salvarAutor")
                        .with(csrf())
                        .flashAttr("autor", autor))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/autores"));

        verify(autorService).salvarAutor(any(Autor.class));
    }
}
