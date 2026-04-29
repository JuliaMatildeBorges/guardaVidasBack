package com.example.demo.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.demo.enums.NivelAcesso;

public class JwtUtilTest {

    private JwtUtil jwt;

    @BeforeEach
    void configurar() {
        jwt = new JwtUtil();

        ReflectionTestUtils.setField(jwt, "secret", "chave-secreta-de-teste-com-pelo-menos-32-caracteres-aqui!");

        jwt.init();
    }

    @Test
    @DisplayName("Verificar se o token retorna o email correto")
    void deveExtrairEmaildoToken() {
        // ARRANGE
        String email = "email@teste.com";
        String token = jwt.generateToken(email, NivelAcesso.ADMIN.toString());

        // ACT
        String emailExtraido = jwt.extractUsername(token);

        // ASSERT
        assertEquals(email, emailExtraido, "O email extraido deve ser identico ao email usado na gerador");
    }

    @Test
    @DisplayName("O teste deve retornar se o nivel de acesso está correto")
    void deveExtrairNivelAcessoTken() {

        String token = jwt.generateToken("teste@teste.com", NivelAcesso.ADMIN.toString());

        String roleExtraido = jwt.extractRole(token);

        assertEquals(NivelAcesso.ADMIN.toString(), roleExtraido, "O papel extraido deve ser identico ao email usado na gerador");
    }


    @Test
    @DisplayName("Valida token adulterado")
    void validarTokenAdulterado(){
        String token = jwt.generateToken("usuario@teste.com", NivelAcesso.PADRAO.toString());

        String tokenAdulterado = token + "xxx";

        boolean valido = jwt.validateToken(tokenAdulterado);

        assertFalse(valido);

    }

    @Test
    @DisplayName("Token vazio")
    void tokenVazio(){

        String token = "";

        boolean valido = jwt.validateToken(token);

        assertFalse(valido);

    }



    
}
