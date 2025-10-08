package com.example.First_String_App.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "usuarios")

public class Usuario {

    @Id
    @GeneratedValue
    @Column(name= "idUsuario")
    private Long id;
    
    @Column(name = "nomeUsuario")
    private String nome;

    @Column(name = "emailUsuario")
    private String email;

    @Column(name = "senhaUsuario")
    private String senha;

    
}
