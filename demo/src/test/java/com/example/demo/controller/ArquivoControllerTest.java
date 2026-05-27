package com.example.demo.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.File;
import java.nio.file.Files;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.example.demo.config.JwtUtil;
import com.example.demo.entity.Arquivo;
import com.example.demo.enums.NivelAcesso;
import com.example.demo.service.ArquivoService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ArquivoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @MockBean
    private ArquivoService arquivoService;

    private String adminToken;

    private String userToken;

    private File arquivoTemporario;

    @BeforeEach
    void setup() throws Exception {

        adminToken = jwtUtil.generateToken(
                "admin@email.com",
                NivelAcesso.ADMIN.toString());

        userToken = jwtUtil.generateToken(
                "user@email.com",
                NivelAcesso.USUARIO.toString());

        arquivoTemporario = File.createTempFile("teste-arquivo", ".txt");

        Files.write(
                arquivoTemporario.toPath(),
                "conteudo teste".getBytes());
    }

    @AfterEach
    void cleanup() {

        if (arquivoTemporario != null && arquivoTemporario.exists()) {
            arquivoTemporario.delete();
        }
    }

    @Test
    @DisplayName("Deve visualizar arquivo com sucesso")
    void deveVisualizarArquivo() throws Exception {

        UUID id = UUID.randomUUID();

        Arquivo arquivo = new Arquivo();

        arquivo.setId(id);
        arquivo.setNome("teste.txt");
        arquivo.setTipo("text/plain");
        arquivo.setCaminho(arquivoTemporario.getAbsolutePath());

        when(arquivoService.buscar(id))
                .thenReturn(arquivo);

        mockMvc.perform(
                get("/arquivos/" + id)
        )
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "text/plain"))
                .andExpect(content().bytes("conteudo teste".getBytes()));
    }

    @Test
    @DisplayName("Deve permitir acesso público para visualizar arquivo")
    void devePermitirAcessoPublico() throws Exception {

        UUID id = UUID.randomUUID();

        Arquivo arquivo = new Arquivo();

        arquivo.setId(id);
        arquivo.setNome("publico.txt");
        arquivo.setTipo("text/plain");
        arquivo.setCaminho(arquivoTemporario.getAbsolutePath());

        when(arquivoService.buscar(id))
                .thenReturn(arquivo);

        mockMvc.perform(
                get("/arquivos/" + id)
        )
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Deve excluir arquivo com token admin")
    void deveExcluirArquivo() throws Exception {

        UUID id = UUID.randomUUID();

        doNothing().when(arquivoService)
                .excluir(id);

        mockMvc.perform(
                delete("/arquivos/" + id)
                        .header("Authorization", "Bearer " + adminToken)
        )
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Usuário comum não deve excluir arquivo")
    void usuarioComumNaoDeveExcluirArquivo() throws Exception {

        UUID id = UUID.randomUUID();

        mockMvc.perform(
                delete("/arquivos/" + id)
                        .header("Authorization", "Bearer " + userToken)
        )
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Não deve excluir arquivo sem token")
    void naoDeveExcluirSemToken() throws Exception {

        UUID id = UUID.randomUUID();

        mockMvc.perform(
                delete("/arquivos/" + id)
        )
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Deve retornar erro quando arquivo não existir")
    void deveRetornarErroQuandoArquivoNaoExistir() throws Exception {

        UUID id = UUID.randomUUID();

        when(arquivoService.buscar(id))
                .thenThrow(new RuntimeException("Arquivo não encontrado"));

        mockMvc.perform(
                get("/arquivos/" + id)
        )
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("Deve retornar erro ao tentar ler arquivo físico inexistente")
    void deveRetornarErroArquivoFisicoInexistente() throws Exception {

        UUID id = UUID.randomUUID();

        Arquivo arquivo = new Arquivo();

        arquivo.setId(id);
        arquivo.setNome("inexistente.txt");
        arquivo.setTipo("text/plain");
        arquivo.setCaminho("arquivo-inexistente.txt");

        when(arquivoService.buscar(id))
                .thenReturn(arquivo);

        mockMvc.perform(
                get("/arquivos/" + id)
        )
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("Deve retornar erro ao excluir arquivo inexistente")
    void deveRetornarErroAoExcluirArquivo() throws Exception {

        UUID id = UUID.randomUUID();

        doThrow(new RuntimeException("Erro ao excluir"))
                .when(arquivoService)
                .excluir(any(UUID.class));

        mockMvc.perform(
                delete("/arquivos/" + id)
                        .header("Authorization", "Bearer " + adminToken)
        )
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("Deve visualizar arquivo png corretamente")
    void deveVisualizarArquivoPng() throws Exception {

        UUID id = UUID.randomUUID();

        File png = File.createTempFile("imagem", ".png");

        Files.write(
                png.toPath(),
                "png fake".getBytes());

        Arquivo arquivo = new Arquivo();

        arquivo.setId(id);
        arquivo.setNome("imagem.png");
        arquivo.setTipo("image/png");
        arquivo.setCaminho(png.getAbsolutePath());

        when(arquivoService.buscar(id))
                .thenReturn(arquivo);

        mockMvc.perform(
                get("/arquivos/" + id)
        )
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "image/png"));

        png.delete();
    }

}