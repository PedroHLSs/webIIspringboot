package com.example.First_String_App.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@Configuration
public class SecurityConfig {

    @Autowired
    private UserDetailsService uds;

    @Autowired
    private BCryptPasswordEncoder encoder;

     @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
          .authorizeHttpRequests(auth -> auth
              .requestMatchers("/", "/index.html", "/registro", "/criarUsuario", "/login", "/css/**", "/js/**", "/imagens/**").permitAll()
              
              // Admin - Acesso total
              .requestMatchers("/usuarios/**").hasAuthority("Admin")
              
              // Alunos - Acesso limitado
              .requestMatchers("/livros").hasAnyAuthority("Aluno", "Professor", "Admin")
              .requestMatchers("/livros/detalhesLivro/**").hasAnyAuthority("Aluno", "Professor", "Admin")
              .requestMatchers("/livros/**").hasAnyAuthority("Professor", "Admin")
              .requestMatchers("/emprestimo", "/emprestimo/cadastrarEmprestimo", "/emprestimo/salvarEmprestimo").hasAnyAuthority("Aluno", "Professor", "Admin")
              .requestMatchers("/emprestimo/**").hasAnyAuthority("Professor", "Admin")
              .requestMatchers("/multas").hasAnyAuthority("Aluno", "Professor", "Admin")
              .requestMatchers("/multas/**").hasAnyAuthority("Professor", "Admin")
              
              // Professores e Admin
              .requestMatchers("/autores/**").hasAnyAuthority("Professor", "Admin")
              
              .anyRequest().authenticated()
          )
          .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
            .defaultSuccessUrl("/livros", true)
            .failureUrl("/login?error")
                .permitAll()
          )
          .logout(logout -> logout.permitAll())
          .exceptionHandling(exception -> exception
              .accessDeniedPage("/acessoNegado")
          );
        return http.build();
    }
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(uds);
        authenticationProvider.setPasswordEncoder(encoder);
        return authenticationProvider;
    }
}