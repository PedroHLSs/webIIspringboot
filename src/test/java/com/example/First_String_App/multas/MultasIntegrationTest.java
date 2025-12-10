package com.example.First_String_App.multas;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.math.BigDecimal;
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

import com.example.First_String_App.model.Emprestimo;
import com.example.First_String_App.model.Livro;
import com.example.First_String_App.model.Multas;
import com.example.First_String_App.model.Usuario;
import com.example.First_String_App.repository.EmprestimoRepository;
import com.example.First_String_App.repository.LivroRepository;
import com.example.First_String_App.repository.MultasRepository;
import com.example.First_String_App.repository.UsuarioRepository;

import jakarta.transaction.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class MultasIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MultasRepository multasRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EmprestimoRepository emprestimoRepository;

    @Autowired
    private LivroRepository livroRepository;

    private Usuario criarUsuario(String nome, String email) {
        Usuario usuario = new Usuario();
        usuario.setNome(nome);
        usuario.setEmail(email);
        usuario.setSenha("senha123");
        return usuarioRepository.save(usuario);
    }

    private Emprestimo criarEmprestimo(Usuario usuario) {
        Livro livro = new Livro();
        livro.setTitulo("Livro Teste");
        livro.setIsbn("123-4567890123");
        livro.setDisponivel(false);
        livro = livroRepository.save(livro);

        Emprestimo emprestimo = new Emprestimo();
        emprestimo.setUsuario(usuario);
        emprestimo.setLivro(livro);
        emprestimo.setDataEmprestimo(LocalDate.now());
        emprestimo.setDataPrevistaDevolucao(LocalDate.now().plusDays(14));
        emprestimo.setStatus(Boolean.TRUE);
        return emprestimoRepository.save(emprestimo);
    }

    @Test
    @WithMockUser(authorities = { "Admin" })
    @DisplayName("Teste de integração - Salvar multa no banco de dados")
    void testSalvarMultaIntegration() throws Exception {
        Usuario usuario = criarUsuario("João Silva", "joao@test.com");
        Emprestimo emprestimo = criarEmprestimo(usuario);

        Multas multa = new Multas();
        multa.setUsuario(usuario);
        multa.setEmprestimo(emprestimo);
        multa.setValor(new BigDecimal("15.00"));
        multa.setDataMulta(LocalDate.now());
        multa.setMotivo("Atraso na devolução");
        multa.setPaga(false);

        mockMvc.perform(post("/multas/salvarMulta")
                .with(csrf())
                .flashAttr("multa", multa))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/multas"));

        assertTrue(multasRepository.findAll()
                .stream()
                .anyMatch(m -> "Atraso na devolução".equals(m.getMotivo())));
    }

    @Test
    @WithMockUser(authorities = { "Professor" })
    @DisplayName("Teste de integração - Editar multa existente")
    void testEditarMultaIntegration() throws Exception {
        Usuario usuario = criarUsuario("Maria Santos", "maria@test.com");
        Emprestimo emprestimo = criarEmprestimo(usuario);

        Multas multa = new Multas();
        multa.setUsuario(usuario);
        multa.setEmprestimo(emprestimo);
        multa.setValor(new BigDecimal("10.00"));
        multa.setDataMulta(LocalDate.now());
        multa.setMotivo("Dano ao livro");
        multa.setPaga(false);
        multa = multasRepository.save(multa);

        multa.setValor(new BigDecimal("25.00"));
        multa.setPaga(true);

        mockMvc.perform(post("/multas/salvarMulta")
                .with(csrf())
                .flashAttr("multa", multa))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/multas"));

        Optional<Multas> multaAtualizada = multasRepository.findById(multa.getId());
        assertTrue(multaAtualizada.isPresent());
        assertEquals(new BigDecimal("25.00"), multaAtualizada.get().getValor());
        assertTrue(multaAtualizada.get().getPaga());
    }

    @Test
    @WithMockUser(authorities = { "Admin" })
    @DisplayName("Teste de integração - Deletar multa do banco")
    void testDeletarMultaIntegration() throws Exception {
        Usuario usuario = criarUsuario("Pedro Alves", "pedro@test.com");
        Emprestimo emprestimo = criarEmprestimo(usuario);

        Multas multa = new Multas();
        multa.setUsuario(usuario);
        multa.setEmprestimo(emprestimo);
        multa.setValor(new BigDecimal("20.00"));
        multa.setDataMulta(LocalDate.now());
        multa.setMotivo("Perda do livro");
        multa.setPaga(false);
        multa = multasRepository.save(multa);

        Long multaId = multa.getId();

        mockMvc.perform(get("/multas/deletarMulta/" + multaId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/multas"));

        assertFalse(multasRepository.findById(multaId).isPresent());
    }

    @Test
    @WithMockUser(authorities = { "Professor" })
    @DisplayName("Teste de integração - Listar todas as multas cadastradas")
    void testListarMultasIntegration() throws Exception {
        Usuario usuario1 = criarUsuario("Ana Costa", "ana@test.com");
        Usuario usuario2 = criarUsuario("Carlos Lima", "carlos@test.com");
        Emprestimo emprestimo1 = criarEmprestimo(usuario1);
        Emprestimo emprestimo2 = criarEmprestimo(usuario2);

        Multas multa1 = new Multas();
        multa1.setUsuario(usuario1);
        multa1.setEmprestimo(emprestimo1);
        multa1.setValor(new BigDecimal("5.00"));
        multa1.setDataMulta(LocalDate.now());
        multa1.setMotivo("Teste 1");
        multa1.setPaga(false);
        multasRepository.save(multa1);

        Multas multa2 = new Multas();
        multa2.setUsuario(usuario2);
        multa2.setEmprestimo(emprestimo2);
        multa2.setValor(new BigDecimal("10.00"));
        multa2.setDataMulta(LocalDate.now());
        multa2.setMotivo("Teste 2");
        multa2.setPaga(true);
        multasRepository.save(multa2);

        mockMvc.perform(get("/multas"))
                .andExpect(status().isOk())
                .andExpect(view().name("multas/listarMultas"));

        assertTrue(multasRepository.findAll().size() >= 2);
    }

    @Test
    @WithMockUser(authorities = { "Admin" })
    @DisplayName("Teste de integração - Salvar multa com todos os campos preenchidos")
    void testSalvarMultaCompletaIntegration() throws Exception {
        Usuario usuario = criarUsuario("Fernanda Souza", "fernanda@test.com");
        Emprestimo emprestimo = criarEmprestimo(usuario);

        Multas multa = new Multas();
        multa.setUsuario(usuario);
        multa.setEmprestimo(emprestimo);
        multa.setValor(new BigDecimal("50.00"));
        multa.setDataMulta(LocalDate.now().minusDays(5));
        multa.setMotivo("Atraso de 10 dias na devolução do livro");
        multa.setPaga(false);

        mockMvc.perform(post("/multas/salvarMulta")
                .with(csrf())
                .flashAttr("multa", multa))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/multas"));

        Optional<Multas> multaSalva = multasRepository.findAll()
                .stream()
                .filter(m -> "Atraso de 10 dias na devolução do livro".equals(m.getMotivo()))
                .findFirst();

        assertTrue(multaSalva.isPresent());
        assertEquals(new BigDecimal("50.00"), multaSalva.get().getValor());
        assertEquals(usuario.getId(), multaSalva.get().getUsuario().getId());
        assertFalse(multaSalva.get().getPaga());
    }

    @Test
    @WithMockUser(authorities = { "Professor" })
    @DisplayName("Teste de integração - Acessar formulário de edição")
    void testAcessarFormularioEdicaoIntegration() throws Exception {
        Usuario usuario = criarUsuario("Roberto Silva", "roberto@test.com");
        Emprestimo emprestimo = criarEmprestimo(usuario);

        Multas multa = new Multas();
        multa.setUsuario(usuario);
        multa.setEmprestimo(emprestimo);
        multa.setValor(new BigDecimal("12.00"));
        multa.setDataMulta(LocalDate.now());
        multa.setMotivo("Multa para edição");
        multa.setPaga(false);
        multa = multasRepository.save(multa);

        mockMvc.perform(get("/multas/editarMulta/" + multa.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("multas/editarMulta"));
    }

    @Test
    @WithMockUser(username = "aluno@test.com", authorities = { "Aluno" })
    @DisplayName("Teste de integração - Aluno não pode editar multa")
    void testAlunoNaoPodeEditarMultaIntegration() throws Exception {
        Usuario usuario = criarUsuario("Aluno Test", "aluno@test.com");
        Emprestimo emprestimo = criarEmprestimo(usuario);

        Multas multa = new Multas();
        multa.setUsuario(usuario);
        multa.setEmprestimo(emprestimo);
        multa.setValor(new BigDecimal("8.00"));
        multa.setDataMulta(LocalDate.now());
        multa.setMotivo("Multa do aluno");
        multa.setPaga(false);
        multa = multasRepository.save(multa);

        mockMvc.perform(get("/multas/editarMulta/" + multa.getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = { "Admin" })
    @DisplayName("Teste de integração - Atualizar status de multa para paga")
    void testAtualizarStatusMultaParaPagaIntegration() throws Exception {
        Usuario usuario = criarUsuario("Sandra Oliveira", "sandra@test.com");
        Emprestimo emprestimo = criarEmprestimo(usuario);

        Multas multa = new Multas();
        multa.setUsuario(usuario);
        multa.setEmprestimo(emprestimo);
        multa.setValor(new BigDecimal("30.00"));
        multa.setDataMulta(LocalDate.now());
        multa.setMotivo("Multa a ser paga");
        multa.setPaga(false);
        multa = multasRepository.save(multa);

        multa.setPaga(true);

        mockMvc.perform(post("/multas/salvarMulta")
                .with(csrf())
                .flashAttr("multa", multa))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/multas"));

        Optional<Multas> multaAtualizada = multasRepository.findById(multa.getId());
        assertTrue(multaAtualizada.isPresent());
        assertTrue(multaAtualizada.get().getPaga());
    }

    @Test
    @WithMockUser(authorities = { "Professor" })
    @DisplayName("Teste de integração - Salvar multa com valor diferente")
    void testSalvarMultaComValoresDiferentesIntegration() throws Exception {
        Usuario usuario = criarUsuario("Lucas Mendes", "lucas@test.com");
        Emprestimo emprestimo = criarEmprestimo(usuario);

        Multas multa = new Multas();
        multa.setUsuario(usuario);
        multa.setEmprestimo(emprestimo);
        multa.setValor(new BigDecimal("99.99"));
        multa.setDataMulta(LocalDate.now());
        multa.setMotivo("Multa com valor alto");
        multa.setPaga(false);

        mockMvc.perform(post("/multas/salvarMulta")
                .with(csrf())
                .flashAttr("multa", multa))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/multas"));

        Optional<Multas> multaSalva = multasRepository.findAll()
                .stream()
                .filter(m -> "Multa com valor alto".equals(m.getMotivo()))
                .findFirst();

        assertTrue(multaSalva.isPresent());
        assertEquals(new BigDecimal("99.99"), multaSalva.get().getValor());
    }

    @Test
    @WithMockUser(authorities = { "Admin" })
    @DisplayName("Teste de integração - Verificar relacionamento entre multa e empréstimo")
    void testVerificarRelacionamentoMultaEmprestimoIntegration() throws Exception {
        Usuario usuario = criarUsuario("Beatriz Rocha", "beatriz@test.com");
        Emprestimo emprestimo = criarEmprestimo(usuario);

        Multas multa = new Multas();
        multa.setUsuario(usuario);
        multa.setEmprestimo(emprestimo);
        multa.setValor(new BigDecimal("7.50"));
        multa.setDataMulta(LocalDate.now());
        multa.setMotivo("Verificação de relacionamento");
        multa.setPaga(false);
        multa = multasRepository.save(multa);

        Optional<Multas> multaSalva = multasRepository.findById(multa.getId());
        assertTrue(multaSalva.isPresent());
        assertEquals(emprestimo.getId(), multaSalva.get().getEmprestimo().getId());
        assertEquals(usuario.getId(), multaSalva.get().getUsuario().getId());
    }

    @Test
    @WithMockUser(authorities = { "Admin" })
    @DisplayName("Teste de integração - Acessar formulário de cadastro")
    void testAcessarFormularioCadastroIntegration() throws Exception {
        mockMvc.perform(get("/multas/cadastrarMulta"))
                .andExpect(status().isOk())
                .andExpect(view().name("multas/cadastrarMulta"));
    }
}
