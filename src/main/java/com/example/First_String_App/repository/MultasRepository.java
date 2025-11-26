package com.example.First_String_App.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.First_String_App.model.Multas;

@Repository
public interface MultasRepository extends JpaRepository<Multas, Long> {
    
}
