package com.example.First_String_App.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.First_String_App.model.Emprestimo;
import com.example.First_String_App.repository.EmprestimoRepository;
import com.example.First_String_App.service.EmprestimoService;

import io.micrometer.observation.annotation.Observed;

@Service
public class EmprestimoServiceImpl implements EmprestimoService {

    @Autowired
    private EmprestimoRepository emprestimoRepository;

    @Override
    public List<Emprestimo> listarEmprestimos() {
        return emprestimoRepository.findAll();
    }

    @Override
    public List<Emprestimo> listarEmprestimosPorUsuario(Integer usuarioId) {
        return emprestimoRepository.findByUsuario_Id(usuarioId);
    }

    @Override
    public Emprestimo buscarEmprestimoPorId(Long id) {
        Optional<Emprestimo> emprestimo = emprestimoRepository.findById(id);
        if (emprestimo.isPresent()) {
            return emprestimo.get();
        } else {
            throw new RuntimeException("Empréstimo não encontrado para o id: " + id);
        }
    }

    @Override
    public Emprestimo getEmprestimoById(Long id) {
        Optional<Emprestimo> emprestimo = emprestimoRepository.findById(id);
        if (emprestimo.isPresent()) {
            return emprestimo.get();
        } else {
            throw new RuntimeException("Empréstimo não encontrado para o id: " + id);
        }
    }

    @Override
    public Emprestimo salvarEmprestimo(Emprestimo emprestimo) {
        return this.emprestimoRepository.save(emprestimo);
    }

    @Override
    public Emprestimo atualizarEmprestimo(Long id, Emprestimo emprestimo) {
        return this.emprestimoRepository.save(emprestimo);
    }

    @Override
    public void deletarEmprestimo(Long id) {
        this.emprestimoRepository.deleteById(id);
    }

}
