package com.example.First_String_App.usuario;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

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

import com.example.First_String_App.model.Usuario;
import com.example.First_String_App.repository.UsuarioRepository;

import jakarta.transaction.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class UsuarioIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Test
    @DisplayName("Teste de integração - Criar novo usuário no banco")
    void testCriarUsuarioIntegration() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setNome("João Silva");
        usuario.setEmail("joao.integration@test.com");
        usuario.setSenha("senha123");
        usuario.setRoles(List.of("Aluno"));

        mockMvc.perform(post("/criarUsuario")
                .with(csrf())
                .flashAttr("usuario", usuario))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/registro"));

        assertTrue(usuarioRepository.findAll()
                .stream()
                .anyMatch(u -> "joao.integration@test.com".equals(u.getEmail())));
    }

    @Test
    @WithMockUser(authorities = { "Admin" })
    @DisplayName("Teste de integração - Listar todos os usuários cadastrados")
    void testListarUsuariosIntegration() throws Exception {
        Usuario usuario1 = new Usuario();
        usuario1.setNome("Maria Santos");
        usuario1.setEmail("maria.test@test.com");
        usuario1.setSenha("senha456");
        usuario1.setRoles(List.of("Professor"));
        usuarioRepository.save(usuario1);

        Usuario usuario2 = new Usuario();
        usuario2.setNome("Pedro Alves");
        usuario2.setEmail("pedro.test@test.com");
        usuario2.setSenha("senha789");
        usuario2.setRoles(List.of("Aluno"));
        usuarioRepository.save(usuario2);

        mockMvc.perform(get("/usuarios"))
                .andExpect(status().isOk())
                .andExpect(view().name("user/listarUsuarios"));

        assertTrue(usuarioRepository.findAll().size() >= 2);
    }

    @Test
    @WithMockUser(authorities = { "Admin" })
    @DisplayName("Teste de integração - Editar usuário existente")
    void testEditarUsuarioIntegration() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setNome("Ana Costa");
        usuario.setEmail("ana.test@test.com");
        usuario.setSenha("senha123");
        usuario.setRoles(List.of("Aluno"));
        usuario = usuarioRepository.save(usuario);

        usuario.setNome("Ana Costa Atualizada");
        usuario.setEmail("ana.atualizada@test.com");

        mockMvc.perform(post("/usuarios/atualizarUsuario")
                .with(csrf())
                .flashAttr("usuario", usuario))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/usuarios"));

        Optional<Usuario> usuarioAtualizado = usuarioRepository.findById(usuario.getId());
        assertTrue(usuarioAtualizado.isPresent());
        assertEquals("Ana Costa Atualizada", usuarioAtualizado.get().getNome());
        assertEquals("ana.atualizada@test.com", usuarioAtualizado.get().getEmail());
    }

    @Test
    @WithMockUser(authorities = { "Admin" })
    @DisplayName("Teste de integração - Deletar usuário do banco")
    void testDeletarUsuarioIntegration() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setNome("Carlos Lima");
        usuario.setEmail("carlos.test@test.com");
        usuario.setSenha("senha123");
        usuario.setRoles(List.of("Aluno"));
        usuario = usuarioRepository.save(usuario);

        Integer usuarioId = usuario.getId();

        mockMvc.perform(get("/usuarios/deletarUsuario/" + usuarioId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/usuarios"));

        assertFalse(usuarioRepository.findById(usuarioId).isPresent());
    }

    @Test
    @WithMockUser(authorities = { "Admin" })
    @DisplayName("Teste de integração - Atualizar senha do usuário")
    void testAtualizarSenhaIntegration() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setNome("Fernanda Souza");
        usuario.setEmail("fernanda.test@test.com");
        usuario.setSenha("senhaAntiga");
        usuario.setRoles(List.of("Professor"));
        usuario = usuarioRepository.save(usuario);

        String senhaAntigaHash = usuario.getSenha();

        mockMvc.perform(post("/usuarios/atualizarSenha")
                .with(csrf())
                .param("id", String.valueOf(usuario.getId()))
                .param("novaSenha", "novaSenha123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/usuarios"));

        Optional<Usuario> usuarioAtualizado = usuarioRepository.findById(usuario.getId());
        assertTrue(usuarioAtualizado.isPresent());
        // A senha deve ter sido atualizada (hash diferente)
        assertNotEquals(senhaAntigaHash, usuarioAtualizado.get().getSenha());
    }

    @Test
    @WithMockUser(authorities = { "Admin" })
    @DisplayName("Teste de integração - Acessar formulário de edição")
    void testAcessarFormularioEdicaoIntegration() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setNome("Roberto Silva");
        usuario.setEmail("roberto.test@test.com");
        usuario.setSenha("senha123");
        usuario.setRoles(List.of("Aluno"));
        usuario = usuarioRepository.save(usuario);

        mockMvc.perform(get("/usuarios/editarUsuario/" + usuario.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("user/editarUsuario"));
    }

    @Test
    @WithMockUser(authorities = { "Admin" })
    @DisplayName("Teste de integração - Editar usuário inexistente redireciona")
    void testEditarUsuarioInexistenteIntegration() throws Exception {
        mockMvc.perform(get("/usuarios/editarUsuario/99999"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/usuarios"));
    }

    @Test
    @DisplayName("Teste de integração - Criar usuário com todos os campos")
    void testCriarUsuarioCompletoIntegration() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setNome("Sandra Oliveira");
        usuario.setEmail("sandra.complete@test.com");
        usuario.setSenha("senhaForte123");
        usuario.setRoles(List.of("Admin", "Professor"));

        mockMvc.perform(post("/criarUsuario")
                .with(csrf())
                .flashAttr("usuario", usuario))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/registro"));

        Optional<Usuario> usuarioSalvo = usuarioRepository.findAll()
                .stream()
                .filter(u -> "sandra.complete@test.com".equals(u.getEmail()))
                .findFirst();

        assertTrue(usuarioSalvo.isPresent());
        assertEquals("Sandra Oliveira", usuarioSalvo.get().getNome());
        assertTrue(usuarioSalvo.get().getRoles().contains("Admin"));
    }

    @Test
    @DisplayName("Teste de integração - Acesso público ao formulário de registro")
    void testRegistroPublicoIntegration() throws Exception {
        mockMvc.perform(get("/registro"))
                .andExpect(status().isOk())
                .andExpect(view().name("user/registro"));
    }

    @Test
    @DisplayName("Teste de integração - Acesso público à página de login")
    void testLoginPublicoIntegration() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("user/login"));
    }

    @Test
    @WithMockUser(authorities = { "Professor" })
    @DisplayName("Teste de integração - Professor não acessa lista de usuários")
    void testProfessorNaoAcessaListaIntegration() throws Exception {
        mockMvc.perform(get("/usuarios"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = { "Admin" })
    @DisplayName("Teste de integração - Criar múltiplos usuários com roles diferentes")
    void testCriarMultiplosUsuariosIntegration() throws Exception {
        Usuario aluno = new Usuario();
        aluno.setNome("Aluno Test");
        aluno.setEmail("aluno.multi@test.com");
        aluno.setSenha("senha123");
        aluno.setRoles(List.of("Aluno"));

        Usuario professor = new Usuario();
        professor.setNome("Professor Test");
        professor.setEmail("professor.multi@test.com");
        professor.setSenha("senha456");
        professor.setRoles(List.of("Professor"));

        Usuario admin = new Usuario();
        admin.setNome("Admin Test");
        admin.setEmail("admin.multi@test.com");
        admin.setSenha("senha789");
        admin.setRoles(List.of("Admin"));

        usuarioRepository.save(aluno);
        usuarioRepository.save(professor);
        usuarioRepository.save(admin);

        mockMvc.perform(get("/usuarios"))
                .andExpect(status().isOk())
                .andExpect(view().name("user/listarUsuarios"));

        assertTrue(usuarioRepository.findAll().size() >= 3);
    }
}
