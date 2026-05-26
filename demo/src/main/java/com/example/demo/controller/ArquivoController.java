package com.example.demo.controller;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.annotations.Admin;
import com.example.demo.annotations.Public;
import com.example.demo.entity.Arquivo;
import com.example.demo.service.ArquivoService;

@RestController
@RequestMapping("/arquivos")
public class ArquivoController {

    @Autowired
    private ArquivoService arquivoService;

    @GetMapping("/{id}")
    @Public
    public ResponseEntity<ByteArrayResource> visualizar(@PathVariable UUID id) throws Exception {
        Arquivo arquivo = arquivoService.buscar(id);
        byte[] bytes = Files.readAllBytes(Paths.get(arquivo.getCaminho()));

        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(arquivo.getTipo()))
            .body(new ByteArrayResource(bytes));
    }

    @DeleteMapping("/{id}")
    @Admin
    public ResponseEntity<?> excluir(@PathVariable UUID id) {
        arquivoService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
