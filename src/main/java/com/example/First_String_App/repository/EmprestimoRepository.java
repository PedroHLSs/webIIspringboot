package com.example.First_String_App.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.First_String_App.model.Emprestimo;

public interface EmprestimoRepository extends JpaRepository<Emprestimo, Long> {
    List<Emprestimo> findByUsuario_Id(Integer usuarioId);
} 
