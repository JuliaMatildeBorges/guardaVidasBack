package com.example.demo.service;

import org.springframework.stereotype.Service;

import com.example.demo.dto.PessoaDTO;
import com.example.demo.entity.Pessoa;
import com.example.demo.repository.PessoaRepository;

@Service
public class PessoaService extends BaseService<Pessoa, PessoaDTO> {

    private PessoaRepository repository;

    protected PessoaService(PessoaRepository repository){
        super(repository);

        this.repository = repository;
    }

}
