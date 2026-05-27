package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.demo.entity.Arquivo;
import com.example.demo.entity.Checkin;
import com.example.demo.entity.Checkout;
import com.example.demo.repository.ArquivoRepository;
import com.example.demo.repository.CheckinRepository;
import com.example.demo.repository.CheckoutRespository;

@SpringBootTest
@ActiveProfiles("test")
public class ArquivoServiceTest {

    @Autowired
    private ArquivoService arquivoService;

    @Autowired
    private ArquivoRepository arquivoRepository;

    @Autowired
    private CheckinRepository checkinRepository;

    @Autowired
    private CheckoutRespository checkoutRepository;

    @Value("${arquivamento.path}")
    private String uploadPath;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(arquivoService, "path", uploadPath);

        checkoutRepository.deleteAll();
        checkinRepository.deleteAll();
        arquivoRepository.deleteAll();
    }

    @AfterEach
    void cleanup() throws IOException {
        File pasta = new File(uploadPath);

        if (pasta.exists()) {
            for (File file : pasta.listFiles()) {
                file.delete();
            }
        }
    }

    @Test
    @DisplayName("Deve realizar upload de arquivo")
    void deveFazerUpload() {

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "foto.png",
                "image/png",
                "conteudo".getBytes());

        Arquivo arquivo = arquivoService.upload(file);

        assertNotNull(arquivo);
        assertNotNull(arquivo.getId());
        assertEquals("foto.png", arquivo.getNome());
        assertEquals("image/png", arquivo.getTipo());
        assertEquals(file.getSize(), arquivo.getTamanho());

        File arquivoFisico = new File(arquivo.getCaminho());

        assertEquals(true, arquivoFisico.exists());
    }

    @Test
    @DisplayName("Deve buscar arquivo por id")
    void deveBuscarArquivoPorId() {

        Arquivo arquivo = new Arquivo();

        arquivo.setNome("teste");
        arquivo.setTipo("image/png");
        arquivo.setTamanho(100L);
        arquivo.setCaminho("caminho/teste.png");

        arquivo = arquivoRepository.save(arquivo);

        Arquivo resultado = arquivoService.buscar(arquivo.getId());

        assertNotNull(resultado);
        assertEquals(arquivo.getId(), resultado.getId());
    }

    @Test
    @DisplayName("Deve lançar exceção ao buscar arquivo inexistente")
    void deveLancarExcecaoAoBuscarArquivoInexistente() {

        UUID id = UUID.randomUUID();

        assertThrows(Exception.class, () -> {
            arquivoService.buscar(id);
        });
    }

    @Test
    @DisplayName("Deve excluir arquivo")
    void deveExcluirArquivo() throws IOException {

        File pasta = new File(uploadPath);

        if (!pasta.exists()) {
            pasta.mkdirs();
        }

        File arquivoFisico = new File(uploadPath + "/teste.txt");

        Files.write(arquivoFisico.toPath(), "teste".getBytes());

        Arquivo arquivo = new Arquivo();

        arquivo.setNome("teste.txt");
        arquivo.setTipo("text/plain");
        arquivo.setTamanho(10L);
        arquivo.setCaminho(arquivoFisico.getAbsolutePath());

        arquivo = arquivoRepository.save(arquivo);

        UUID id = arquivo.getId();

        arquivoService.excluir(id);

        Optional<Arquivo> resultado = arquivoRepository.findById(id);

        assertEquals(false, resultado.isPresent());
        assertEquals(false, arquivoFisico.exists());
    }

    @Test
    @DisplayName("Deve desvincular arquivo do checkin")
    void deveDesvincularArquivoDoCheckin() throws IOException {

        File pasta = new File(uploadPath);

        if (!pasta.exists()) {
            pasta.mkdirs();
        }

        File arquivoFisico = new File(uploadPath + "/checkin.txt");

        Files.write(arquivoFisico.toPath(), "teste".getBytes());

        Arquivo arquivo = new Arquivo();

        arquivo.setNome("checkin.txt");
        arquivo.setTipo("text/plain");
        arquivo.setTamanho(20L);
        arquivo.setCaminho(arquivoFisico.getAbsolutePath());

        arquivo = arquivoRepository.save(arquivo);

        Checkin checkin = new Checkin();

        List<Arquivo> fotos = new ArrayList<>();

        fotos.add(arquivo);

        checkin.setFotos(fotos);
        checkin.setFoto(arquivo);

        checkin = checkinRepository.save(checkin);

        arquivoService.excluir(arquivo.getId());

        Checkin atualizado = checkinRepository.findById(checkin.getId()).orElseThrow();

        assertNotNull(atualizado);
        assertEquals(0, atualizado.getFotos().size());
        assertNull(atualizado.getFoto());
    }

    @Test
    @DisplayName("Deve desvincular arquivo do checkout")
    void deveDesvincularArquivoDoCheckout() throws IOException {

        File pasta = new File(uploadPath);

        if (!pasta.exists()) {
            pasta.mkdirs();
        }

        File arquivoFisico = new File(uploadPath + "/checkout.txt");

        Files.write(arquivoFisico.toPath(), "teste".getBytes());

        Arquivo arquivo = new Arquivo();

        arquivo.setNome("checkout.txt");
        arquivo.setTipo("text/plain");
        arquivo.setTamanho(20L);
        arquivo.setCaminho(arquivoFisico.getAbsolutePath());

        arquivo = arquivoRepository.save(arquivo);

        Checkout checkout = new Checkout();

        List<Arquivo> fotos = new ArrayList<>();

        fotos.add(arquivo);

        checkout.setFotos(fotos);
        checkout.setFoto(arquivo);

        checkout = checkoutRepository.save(checkout);

        arquivoService.excluir(arquivo.getId());

        Checkout atualizado = checkoutRepository.findById(checkout.getId()).orElseThrow();

        assertNotNull(atualizado);
        assertEquals(0, atualizado.getFotos().size());
        assertNull(atualizado.getFoto());
    }

    @Test
    @DisplayName("Deve manter primeira foto restante no checkin")
    void deveManterPrimeiraFotoRestanteNoCheckin() throws IOException {

        Arquivo foto1 = new Arquivo();
        foto1.setNome("foto1");
        foto1.setTipo("png");
        foto1.setTamanho(10L);
        foto1.setCaminho(uploadPath + "/foto1.png");

        foto1 = arquivoRepository.save(foto1);

        Arquivo foto2 = new Arquivo();
        foto2.setNome("foto2");
        foto2.setTipo("png");
        foto2.setTamanho(10L);
        foto2.setCaminho(uploadPath + "/foto2.png");

        foto2 = arquivoRepository.save(foto2);

        Checkin checkin = new Checkin();

        List<Arquivo> fotos = new ArrayList<>();
        fotos.add(foto1);
        fotos.add(foto2);

        checkin.setFotos(fotos);
        checkin.setFoto(foto1);

        checkin = checkinRepository.save(checkin);

        arquivoService.excluir(foto1.getId());

        Checkin atualizado = checkinRepository.findById(checkin.getId()).orElseThrow();

        assertEquals(1, atualizado.getFotos().size());
        assertEquals(foto2.getId(), atualizado.getFoto().getId());
    }

    @Test
    @DisplayName("Deve manter primeira foto restante no checkout")
    void deveManterPrimeiraFotoRestanteNoCheckout() throws IOException {

        Arquivo foto1 = new Arquivo();
        foto1.setNome("foto1");
        foto1.setTipo("png");
        foto1.setTamanho(10L);
        foto1.setCaminho(uploadPath + "/foto1.png");

        foto1 = arquivoRepository.save(foto1);

        Arquivo foto2 = new Arquivo();
        foto2.setNome("foto2");
        foto2.setTipo("png");
        foto2.setTamanho(10L);
        foto2.setCaminho(uploadPath + "/foto2.png");

        foto2 = arquivoRepository.save(foto2);

        Checkout checkout = new Checkout();

        List<Arquivo> fotos = new ArrayList<>();
        fotos.add(foto1);
        fotos.add(foto2);

        checkout.setFotos(fotos);
        checkout.setFoto(foto1);

        checkout = checkoutRepository.save(checkout);

        arquivoService.excluir(foto1.getId());

        Checkout atualizado = checkoutRepository.findById(checkout.getId()).orElseThrow();

        assertEquals(1, atualizado.getFotos().size());
        assertEquals(foto2.getId(), atualizado.getFoto().getId());
    }

    @Test
    @DisplayName("Não deve falhar ao excluir arquivo inexistente fisicamente")
    void naoDeveFalharAoExcluirArquivoFisicoInexistente() {

        Arquivo arquivo = new Arquivo();

        arquivo.setNome("arquivo.txt");
        arquivo.setTipo("text/plain");
        arquivo.setTamanho(10L);
        arquivo.setCaminho(uploadPath + "/nao-existe.txt");

        arquivo = arquivoRepository.save(arquivo);

        assertDoesNotThrow(() -> {
            arquivoService.excluir(arquivo.getId());
        });
    }

}