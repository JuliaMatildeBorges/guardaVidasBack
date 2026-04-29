package com.example.demo.repository;

import org.springframework.stereotype.Repository;

import com.example.demo.entity.Pessoa;

@Repository
public interface PessoaRepository extends BaseRepository<Pessoa, Long> {

}
