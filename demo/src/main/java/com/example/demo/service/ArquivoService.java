package com.example.demo.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.entity.Arquivo;
import com.example.demo.entity.Checkin;
import com.example.demo.entity.Checkout;
import com.example.demo.repository.ArquivoRepository;
import com.example.demo.repository.CheckinRepository;
import com.example.demo.repository.CheckoutRespository;

import jakarta.transaction.Transactional;

@Service
public class ArquivoService {

    @Value("${arquivamento.path}")
    private String path;

    @Autowired
    private ArquivoRepository arquivoRepository;

    @Autowired
    private CheckinRepository checkinRepository;

    @Autowired
    private CheckoutRespository checkoutRespository;

    public Arquivo upload(MultipartFile file) {
        Path root = Paths.get(path);

        try {
            if (!Files.exists(root)) {
                Files.createDirectories(root);
            }

            String nomeOriginal = file.getOriginalFilename() != null ? file.getOriginalFilename() : "foto";
            String dataHora = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
            String nome = dataHora + "-" + UUID.randomUUID() + "-" + nomeOriginal.replaceAll("[^a-zA-Z0-9._-]", "_");

            Path destino = root.resolve(nome);

            Files.copy(file.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);

            Arquivo arquivo = new Arquivo();

            arquivo.setCaminho(destino.toString());
            arquivo.setNome(nomeOriginal);
            arquivo.setTamanho(file.getSize());
            arquivo.setTipo(file.getContentType());


            // Garante que a linha de arquivo exista no banco antes de checkin/checkout gravar foto_id.
            return arquivoRepository.saveAndFlush(arquivo);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao salvar o arquivo", e);
        }

    }

    public Arquivo buscar(UUID id) {
        return arquivoRepository.findById(id).orElseThrow();
    }

    @Transactional
    public void excluir(UUID id) {
        Arquivo arquivo = buscar(id);
        try {
            desvincularCheckins(arquivo);
            desvincularCheckouts(arquivo);
            Files.deleteIfExists(Paths.get(arquivo.getCaminho()));
            arquivoRepository.delete(arquivo);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao excluir o arquivo", e);
        }
    }

    private void desvincularCheckins(Arquivo arquivo) {
        for (Checkin checkin : checkinRepository.findAll()) {
            boolean alterado = false;

            if (checkin.getFotos() != null && checkin.getFotos().removeIf(foto -> foto.getId().equals(arquivo.getId()))) {
                alterado = true;
            }

            if (checkin.getFoto() != null && checkin.getFoto().getId().equals(arquivo.getId())) {
                checkin.setFoto(checkin.getFotos() != null && !checkin.getFotos().isEmpty() ? checkin.getFotos().get(0) : null);
                alterado = true;
            }

            if (alterado) {
                checkinRepository.save(checkin);
            }
        }
    }

    private void desvincularCheckouts(Arquivo arquivo) {
        for (Checkout checkout : checkoutRespository.findAll()) {
            boolean alterado = false;

            if (checkout.getFotos() != null && checkout.getFotos().removeIf(foto -> foto.getId().equals(arquivo.getId()))) {
                alterado = true;
            }

            if (checkout.getFoto() != null && checkout.getFoto().getId().equals(arquivo.getId())) {
                checkout.setFoto(checkout.getFotos() != null && !checkout.getFotos().isEmpty() ? checkout.getFotos().get(0) : null);
                alterado = true;
            }

            if (alterado) {
                checkoutRespository.save(checkout);
            }
        }
    }

    /**
     * Exclui fisicamente todos os arquivos registrados do disco e limpa a tabela de arquivos.
     */
    @Transactional
    public void excluirTodosOsArquivos() {
        java.util.List<Arquivo> arquivos = arquivoRepository.findAll();
        for (Arquivo arquivo : arquivos) {
            try {
                java.nio.file.Files.deleteIfExists(java.nio.file.Paths.get(arquivo.getCaminho()));
            } catch (java.io.IOException e) {
                System.err.println("Erro ao deletar arquivo fisico: " + arquivo.getCaminho() + ". " + e.getMessage());
            }
        }
        arquivoRepository.deleteAll();
    }

}
