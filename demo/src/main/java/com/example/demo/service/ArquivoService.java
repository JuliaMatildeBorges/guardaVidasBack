package com.example.demo.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.entity.Arquivo;

@Service
public class ArquivoService {

    @Value("${arquivamento.path}")
    private String path;

    public Arquivo upload(MultipartFile file) {
        Path root = Paths.get(path);

        try {
            if (!Files.exists(root)) {
                Files.createDirectories(root);
            }

            String nome = UUID.randomUUID().toString();

            Path destino = root.resolve(nome);

            Files.copy(file.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);

            Arquivo arquivo = new Arquivo();
            arquivo.setCaminhho(destino.toString());
            arquivo.setNome(file.getOriginalFilename());
            arquivo.setTamanho(file.getSize());
            arquivo.setTipo(file.getContentType());
              


        } catch (IOException e) {

        }

    }

}
