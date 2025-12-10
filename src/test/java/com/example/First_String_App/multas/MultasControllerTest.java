package com.example.First_String_App.multas;

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

import java.math.BigDecimal;
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
import com.example.First_String_App.controller.MultasController;
import com.example.First_String_App.model.Emprestimo;
import com.example.First_String_App.model.Multas;
import com.example.First_String_App.model.Usuario;
import com.example.First_String_App.service.EmprestimoService;
import com.example.First_String_App.service.MultaService;
import com.example.First_String_App.service.UsuarioService;

@WebMvcTest(MultasController.class)
@Import(TestConfig.class)
public class MultasControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MultaService multaService;

    @Autowired
    private EmprestimoService emprestimoService;

    @Autowired
    private UsuarioService usuarioService;

    @AfterEach
    void resetMocks() {
        reset(multaService, emprestimoService, usuarioService);
    }

    private List<Multas> testCreateMultasList() {
        Usuario usuario1 = new Usuario();
        usuario1.setId(1);
        usuario1.setNome("João Silva");
        usuario1.setEmail("joao@email.com");

        Emprestimo emprestimo1 = new Emprestimo();
        emprestimo1.setId(1L);

        Multas multa1 = new Multas();
        multa1.setId(1L);
        multa1.setUsuario(usuario1);
        multa1.setEmprestimo(emprestimo1);
        multa1.setValor(new BigDecimal("10.00"));
        multa1.setDataMulta(LocalDate.now());
        multa1.setMotivo("Atraso na devolução");
        multa1.setPaga(false);

        Usuario usuario2 = new Usuario();
        usuario2.setId(2);
        usuario2.setNome("Maria Santos");
        usuario2.setEmail("maria@email.com");

        Emprestimo emprestimo2 = new Emprestimo();
        emprestimo2.setId(2L);

        Multas multa2 = new Multas();
        multa2.setId(2L);
        multa2.setUsuario(usuario2);
        multa2.setEmprestimo(emprestimo2);
        multa2.setValor(new BigDecimal("15.00"));
        multa2.setDataMulta(LocalDate.now());
        multa2.setMotivo("Dano ao livro");
        multa2.setPaga(true);

        return List.of(multa1, multa2);
    }

    @Test
    @DisplayName("GET /multas - Listar multas sem usuário autenticado")
    void testIndexNotAuthenticatedUser() throws Exception {
        mockMvc.perform(get("/multas"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "admin@biblioteca.com", authorities = { "Admin" })
    @DisplayName("GET /multas - Admin visualiza todas as multas")
    void testIndexAuthenticatedAdmin() throws Exception {
        when(multaService.listarMultas()).thenReturn(testCreateMultasList());

        mockMvc.perform(get("/multas"))
                .andExpect(status().isOk())
                .andExpect(view().name("multas/listarMultas"))
                .andExpect(model().attributeExists("listaMultas"))
                .andExpect(content().string(containsString("João Silva")))
                .andExpect(content().string(containsString("Maria Santos")));

        verify(multaService).listarMultas();
    }

    @Test
    @WithMockUser(username = "professor@biblioteca.com", authorities = { "Professor" })
    @DisplayName("GET /multas - Professor visualiza todas as multas")
    void testIndexAuthenticatedProfessor() throws Exception {
        when(multaService.listarMultas()).thenReturn(testCreateMultasList());

        mockMvc.perform(get("/multas"))
                .andExpect(status().isOk())
                .andExpect(view().name("multas/listarMultas"))
                .andExpect(model().attributeExists("listaMultas"));

        verify(multaService).listarMultas();
    }

    @Test
    @DisplayName("GET /multas - Aluno visualiza apenas suas multas")
    void testIndexAuthenticatedAluno() throws Exception {
        Usuario aluno = new Usuario();
        aluno.setId(1);
        aluno.setEmail("aluno@biblioteca.com");
        aluno.setNome("João Silva");

        List<Multas> multasAluno = List.of(testCreateMultasList().get(0));

        when(usuarioService.buscarPorEmail("aluno@biblioteca.com")).thenReturn(aluno);
        when(multaService.listarMultasPorUsuario(1)).thenReturn(multasAluno);

        mockMvc.perform(get("/multas")
                .with(user("aluno@biblioteca.com").authorities(() -> "Aluno")))
                .andExpect(status().isOk())
                .andExpect(view().name("multas/listarMultas"))
                .andExpect(model().attributeExists("listaMultas"))
                .andExpect(content().string(containsString("João Silva")))
                .andExpect(content().string(not(containsString("Maria Santos"))));

        verify(multaService).listarMultasPorUsuario(1);
        verify(multaService, never()).listarMultas();
    }

    @Test
    @WithMockUser(authorities = { "Admin" })
    @DisplayName("GET /multas - Admin visualiza link de cadastro")
    void testCadastroLinkVisibleForAdmin() throws Exception {
        when(multaService.listarMultas()).thenReturn(testCreateMultasList());

        mockMvc.perform(get("/multas"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Cadastrar Nova Multa")));
    }

    @Test
    @WithMockUser(authorities = { "Professor" })
    @DisplayName("GET /multas - Professor visualiza link de cadastro")
    void testCadastroLinkVisibleForProfessor() throws Exception {
        when(multaService.listarMultas()).thenReturn(testCreateMultasList());

        mockMvc.perform(get("/multas"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Cadastrar Nova Multa")));
    }

    @Test
    @WithMockUser(username = "aluno@biblioteca.com", authorities = { "Aluno" })
    @DisplayName("GET /multas - Aluno não visualiza link de cadastro")
    void testCadastroLinkNotVisibleForAluno() throws Exception {
        Usuario aluno = new Usuario();
        aluno.setId(1);
        aluno.setEmail("aluno@biblioteca.com");

        when(usuarioService.buscarPorEmail("aluno@biblioteca.com")).thenReturn(aluno);
        when(multaService.listarMultasPorUsuario(1)).thenReturn(List.of());

        mockMvc.perform(get("/multas")
                .with(user("aluno@biblioteca.com").authorities(() -> "Aluno")))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("Cadastrar Nova Multa"))));
    }

    @Test
    @WithMockUser(authorities = { "Admin" })
    @DisplayName("GET /multas/cadastrarMulta - Admin acessa formulário de cadastro")
    void testCadastrarMultaForm() throws Exception {
        mockMvc.perform(get("/multas/cadastrarMulta"))
                .andExpect(status().isOk())
                .andExpect(view().name("multas/cadastrarMulta"))
                .andExpect(model().attributeExists("multa"))
                .andExpect(model().attributeExists("listaUsuarios"))
                .andExpect(model().attributeExists("listaEmprestimos"));
    }

    @Test
    @WithMockUser(authorities = { "Aluno" })
    @DisplayName("GET /multas/cadastrarMulta - Aluno não tem acesso ao formulário de cadastro")
    void testCadastrarMultaFormNotAuthorized() throws Exception {
        mockMvc.perform(get("/multas/cadastrarMulta"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = { "Professor" })
    @DisplayName("POST /multas/salvarMulta - Professor salva multa válida")
    void testSalvarMultaValida() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setId(1);

        Emprestimo emprestimo = new Emprestimo();
        emprestimo.setId(1L);

        Multas multa = new Multas();
        multa.setUsuario(usuario);
        multa.setEmprestimo(emprestimo);
        multa.setValor(new BigDecimal("20.00"));
        multa.setDataMulta(LocalDate.now());
        multa.setMotivo("Atraso na devolução");
        multa.setPaga(false);

        when(usuarioService.buscarPorId(1)).thenReturn(usuario);
        when(emprestimoService.buscarEmprestimoPorId(1L)).thenReturn(emprestimo);

        mockMvc.perform(post("/multas/salvarMulta")
                        .with(csrf())
                        .flashAttr("multa", multa))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/multas"));

        verify(multaService).salvarMulta(any(Multas.class));
    }

    @Test
    @WithMockUser(authorities = { "Professor" })
    @DisplayName("GET /multas/editarMulta/{id} - Professor acessa edição")
    void testEditarMultaAuthorized() throws Exception {
        Multas multa = testCreateMultasList().get(0);

        when(multaService.getMultaById(1L)).thenReturn(multa);

        mockMvc.perform(get("/multas/editarMulta/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("multas/editarMulta"))
                .andExpect(model().attributeExists("multa"))
                .andExpect(model().attributeExists("listaUsuarios"))
                .andExpect(model().attributeExists("listaEmprestimos"));
    }

    @Test
    @WithMockUser(authorities = { "Aluno" })
    @DisplayName("GET /multas/editarMulta/{id} - Aluno não tem acesso à edição")
    void testEditarMultaNotAuthorized() throws Exception {
        mockMvc.perform(get("/multas/editarMulta/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = { "Admin" })
    @DisplayName("GET /multas/deletarMulta/{id} - Admin deleta multa")
    void testDeletarMulta() throws Exception {
        mockMvc.perform(get("/multas/deletarMulta/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/multas"));

        verify(multaService).deletarMulta(1L);
    }

    @Test
    @WithMockUser(authorities = { "Aluno" })
    @DisplayName("GET /multas/deletarMulta/{id} - Aluno não pode deletar multa")
    void testDeletarMultaNotAuthorized() throws Exception {
        mockMvc.perform(get("/multas/deletarMulta/1"))
                .andExpect(status().isForbidden());

        verify(multaService, never()).deletarMulta(1L);
    }

    @Test
    @WithMockUser(authorities = { "Admin" })
    @DisplayName("POST /multas/salvarMulta - Admin salva multa completa")
    void testSalvarMultaCompleta() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setId(2);

        Emprestimo emprestimo = new Emprestimo();
        emprestimo.setId(2L);

        Multas multa = new Multas();
        multa.setUsuario(usuario);
        multa.setEmprestimo(emprestimo);
        multa.setValor(new BigDecimal("50.00"));
        multa.setDataMulta(LocalDate.now());
        multa.setMotivo("Perda do livro");
        multa.setPaga(false);

        when(usuarioService.buscarPorId(2)).thenReturn(usuario);
        when(emprestimoService.buscarEmprestimoPorId(2L)).thenReturn(emprestimo);

        mockMvc.perform(post("/multas/salvarMulta")
                        .with(csrf())
                        .flashAttr("multa", multa))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/multas"));

        verify(multaService).salvarMulta(any(Multas.class));
    }

    @Test
    @WithMockUser(authorities = { "Professor" })
    @DisplayName("GET /multas/editarMulta/{id} - Professor pode editar multa")
    void testProfessorEditarMulta() throws Exception {
        Multas multa = testCreateMultasList().get(1);

        when(multaService.getMultaById(2L)).thenReturn(multa);

        mockMvc.perform(get("/multas/editarMulta/2"))
                .andExpect(status().isOk())
                .andExpect(view().name("multas/editarMulta"))
                .andExpect(model().attributeExists("multa"));
    }

    @Test
    @WithMockUser(authorities = { "Admin" })
    @DisplayName("POST /multas/salvarMulta - Atualizar status de multa para paga")
    void testAtualizarMultaParaPaga() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setId(1);

        Emprestimo emprestimo = new Emprestimo();
        emprestimo.setId(1L);

        Multas multa = new Multas();
        multa.setId(1L);
        multa.setUsuario(usuario);
        multa.setEmprestimo(emprestimo);
        multa.setValor(new BigDecimal("10.00"));
        multa.setDataMulta(LocalDate.now());
        multa.setMotivo("Atraso");
        multa.setPaga(true);

        when(usuarioService.buscarPorId(1)).thenReturn(usuario);
        when(emprestimoService.buscarEmprestimoPorId(1L)).thenReturn(emprestimo);

        mockMvc.perform(post("/multas/salvarMulta")
                        .with(csrf())
                        .flashAttr("multa", multa))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/multas"));

        verify(multaService).salvarMulta(any(Multas.class));
    }
}
