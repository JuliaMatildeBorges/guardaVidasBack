package com.example.demo;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.http.MediaType;

import org.springframework.mock.web.MockMultipartFile;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import org.springframework.web.context.WebApplicationContext;

import com.example.demo.config.JwtUtil;
import com.example.demo.entity.Posto;
import com.example.demo.entity.Usuario;
import com.example.demo.enums.NivelAcesso;
import com.example.demo.repository.PostoRepository;
import com.example.demo.repository.UsuarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@ActiveProfiles("test")
public class CheckinTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    private ObjectMapper objectMapper;

    private Posto posto;

    @Autowired
    private JwtUtil jwt;

    private String adminToken;

    private String userToken;

    @Autowired
    private PostoRepository pr;

    @Autowired
    private UsuarioRepository ur;

    @BeforeEach
    public void setup() {

        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .build();

        this.objectMapper = new ObjectMapper();

        Usuario usuario = new Usuario();

        usuario.setEmail("tantofazcomotantofez@admin.com");
        usuario.setSenha("123");

        ur.save(usuario);

        this.adminToken = jwt.generateToken(
                "tantofazcomotantofez@admin.com",
                NivelAcesso.ADMIN.toString());

        this.userToken = jwt.generateToken(
                "usuario@email.com",
                NivelAcesso.USUARIO.toString());

        this.posto = new Posto();

        posto.setNome("Posto Teste");
        posto.setDescricao("teste");

        pr.save(posto);
    }

    // =========================
    // CHECKIN TESTS
    // =========================

    @Test
    @DisplayName("Deve realizar checkin com sucesso")
    void checkin() throws Exception {

        MockMultipartFile fotoMock = new MockMultipartFile(
                "foto",
                "bombeiro.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "conteudo".getBytes());

        mockMvc.perform(
                multipart("/check/in")
                        .file(fotoMock)
                        .param("postoId", posto.getId().toString())
                        .header("Authorization", "Bearer " + adminToken)
        )
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Deve realizar checkin com múltiplas fotos")
    void deveRealizarCheckinComMultiplasFotos() throws Exception {

        MockMultipartFile foto1 = new MockMultipartFile(
                "fotos",
                "foto1.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "foto1".getBytes());

        MockMultipartFile foto2 = new MockMultipartFile(
                "fotos",
                "foto2.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "foto2".getBytes());

        mockMvc.perform(
                multipart("/check/in")
                        .file(foto1)
                        .file(foto2)
                        .param("postoId", posto.getId().toString())
                        .header("Authorization", "Bearer " + adminToken)
        )
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Não deve permitir checkin sem fotos")
    void naoDevePermitirCheckinSemFotos() throws Exception {

        mockMvc.perform(
                multipart("/check/in")
                        .param("postoId", posto.getId().toString())
                        .header("Authorization", "Bearer " + adminToken)
        )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Envie de 1 até 3 fotos."));
    }

    @Test
    @DisplayName("Não deve permitir checkin com mais de 3 fotos")
    void naoDevePermitirCheckinComMaisDe3Fotos() throws Exception {

        MockMultipartFile foto1 = new MockMultipartFile(
                "fotos",
                "1.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "1".getBytes());

        MockMultipartFile foto2 = new MockMultipartFile(
                "fotos",
                "2.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "2".getBytes());

        MockMultipartFile foto3 = new MockMultipartFile(
                "fotos",
                "3.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "3".getBytes());

        MockMultipartFile foto4 = new MockMultipartFile(
                "fotos",
                "4.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "4".getBytes());

        mockMvc.perform(
                multipart("/check/in")
                        .file(foto1)
                        .file(foto2)
                        .file(foto3)
                        .file(foto4)
                        .param("postoId", posto.getId().toString())
                        .header("Authorization", "Bearer " + adminToken)
        )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Envie de 1 até 3 fotos."));
    }

    @Test
    @DisplayName("Não deve permitir checkin sem autenticação")
    void naoDevePermitirCheckinSemAutenticacao() throws Exception {

        MockMultipartFile fotoMock = new MockMultipartFile(
                "foto",
                "bombeiro.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "conteudo".getBytes());

        mockMvc.perform(
                multipart("/check/in")
                        .file(fotoMock)
                        .param("postoId", posto.getId().toString())
        )
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Não deve permitir checkin com posto inexistente")
    void naoDevePermitirCheckinComPostoInexistente() throws Exception {

        MockMultipartFile fotoMock = new MockMultipartFile(
                "foto",
                "bombeiro.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "conteudo".getBytes());

        mockMvc.perform(
                multipart("/check/in")
                        .file(fotoMock)
                        .param("postoId", UUID.randomUUID().toString())
                        .header("Authorization", "Bearer " + adminToken)
        )
                .andExpect(status().isInternalServerError());
    }

    // =========================
    // CHECKOUT TESTS
    // =========================

    @Test
    @DisplayName("Deve realizar checkout com sucesso")
    void deveRealizarCheckout() throws Exception {

        MockMultipartFile fotoMock = new MockMultipartFile(
                "foto",
                "checkout.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "conteudo".getBytes());

        mockMvc.perform(
                multipart("/check/out")
                        .file(fotoMock)
                        .param("postoId", posto.getId().toString())
                        .param("prevencoesManha", "10")
                        .param("prevencoesTarde", "20")
                        .param("lesoesAguaVivaManha", "1")
                        .param("lesoesAguaVivaTarde", "2")
                        .header("Authorization", "Bearer " + adminToken)
        )
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Deve realizar checkout com múltiplas fotos")
    void deveRealizarCheckoutComMultiplasFotos() throws Exception {

        MockMultipartFile foto1 = new MockMultipartFile(
                "fotos",
                "foto1.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "foto1".getBytes());

        MockMultipartFile foto2 = new MockMultipartFile(
                "fotos",
                "foto2.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "foto2".getBytes());

        mockMvc.perform(
                multipart("/check/out")
                        .file(foto1)
                        .file(foto2)
                        .param("postoId", posto.getId().toString())
                        .param("prevencoesManha", "5")
                        .param("prevencoesTarde", "10")
                        .param("lesoesAguaVivaManha", "0")
                        .param("lesoesAguaVivaTarde", "1")
                        .header("Authorization", "Bearer " + adminToken)
        )
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Não deve permitir checkout sem fotos")
    void naoDevePermitirCheckoutSemFotos() throws Exception {

        mockMvc.perform(
                multipart("/check/out")
                        .param("postoId", posto.getId().toString())
                        .param("prevencoesManha", "1")
                        .param("prevencoesTarde", "1")
                        .header("Authorization", "Bearer " + adminToken)
        )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Envie de 1 até 3 fotos."));
    }

    @Test
    @DisplayName("Não deve permitir checkout com mais de 3 fotos")
    void naoDevePermitirCheckoutComMaisDe3Fotos() throws Exception {

        MockMultipartFile foto1 = new MockMultipartFile(
                "fotos",
                "1.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "1".getBytes());

        MockMultipartFile foto2 = new MockMultipartFile(
                "fotos",
                "2.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "2".getBytes());

        MockMultipartFile foto3 = new MockMultipartFile(
                "fotos",
                "3.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "3".getBytes());

        MockMultipartFile foto4 = new MockMultipartFile(
                "fotos",
                "4.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "4".getBytes());

        mockMvc.perform(
                multipart("/check/out")
                        .file(foto1)
                        .file(foto2)
                        .file(foto3)
                        .file(foto4)
                        .param("postoId", posto.getId().toString())
                        .param("prevencoesManha", "1")
                        .param("prevencoesTarde", "1")
                        .header("Authorization", "Bearer " + adminToken)
        )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Envie de 1 até 3 fotos."));
    }

    @Test
    @DisplayName("Não deve permitir checkout sem autenticação")
    void naoDevePermitirCheckoutSemAutenticacao() throws Exception {

        MockMultipartFile fotoMock = new MockMultipartFile(
                "foto",
                "checkout.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "conteudo".getBytes());

        mockMvc.perform(
                multipart("/check/out")
                        .file(fotoMock)
                        .param("postoId", posto.getId().toString())
        )
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Não deve permitir checkout com posto inexistente")
    void naoDevePermitirCheckoutComPostoInexistente() throws Exception {

        MockMultipartFile fotoMock = new MockMultipartFile(
                "foto",
                "checkout.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "conteudo".getBytes());

        mockMvc.perform(
                multipart("/check/out")
                        .file(fotoMock)
                        .param("postoId", UUID.randomUUID().toString())
                        .param("prevencoesManha", "1")
                        .param("prevencoesTarde", "1")
                        .header("Authorization", "Bearer " + adminToken)
        )
                .andExpect(status().isInternalServerError());
    }

}