package com.example.demo.service;

import org.springframework.stereotype.Service;
import com.example.demo.dto.UsuarioDTO;
import com.example.demo.entity.Usuario;
import com.example.demo.repository.UsuarioRepository;

@Service
public class UsuarioService extends BaseService<Usuario, UsuarioDTO> {

    private UsuarioRepository repository;

    public UsuarioService(UsuarioRepository repository){
        super(repository);
        this.repository = repository;
    }

}
