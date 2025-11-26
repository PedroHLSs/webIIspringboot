package com.example.First_String_App.service;

import java.util.List;

import com.example.First_String_App.model.Multas;

public interface MultaService {

    List<Multas> listarMultas();

    Multas buscarMultaPorId(Long id);

    Multas getMultaById(Long id);

    Multas salvarMulta(Multas multa);

    Multas atualizarMulta(Long id, Multas multa);

    void deletarMulta(Long id);

}
