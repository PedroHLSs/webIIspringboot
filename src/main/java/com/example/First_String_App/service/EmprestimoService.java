package com.example.First_String_App.service;

import java.util.List;

import com.example.First_String_App.model.Emprestimo;

public interface EmprestimoService {

    List<Emprestimo> listarEmprestimos();

    List<Emprestimo> listarEmprestimosPorUsuario(Integer usuarioId);

    Emprestimo buscarEmprestimoPorId(Long id);

    Emprestimo getEmprestimoById(Long id);

    Emprestimo salvarEmprestimo(Emprestimo emprestimo);

    Emprestimo atualizarEmprestimo(Long id, Emprestimo emprestimo);

    void deletarEmprestimo(Long id);

}
