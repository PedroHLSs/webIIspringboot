package com.example.First_String_App.model;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "livros")

public class Livro {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Titulo deve ser obrigatorio")
    @Column(name = "name", nullable = false)
    private String titulo;
    @Column
    private String autor;
    @Column
    private LocalDate ano;
    @Column
    private String editora;
    @Column
    private Boolean disponivel;
    @Column
    private String descricao;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    public LocalDate getAno() {
        return ano;
    }

    public void setAno(LocalDate ano) {
        this.ano = ano;
    }

    public String getEditora() {
        return editora;
    }

    public void setEditora(String editora) {
        this.editora = editora;
    }

    public Boolean getDisponivel() {
        return disponivel;
    }

    public void setDisponivel(Boolean disponivel) {
        this.disponivel = disponivel;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Livro() {
    }

    public Livro(Long id, String titulo, String autor, LocalDate ano, String editora, Boolean disponivel,
            String descricao) {
        this.id = id;
        this.titulo = titulo;
        this.autor = autor;
        this.ano = ano;
        this.editora = editora;
        this.disponivel = disponivel;
        this.descricao = descricao;
    }

}
