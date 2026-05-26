package com.example.demo.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.annotations.Admin;
import com.example.demo.dto.PostoDTO;
import com.example.demo.service.PostoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/postos")
public class PostoController extends BaseController<PostoDTO>{

        
        protected PostoController(PostoService service){
        super(service);
        System.out.println("teste");
    }

    @Override
    @PostMapping
    @Admin
    public PostoDTO create(@RequestBody @Valid PostoDTO dto) {
        return super.create(dto);
    }

    @Override
    @PutMapping("/{id}")
    @Admin
    public PostoDTO update(@PathVariable Long id, @RequestBody @Valid PostoDTO dto) {
        return super.update(id, dto);
    }

    @Override
    @DeleteMapping("/{id}")
    @Admin
    public PostoDTO delete(@PathVariable Long id) {
        return super.delete(id);
    }
    

}
