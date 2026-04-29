package com.example.demo.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.PessoaDTO;
import com.example.demo.service.PessoaService;

@RestController
@RequestMapping("/pessoas")
public class PessoaController extends BaseController<PessoaDTO> {

    private PessoaService service;

    protected PessoaController(PessoaService service){
        super(service);
        this.service = service;
    }

}
