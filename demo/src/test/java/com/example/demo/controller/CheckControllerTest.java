package com.example.demo.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import org.springframework.mock.web.MockMultipartFile;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.example.demo.config.JwtUtil;
import com.example.demo.dto.CheckinDTO;
import com.example.demo.dto.CheckinResponseDTO;
import com.example.demo.dto.CheckoutDTO;
import com.example.demo.dto.CheckoutResponseDTO;
import com.example.demo.dto.PostoCheckResumoDTO;
import com.example.demo.enums.NivelAcesso;
import com.example.demo.service.CheckResumoService;
import com.example.demo.service.CheckService;
import com.example.demo.service.CheckoutService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class CheckControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @MockBean
    private CheckService checkService;

    @MockBean
    private CheckoutService checkoutService;

    @MockBean
    private CheckResumoService checkResumoService;

    private String adminToken;

    private String userToken;

    @BeforeEach
    void setup() {

        adminToken = jwtUtil.generateToken(
                "admin@email.com",
                NivelAcesso.ADMIN.toString());

        userToken = jwtUtil.generateToken(
                "user@email.com",
                NivelAcesso.USUARIO.toString());
    }

    @Test
    @DisplayName("Deve realizar checkin com sucesso")
    void deveRealizarCheckin() throws Exception {

        CheckinResponseDTO response = new CheckinResponseDTO();

        when(checkService.checkin(any(CheckinDTO.class)))
                .thenReturn(response);

        MockMultipartFile foto = new MockMultipartFile(
                "foto",
                "foto.png",
                MediaType.IMAGE_PNG_VALUE,
                "imagem".getBytes());

        mockMvc.perform(
                multipart("/check/in")
                        .file(foto)
                        .param("observacao", "teste")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
        )
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Deve realizar checkout com sucesso")
    void deveRealizarCheckout() throws Exception {

        CheckoutResponseDTO response = new CheckoutResponseDTO();

        when(checkoutService.checkout(any(CheckoutDTO.class)))
                .thenReturn(response);

        MockMultipartFile foto = new MockMultipartFile(
                "foto",
                "foto.png",
                MediaType.IMAGE_PNG_VALUE,
                "imagem".getBytes());

        mockMvc.perform(
                multipart("/check/out")
                        .file(foto)
                        .param("observacao", "checkout teste")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
        )
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Admin deve acessar status de hoje")
    void adminDeveAcessarStatusHoje() throws Exception {

        List<PostoCheckResumoDTO> lista = new ArrayList<>();

        PostoCheckResumoDTO dto = new PostoCheckResumoDTO();

        lista.add(dto);

        when(checkResumoService.statusHoje())
                .thenReturn(lista);

        mockMvc.perform(
                get("/check/status-hoje")
                        .header("Authorization", "Bearer " + adminToken)
        )
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Usuário comum não deve acessar status de hoje")
    void usuarioNaoDeveAcessarStatusHoje() throws Exception {

        mockMvc.perform(
                get("/check/status-hoje")
                        .header("Authorization", "Bearer " + userToken)
        )
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Deve retornar lista vazia no status hoje")
    void deveRetornarListaVaziaStatusHoje() throws Exception {

        when(checkResumoService.statusHoje())
                .thenReturn(new ArrayList<>());

        mockMvc.perform(
                get("/check/status-hoje")
                        .header("Authorization", "Bearer " + adminToken)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("Não deve permitir acesso sem token no status hoje")
    void naoDevePermitirAcessoSemToken() throws Exception {

        mockMvc.perform(
                get("/check/status-hoje")
        )
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Não deve permitir checkin sem autenticação")
    void naoDevePermitirCheckinSemToken() throws Exception {

        MockMultipartFile foto = new MockMultipartFile(
                "foto",
                "foto.png",
                MediaType.IMAGE_PNG_VALUE,
                "imagem".getBytes());

        mockMvc.perform(
                multipart("/check/in")
                        .file(foto)
                        .param("observacao", "teste")
        )
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Não deve permitir checkout sem autenticação")
    void naoDevePermitirCheckoutSemToken() throws Exception {

        MockMultipartFile foto = new MockMultipartFile(
                "foto",
                "foto.png",
                MediaType.IMAGE_PNG_VALUE,
                "imagem".getBytes());

        mockMvc.perform(
                multipart("/check/out")
                        .file(foto)
                        .param("observacao", "teste")
        )
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Deve aceitar multipart vazio no checkin")
    void deveAceitarMultipartVazioNoCheckin() throws Exception {

        CheckinResponseDTO response = new CheckinResponseDTO();

        when(checkService.checkin(any(CheckinDTO.class)))
                .thenReturn(response);

        mockMvc.perform(
                multipart("/check/in")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
        )
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Deve aceitar multipart vazio no checkout")
    void deveAceitarMultipartVazioNoCheckout() throws Exception {

        CheckoutResponseDTO response = new CheckoutResponseDTO();

        when(checkoutService.checkout(any(CheckoutDTO.class)))
                .thenReturn(response);

        mockMvc.perform(
                multipart("/check/out")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
        )
                .andExpect(status().isOk());
    }

}