package com.example.First_String_App.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.First_String_App.model.LivroAutor;

@Repository
public interface LivroAutorRepository extends JpaRepository<LivroAutor, Long> {
}
