package com.example.First_String_App.autor;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.example.First_String_App.model.Autor;
import com.example.First_String_App.repository.AutorRepository;

import jakarta.transaction.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test") // Usa application-test.properties
@Transactional // Limpa o banco após cada teste
public class AutoresIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AutorRepository autorRepository;

    @Test
    @WithMockUser(authorities = { "Admin" })
    @DisplayName("Teste de integração - Salvar autor no banco de dados")
    void testSalvarAutorIntegration() throws Exception {
        Autor autor = new Autor();
        autor.setNome("Robert C. Martin");
        autor.setNacionalidade("Americano");

        mockMvc.perform(post("/autores/salvarAutor")
                .with(csrf())
                .flashAttr("autor", autor))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/autores"));

        // Verifica no banco se foi salvo
        assertTrue(autorRepository.findAll()
                .stream()
                .anyMatch(a -> "Robert C. Martin".equals(a.getNome())));
    }

    @Test
    @WithMockUser(authorities = { "Professor" })
    @DisplayName("Teste de integração - Editar autor existente")
    void testEditarAutorIntegration() throws Exception {
        // Primeiro salva um autor
        Autor autor = new Autor();
        autor.setNome("Martin Fowler");
        autor.setNacionalidade("Britânico");
        autor = autorRepository.save(autor);

        // Agora edita
        autor.setNome("Martin Fowler - Atualizado");
        autor.setNacionalidade("Inglês");

        mockMvc.perform(post("/autores/salvarAutor")
                .with(csrf())
                .flashAttr("autor", autor))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/autores"));

        // Verifica se foi atualizado no banco
        Optional<Autor> autorAtualizado = autorRepository.findById(autor.getId());
        assertTrue(autorAtualizado.isPresent());
        assertEquals("Martin Fowler - Atualizado", autorAtualizado.get().getNome());
        assertEquals("Inglês", autorAtualizado.get().getNacionalidade());
    }

    @Test
    @WithMockUser(authorities = { "Admin" })
    @DisplayName("Teste de integração - Deletar autor do banco")
    void testDeletarAutorIntegration() throws Exception {
        // Salva um autor
        Autor autor = new Autor();
        autor.setNome("Kent Beck");
        autor.setNacionalidade("Americano");
        autor = autorRepository.save(autor);

        Long autorId = autor.getId();

        // Deleta o autor
        mockMvc.perform(get("/autores/deletarAutor/" + autorId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/autores"));

        // Verifica se foi deletado do banco
        assertFalse(autorRepository.findById(autorId).isPresent());
    }

    @Test
    @WithMockUser(authorities = { "Professor" })
    @DisplayName("Teste de integração - Professor pode deletar autor")
    void testProfessorDeletarAutorIntegration() throws Exception {
        // Salva um autor
        Autor autor = new Autor();
        autor.setNome("Eric Evans");
        autor.setNacionalidade("Americano");
        autor = autorRepository.save(autor);

        Long autorId = autor.getId();

        // Professor deleta o autor
        mockMvc.perform(get("/autores/deletarAutor/" + autorId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/autores"));

        // Verifica se foi deletado do banco
        assertFalse(autorRepository.findById(autorId).isPresent());
    }

    @Test
    @WithMockUser(authorities = { "Admin" })
    @DisplayName("Teste de integração - Listar todos os autores cadastrados")
    void testListarAutoresIntegration() throws Exception {
        // Salva alguns autores
        Autor autor1 = new Autor();
        autor1.setNome("Autor Teste 1");
        autor1.setNacionalidade("Brasileiro");
        autorRepository.save(autor1);

        Autor autor2 = new Autor();
        autor2.setNome("Autor Teste 2");
        autor2.setNacionalidade("Português");
        autorRepository.save(autor2);

        // Lista os autores
        mockMvc.perform(get("/autores"))
                .andExpect(status().isOk())
                .andExpect(view().name("autores/listarAutores"));

        // Verifica se ambos estão no banco
        assertTrue(autorRepository.findAll().size() >= 2);
    }

    @Test
    @WithMockUser(authorities = { "Admin" })
    @DisplayName("Teste de integração - Salvar autor com todos os campos preenchidos")
    void testSalvarAutorCompletoIntegration() throws Exception {
        Autor autor = new Autor();
        autor.setNome("Joshua Bloch");
        autor.setNacionalidade("Americano");

        mockMvc.perform(post("/autores/salvarAutor")
                .with(csrf())
                .flashAttr("autor", autor))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/autores"));

        // Verifica todos os campos no banco
        Optional<Autor> autorSalvo = autorRepository.findAll()
                .stream()
                .filter(a -> "Joshua Bloch".equals(a.getNome()))
                .findFirst();

        assertTrue(autorSalvo.isPresent());
        assertEquals("Joshua Bloch", autorSalvo.get().getNome());
        assertEquals("Americano", autorSalvo.get().getNacionalidade());
    }

    @Test
    @WithMockUser(authorities = { "Professor" })
    @DisplayName("Teste de integração - Acessar formulário de edição")
    void testAcessarFormularioEdicaoIntegration() throws Exception {
        // Salva um autor
        Autor autor = new Autor();
        autor.setNome("Autor para Edição");
        autor.setNacionalidade("Canadense");
        autor = autorRepository.save(autor);

        // Acessa o formulário de edição
        mockMvc.perform(get("/autores/editarAutor/" + autor.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("autores/editarAutor"));
    }

    @Test
    @WithMockUser(authorities = { "Admin" })
    @DisplayName("Teste de integração - Acessar formulário de cadastro")
    void testAcessarFormularioCadastroIntegration() throws Exception {
        // Acessa o formulário de cadastro
        mockMvc.perform(get("/autores/cadastrarAutor"))
                .andExpect(status().isOk())
                .andExpect(view().name("autores/cadastrarAutor"));
    }

    @Test
    @WithMockUser(authorities = { "Professor" })
    @DisplayName("Teste de integração - Professor salva novo autor")
    void testProfessorSalvarAutorIntegration() throws Exception {
        Autor autor = new Autor();
        autor.setNome("Uncle Bob");
        autor.setNacionalidade("Americano");

        mockMvc.perform(post("/autores/salvarAutor")
                .with(csrf())
                .flashAttr("autor", autor))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/autores"));

        // Verifica no banco se foi salvo
        assertTrue(autorRepository.findAll()
                .stream()
                .anyMatch(a -> "Uncle Bob".equals(a.getNome())));
    }

    @Test
    @WithMockUser(authorities = { "Aluno" })
    @DisplayName("Teste de integração - Aluno não tem acesso à listagem de autores")
    void testAlunoNaoTemAcessoListagemIntegration() throws Exception {
        mockMvc.perform(get("/autores"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = { "Aluno" })
    @DisplayName("Teste de integração - Aluno não pode cadastrar autor")
    void testAlunoNaoPodeCadastrarAutorIntegration() throws Exception {
        mockMvc.perform(get("/autores/cadastrarAutor"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = { "Admin" })
    @DisplayName("Teste de integração - Atualizar nacionalidade do autor")
    void testAtualizarNacionalidadeAutorIntegration() throws Exception {
        // Salva um autor
        Autor autor = new Autor();
        autor.setNome("Grady Booch");
        autor.setNacionalidade("Canadense");
        autor = autorRepository.save(autor);

        // Atualiza a nacionalidade
        autor.setNacionalidade("Americano");

        mockMvc.perform(post("/autores/salvarAutor")
                .with(csrf())
                .flashAttr("autor", autor))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/autores"));

        // Verifica se foi atualizado
        Optional<Autor> autorAtualizado = autorRepository.findById(autor.getId());
        assertTrue(autorAtualizado.isPresent());
        assertEquals("Americano", autorAtualizado.get().getNacionalidade());
    }
}
