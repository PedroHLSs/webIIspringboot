package com.example.First_String_App.livros;

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
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.example.First_String_App.model.Livro;
import com.example.First_String_App.repository.LivroRepository;

import jakarta.transaction.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test") // Usa application-test.properties
@Transactional // Limpa o banco após cada teste
public class LivrosIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LivroRepository livroRepository;

    @Test
    @WithMockUser(authorities = { "Admin" })
    @DisplayName("Teste de integração - Salvar livro no banco de dados")
    void testSalvarLivroIntegration() throws Exception {
        Livro livro = new Livro();
        livro.setTitulo("Clean Code");
        livro.setIsbn("978-0132350884");
        livro.setEditora("Prentice Hall");
        livro.setAno(LocalDate.of(2008, 8, 1));
        livro.setDescricao("A Handbook of Agile Software Craftsmanship");
        livro.setDisponivel(true);

        mockMvc.perform(post("/livros/salvarLivro")
                .with(csrf())
                .flashAttr("livro", livro))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/livros"));

        // Verifica no banco se foi salvo
        assertTrue(livroRepository.findAll()
                .stream()
                .anyMatch(l -> "Clean Code".equals(l.getTitulo())));
    }

    @Test
    @WithMockUser(authorities = { "Professor" })
    @DisplayName("Teste de integração - Editar livro existente")
    void testEditarLivroIntegration() throws Exception {
        // Primeiro salva um livro
        Livro livro = new Livro();
        livro.setTitulo("Design Patterns");
        livro.setIsbn("978-0201633612");
        livro.setDisponivel(true);
        livro = livroRepository.save(livro);

        // Agora edita
        livro.setTitulo("Design Patterns - Atualizado");
        livro.setEditora("Addison-Wesley");

        mockMvc.perform(post("/livros/salvarLivro")
                .with(csrf())
                .flashAttr("livro", livro))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/livros"));

        // Verifica se foi atualizado no banco
        Optional<Livro> livroAtualizado = livroRepository.findById(livro.getId());
        assertTrue(livroAtualizado.isPresent());
        assertEquals("Design Patterns - Atualizado", livroAtualizado.get().getTitulo());
        assertEquals("Addison-Wesley", livroAtualizado.get().getEditora());
    }

    @Test
    @WithMockUser(authorities = { "Admin" })
    @DisplayName("Teste de integração - Deletar livro do banco")
    void testDeletarLivroIntegration() throws Exception {
        // Salva um livro
        Livro livro = new Livro();
        livro.setTitulo("Refactoring");
        livro.setIsbn("978-0201485677");
        livro.setDisponivel(true);
        livro = livroRepository.save(livro);

        Long livroId = livro.getId();

        // Deleta o livro
        mockMvc.perform(get("/livros/deletarLivro/" + livroId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/livros"));

        // Verifica se foi deletado do banco
        assertFalse(livroRepository.findById(livroId).isPresent());
    }

    @Test
    @WithMockUser(authorities = { "Aluno" })
    @DisplayName("Teste de integração - Aluno visualiza detalhes do livro")
    void testVisualizarDetalhesLivroIntegration() throws Exception {
        // Salva um livro
        Livro livro = new Livro();
        livro.setTitulo("Test Driven Development");
        livro.setIsbn("978-0321146533");
        livro.setDescricao("By Example");
        livro.setDisponivel(true);
        livro = livroRepository.save(livro);

        // Visualiza os detalhes
        mockMvc.perform(get("/livros/detalhesLivro/" + livro.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("livros/detalhesLivro"));
    }

    @Test
    @WithMockUser(authorities = { "Admin" })
    @DisplayName("Teste de integração - Listar todos os livros cadastrados")
    void testListarLivrosIntegration() throws Exception {
        // Salva alguns livros
        Livro livro1 = new Livro();
        livro1.setTitulo("Livro Teste 1");
        livro1.setIsbn("111-1111111111");
        livro1.setDisponivel(true);
        livroRepository.save(livro1);

        Livro livro2 = new Livro();
        livro2.setTitulo("Livro Teste 2");
        livro2.setIsbn("222-2222222222");
        livro2.setDisponivel(true);
        livroRepository.save(livro2);

        // Lista os livros
        mockMvc.perform(get("/livros"))
                .andExpect(status().isOk())
                .andExpect(view().name("livros/listarLivros"));

        // Verifica se ambos estão no banco
        assertTrue(livroRepository.findAll().size() >= 2);
    }

    @Test
    @WithMockUser(authorities = { "Admin" })
    @DisplayName("Teste de integração - Salvar livro com todos os campos preenchidos")
    void testSalvarLivroCompletoIntegration() throws Exception {
        Livro livro = new Livro();
        livro.setTitulo("The Pragmatic Programmer");
        livro.setIsbn("978-0135957059");
        livro.setEditora("Addison-Wesley");
        livro.setAno(LocalDate.of(2019, 9, 13));
        livro.setDescricao("Your Journey to Mastery");
        livro.setLocalizacao("Prateleira A - Seção 3");
        livro.setImagemUrl("https://example.com/pragmatic.jpg");
        livro.setDisponivel(true);

        mockMvc.perform(post("/livros/salvarLivro")
                .with(csrf())
                .flashAttr("livro", livro))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/livros"));

        // Verifica todos os campos no banco
        Optional<Livro> livroSalvo = livroRepository.findAll()
                .stream()
                .filter(l -> "The Pragmatic Programmer".equals(l.getTitulo()))
                .findFirst();

        assertTrue(livroSalvo.isPresent());
        assertEquals("978-0135957059", livroSalvo.get().getIsbn());
        assertEquals("Addison-Wesley", livroSalvo.get().getEditora());
        assertEquals("Prateleira A - Seção 3", livroSalvo.get().getLocalizacao());
        assertTrue(livroSalvo.get().getDisponivel());
    }

    @Test
    @WithMockUser(authorities = { "Professor" })
    @DisplayName("Teste de integração - Acessar formulário de edição")
    void testAcessarFormularioEdicaoIntegration() throws Exception {
        // Salva um livro
        Livro livro = new Livro();
        livro.setTitulo("Livro para Edição");
        livro.setIsbn("999-9999999999");
        livro.setDisponivel(true);
        livro = livroRepository.save(livro);

        // Acessa o formulário de edição
        mockMvc.perform(get("/livros/editarLivro/" + livro.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("livros/editarLivro"));
    }
}
