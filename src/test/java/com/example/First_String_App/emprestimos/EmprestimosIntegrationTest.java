package com.example.First_String_App.emprestimos;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.example.First_String_App.model.Emprestimo;
import com.example.First_String_App.model.Livro;
import com.example.First_String_App.model.Usuario;
import com.example.First_String_App.repository.EmprestimoRepository;
import com.example.First_String_App.repository.LivroRepository;
import com.example.First_String_App.repository.UsuarioRepository;

import jakarta.transaction.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test") // Usa application-test.properties
@Transactional // Limpa o banco após cada teste
public class EmprestimosIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EmprestimoRepository emprestimoRepository;

    @Autowired
    private LivroRepository livroRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Test
    @WithMockUser(authorities = { "Admin" })
    @DisplayName("Teste de integração - Salvar empréstimo no banco de dados")
    void testSalvarEmprestimoIntegration() throws Exception {
        // Cria e salva um livro
        Livro livro = new Livro();
        livro.setTitulo("Clean Code");
        livro.setIsbn("978-0132350884");
        livro.setDisponivel(true);
        livro = livroRepository.save(livro);

        // Cria e salva um usuário
        Usuario usuario = new Usuario();
        usuario.setNome("João Silva");
        usuario.setEmail("joao@test.com");
        usuario.setSenha("senha123");
        usuario = usuarioRepository.save(usuario);

        // Cria o empréstimo
        Emprestimo emprestimo = new Emprestimo();
        emprestimo.setLivro(livro);
        emprestimo.setUsuario(usuario);
        emprestimo.setDataEmprestimo(LocalDate.now());
        emprestimo.setDataPrevistaDevolucao(LocalDate.now().plusDays(7));
        emprestimo.setStatus(true);

        mockMvc.perform(post("/emprestimo/salvarEmprestimo")
                .with(csrf())
                .flashAttr("emprestimo", emprestimo))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/emprestimo"));

        // Verifica no banco se foi salvo
        List<Emprestimo> emprestimos = emprestimoRepository.findAll();
        assertTrue(emprestimos.stream()
                .anyMatch(e -> e.getLivro().getTitulo().equals("Clean Code")));
    }

    @Test
    @WithMockUser(authorities = { "Professor" })
    @DisplayName("Teste de integração - Editar empréstimo existente")
    void testEditarEmprestimoIntegration() throws Exception {
        // Cria dados necessários
        Livro livro = new Livro();
        livro.setTitulo("Design Patterns");
        livro.setDisponivel(true);
        livro = livroRepository.save(livro);

        Usuario usuario = new Usuario();
        usuario.setNome("Maria Santos");
        usuario.setEmail("maria@test.com");
        usuario.setSenha("senha456");
        usuario = usuarioRepository.save(usuario);

        // Salva o empréstimo
        Emprestimo emprestimo = new Emprestimo();
        emprestimo.setLivro(livro);
        emprestimo.setUsuario(usuario);
        emprestimo.setDataEmprestimo(LocalDate.now());
        emprestimo.setDataPrevistaDevolucao(LocalDate.now().plusDays(7));
        emprestimo.setStatus(true);
        emprestimo = emprestimoRepository.save(emprestimo);

        // Atualiza o empréstimo
        emprestimo.setDataPrevistaDevolucao(LocalDate.now().plusDays(14));
        emprestimo.setObservacoes("Renovação solicitada");

        mockMvc.perform(post("/emprestimo/salvarEmprestimo")
                .with(csrf())
                .flashAttr("emprestimo", emprestimo))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/emprestimo"));

        // Verifica se foi atualizado no banco
        Optional<Emprestimo> emprestimoAtualizado = emprestimoRepository.findById(emprestimo.getId());
        assertTrue(emprestimoAtualizado.isPresent());
        assertEquals("Renovação solicitada", emprestimoAtualizado.get().getObservacoes());
    }

    @Test
    @WithMockUser(authorities = { "Admin" })
    @DisplayName("Teste de integração - Deletar empréstimo do banco")
    void testDeletarEmprestimoIntegration() throws Exception {
        // Cria dados necessários
        Livro livro = new Livro();
        livro.setTitulo("Refactoring");
        livro.setDisponivel(true);
        livro = livroRepository.save(livro);

        Usuario usuario = new Usuario();
        usuario.setNome("Pedro Costa");
        usuario.setEmail("pedro@test.com");
        usuario.setSenha("senha789");
        usuario = usuarioRepository.save(usuario);

        // Salva o empréstimo
        Emprestimo emprestimo = new Emprestimo();
        emprestimo.setLivro(livro);
        emprestimo.setUsuario(usuario);
        emprestimo.setDataEmprestimo(LocalDate.now());
        emprestimo.setDataPrevistaDevolucao(LocalDate.now().plusDays(7));
        emprestimo.setStatus(true);
        emprestimo = emprestimoRepository.save(emprestimo);

        Long emprestimoId = emprestimo.getId();

        // Deleta o empréstimo
        mockMvc.perform(get("/emprestimo/deletarEmprestimo/" + emprestimoId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/emprestimo"));

        // Verifica se foi deletado do banco
        assertFalse(emprestimoRepository.findById(emprestimoId).isPresent());
    }

    @Test
    @WithMockUser(authorities = { "Admin" })
    @DisplayName("Teste de integração - Listar todos os empréstimos cadastrados")
    void testListarEmprestimosIntegration() throws Exception {
        // Cria livro 1
        Livro livro1 = new Livro();
        livro1.setTitulo("Livro Teste 1");
        livro1.setDisponivel(true);
        livro1 = livroRepository.save(livro1);

        // Cria usuário 1
        Usuario usuario1 = new Usuario();
        usuario1.setNome("Usuario Teste 1");
        usuario1.setEmail("usuario1@test.com");
        usuario1.setSenha("senha1");
        usuario1 = usuarioRepository.save(usuario1);

        // Salva empréstimo 1
        Emprestimo emprestimo1 = new Emprestimo();
        emprestimo1.setLivro(livro1);
        emprestimo1.setUsuario(usuario1);
        emprestimo1.setDataEmprestimo(LocalDate.now());
        emprestimo1.setDataPrevistaDevolucao(LocalDate.now().plusDays(7));
        emprestimo1.setStatus(true);
        emprestimoRepository.save(emprestimo1);

        // Cria livro 2
        Livro livro2 = new Livro();
        livro2.setTitulo("Livro Teste 2");
        livro2.setDisponivel(true);
        livro2 = livroRepository.save(livro2);

        // Cria usuário 2
        Usuario usuario2 = new Usuario();
        usuario2.setNome("Usuario Teste 2");
        usuario2.setEmail("usuario2@test.com");
        usuario2.setSenha("senha2");
        usuario2 = usuarioRepository.save(usuario2);

        // Salva empréstimo 2
        Emprestimo emprestimo2 = new Emprestimo();
        emprestimo2.setLivro(livro2);
        emprestimo2.setUsuario(usuario2);
        emprestimo2.setDataEmprestimo(LocalDate.now());
        emprestimo2.setDataPrevistaDevolucao(LocalDate.now().plusDays(14));
        emprestimo2.setStatus(true);
        emprestimoRepository.save(emprestimo2);

        // Lista os empréstimos
        mockMvc.perform(get("/emprestimo"))
                .andExpect(status().isOk())
                .andExpect(view().name("emprestimo/listarEmprestimos"));

        // Verifica se ambos estão no banco
        assertTrue(emprestimoRepository.findAll().size() >= 2);
    }

    @Test
    @WithMockUser(authorities = { "Aluno" })
    @DisplayName("Teste de integração - Aluno cria empréstimo")
    void testAlunoCriarEmprestimoIntegration() throws Exception {
        // Cria livro
        Livro livro = new Livro();
        livro.setTitulo("Test Driven Development");
        livro.setDisponivel(true);
        livro = livroRepository.save(livro);

        // Cria usuário
        Usuario usuario = new Usuario();
        usuario.setNome("Aluno Teste");
        usuario.setEmail("aluno@test.com");
        usuario.setSenha("senhaAluno");
        usuario = usuarioRepository.save(usuario);

        // Cria empréstimo
        Emprestimo emprestimo = new Emprestimo();
        emprestimo.setLivro(livro);
        emprestimo.setUsuario(usuario);
        emprestimo.setDataEmprestimo(LocalDate.now());
        emprestimo.setDataPrevistaDevolucao(LocalDate.now().plusDays(7));
        emprestimo.setStatus(true);

        mockMvc.perform(post("/emprestimo/salvarEmprestimo")
                .with(csrf())
                .flashAttr("emprestimo", emprestimo))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/emprestimo"));

        // Verifica no banco
        assertTrue(emprestimoRepository.findAll().stream()
                .anyMatch(e -> e.getLivro().getTitulo().equals("Test Driven Development")));
    }

    @Test
    @WithMockUser(authorities = { "Professor" })
    @DisplayName("Teste de integração - Acessar formulário de edição")
    void testAcessarFormularioEdicaoIntegration() throws Exception {
        // Cria dados necessários
        Livro livro = new Livro();
        livro.setTitulo("Livro para Edição");
        livro.setDisponivel(true);
        livro = livroRepository.save(livro);

        Usuario usuario = new Usuario();
        usuario.setNome("Usuario Edição");
        usuario.setEmail("edicao@test.com");
        usuario.setSenha("senhaEdicao");
        usuario = usuarioRepository.save(usuario);

        // Salva empréstimo
        Emprestimo emprestimo = new Emprestimo();
        emprestimo.setLivro(livro);
        emprestimo.setUsuario(usuario);
        emprestimo.setDataEmprestimo(LocalDate.now());
        emprestimo.setDataPrevistaDevolucao(LocalDate.now().plusDays(7));
        emprestimo.setStatus(true);
        emprestimo = emprestimoRepository.save(emprestimo);

        // Acessa o formulário de edição
        mockMvc.perform(get("/emprestimo/editarEmprestimo/" + emprestimo.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("emprestimo/editarEmprestimo"));
    }

    @Test
    @WithMockUser(authorities = { "Admin" })
    @DisplayName("Teste de integração - Salvar empréstimo com todos os campos preenchidos")
    void testSalvarEmprestimoCompletoIntegration() throws Exception {
        // Cria livro
        Livro livro = new Livro();
        livro.setTitulo("The Pragmatic Programmer");
        livro.setDisponivel(true);
        livro = livroRepository.save(livro);

        // Cria usuário
        Usuario usuario = new Usuario();
        usuario.setNome("Carlos Silva");
        usuario.setEmail("carlos@test.com");
        usuario.setSenha("senhaCarlos");
        usuario = usuarioRepository.save(usuario);

        // Cria empréstimo completo
        Emprestimo emprestimo = new Emprestimo();
        emprestimo.setLivro(livro);
        emprestimo.setUsuario(usuario);
        emprestimo.setDataEmprestimo(LocalDate.now());
        emprestimo.setDataPrevistaDevolucao(LocalDate.now().plusDays(14));
        emprestimo.setStatus(true);
        emprestimo.setObservacoes("Empréstimo para pesquisa acadêmica");
        emprestimo.setRenovado(false);

        mockMvc.perform(post("/emprestimo/salvarEmprestimo")
                .with(csrf())
                .flashAttr("emprestimo", emprestimo))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/emprestimo"));

        // Verifica todos os campos no banco
        Optional<Emprestimo> emprestimoSalvo = emprestimoRepository.findAll()
                .stream()
                .filter(e -> e.getUsuario().getNome().equals("Carlos Silva"))
                .findFirst();

        assertTrue(emprestimoSalvo.isPresent());
        assertEquals("The Pragmatic Programmer", emprestimoSalvo.get().getLivro().getTitulo());
        assertEquals("Empréstimo para pesquisa acadêmica", emprestimoSalvo.get().getObservacoes());
        assertTrue(emprestimoSalvo.get().getStatus());
    }

    @Test
    @WithMockUser(authorities = { "Professor" })
    @DisplayName("Teste de integração - Professor pode deletar empréstimo")
    void testProfessorDeletarEmprestimoIntegration() throws Exception {
        // Cria dados necessários
        Livro livro = new Livro();
        livro.setTitulo("Livro Deletar");
        livro.setDisponivel(true);
        livro = livroRepository.save(livro);

        Usuario usuario = new Usuario();
        usuario.setNome("Usuario Deletar");
        usuario.setEmail("deletar@test.com");
        usuario.setSenha("senhaDeletar");
        usuario = usuarioRepository.save(usuario);

        // Salva empréstimo
        Emprestimo emprestimo = new Emprestimo();
        emprestimo.setLivro(livro);
        emprestimo.setUsuario(usuario);
        emprestimo.setDataEmprestimo(LocalDate.now());
        emprestimo.setDataPrevistaDevolucao(LocalDate.now().plusDays(7));
        emprestimo.setStatus(true);
        emprestimo = emprestimoRepository.save(emprestimo);

        Long emprestimoId = emprestimo.getId();

        // Professor deleta
        mockMvc.perform(get("/emprestimo/deletarEmprestimo/" + emprestimoId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/emprestimo"));

        // Verifica se foi deletado
        assertFalse(emprestimoRepository.findById(emprestimoId).isPresent());
    }

    @Test
    @WithMockUser(authorities = { "Aluno" })
    @DisplayName("Teste de integração - Aluno não pode deletar empréstimo")
    void testAlunoNaoPodeDeletarEmprestimoIntegration() throws Exception {
        mockMvc.perform(get("/emprestimo/deletarEmprestimo/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = { "Aluno" })
    @DisplayName("Teste de integração - Aluno não pode editar empréstimo")
    void testAlunoNaoPodeEditarEmprestimoIntegration() throws Exception {
        mockMvc.perform(get("/emprestimo/editarEmprestimo/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = { "Admin" })
    @DisplayName("Teste de integração - Atualizar status do empréstimo")
    void testAtualizarStatusEmprestimoIntegration() throws Exception {
        // Cria dados
        Livro livro = new Livro();
        livro.setTitulo("Livro Status");
        livro.setDisponivel(true);
        livro = livroRepository.save(livro);

        Usuario usuario = new Usuario();
        usuario.setNome("Usuario Status");
        usuario.setEmail("status@test.com");
        usuario.setSenha("senhaStatus");
        usuario = usuarioRepository.save(usuario);

        // Salva empréstimo
        Emprestimo emprestimo = new Emprestimo();
        emprestimo.setLivro(livro);
        emprestimo.setUsuario(usuario);
        emprestimo.setDataEmprestimo(LocalDate.now());
        emprestimo.setDataPrevistaDevolucao(LocalDate.now().plusDays(7));
        emprestimo.setStatus(true);
        emprestimo = emprestimoRepository.save(emprestimo);

        // Atualiza status para devolvido
        emprestimo.setStatus(false);
        emprestimo.setDataDevolucao(LocalDate.now());

        mockMvc.perform(post("/emprestimo/salvarEmprestimo")
                .with(csrf())
                .flashAttr("emprestimo", emprestimo))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/emprestimo"));

        // Verifica se foi atualizado
        Optional<Emprestimo> emprestimoAtualizado = emprestimoRepository.findById(emprestimo.getId());
        assertTrue(emprestimoAtualizado.isPresent());
        assertFalse(emprestimoAtualizado.get().getStatus());
        assertEquals(LocalDate.now(), emprestimoAtualizado.get().getDataDevolucao());
    }
}
