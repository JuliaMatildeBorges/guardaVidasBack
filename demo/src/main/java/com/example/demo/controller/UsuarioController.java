package com.example.demo.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.annotations.Admin;
import com.example.demo.dto.UsuarioDTO;
import com.example.demo.service.UsuarioService;

@RestController
@RequestMapping("/usuarios")
@Admin
public class UsuarioController extends BaseController<UsuarioDTO> {

    public UsuarioController(UsuarioService service){
        super(service);
    }



}
