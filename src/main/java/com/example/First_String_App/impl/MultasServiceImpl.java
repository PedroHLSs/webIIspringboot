package com.example.First_String_App.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.First_String_App.model.Multas;
import com.example.First_String_App.repository.MultasRepository;
import com.example.First_String_App.service.MultaService;

@Service
public class MultasServiceImpl implements MultaService {

    @Autowired
    private MultasRepository multasRepository;

    @Override
    public List<Multas> listarMultas() {
        return multasRepository.findAll();
    }

    @Override
    public Multas buscarMultaPorId(Long id) {
        Optional<Multas> multa = multasRepository.findById(id);
        if (multa.isPresent()) {
            return multa.get();
        } else {
            throw new RuntimeException("Multa não encontrada para o id: " + id);
        }
    }

    @Override
    public Multas salvarMulta(Multas multa) {
        return this.multasRepository.save(multa);
    }

    @Override
    public Multas atualizarMulta(Long id, Multas multa){
        return this.multasRepository.save(multa);
    }

    @Override
    public Multas getMultaById(Long id) {
        Optional<Multas> optional = multasRepository.findById(id);
        if (optional.isPresent()) {
            return optional.get();
        } else {
            throw new RuntimeException("Multa não foi encontrada no id: " + id);
        }
    }
    @Override 
    public void deletarMulta(Long id){
        this.multasRepository.deleteById(id);
    }
}
