package com.example.First_String_App.config;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import com.example.First_String_App.service.AutorService;
import com.example.First_String_App.service.EmprestimoService;
import com.example.First_String_App.service.LivroService;
import com.example.First_String_App.service.MultaService;
import com.example.First_String_App.service.UsuarioService;

@TestConfiguration
public class TestConfig {
    @Bean
    public AutorService autorService() {
        return Mockito.mock(AutorService.class);
    }

    @Bean
    public EmprestimoService emprestimo() {
        return Mockito.mock(EmprestimoService.class);
    }

    @Bean
    public LivroService livroService() {
        return Mockito.mock(LivroService.class);
    }

    @Bean
    public MultaService multaService() {
        return Mockito.mock(MultaService.class);
    }

    @Bean
    public UsuarioService usuarioService() {
        return Mockito.mock(UsuarioService.class);
    }
}
