package com.example.demo.controller;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.aspectj.lang.annotation.Before;
import org.h2.server.web.WebApp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.example.demo.config.JwtUtil;
import com.example.demo.entity.Usuario;
import com.example.demo.enums.NivelAcesso;
import com.example.demo.repository.UsuarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@ActiveProfiles("test") 
public class AuthControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    private ObjectMapper objectMapper;

    private String token;

    @Autowired
    private JwtUtil jwt;

    @Autowired
    private UsuarioRepository ur;

    @BeforeEach
    public void setup (){
         this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        this.objectMapper = new ObjectMapper();

        this.token = jwt.generateToken("teste@admin.com", NivelAcesso.ADMIN.toString());


    }

        @Test
    @DisplayName("Deve encontrar usuário pelo email")
    void deveEncontrarUsuario() {

        Usuario usuario = new Usuario();

        usuario.setEmail("admin@email.com");
        usuario.setSenha("123");

        ur.save(usuario);

        Optional<Usuario> usuarioOpt = ur.findByEmail("admin@email.com");

        assertTrue(usuarioOpt.isPresent());
    }


    

    




}
