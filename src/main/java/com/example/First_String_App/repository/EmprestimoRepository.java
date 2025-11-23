package com.example.First_String_App.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.First_String_App.model.Emprestimo;

public interface EmprestimoRepository extends JpaRepository<Emprestimo, Long> {} 
