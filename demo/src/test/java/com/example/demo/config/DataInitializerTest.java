package com.example.demo.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.test.context.ActiveProfiles;

import com.example.demo.entity.Usuario;
import com.example.demo.enums.NivelAcesso;
import com.example.demo.repository.UsuarioRepository;

@SpringBootTest
@ActiveProfiles("test")
public class DataInitializerTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private DataInitializer dataInitializer;

    @BeforeEach
    void setup() {

        usuarioRepository.deleteAll();
    }

    @Test
    @DisplayName("Deve criar usuários padrão")
    void deveCriarUsuariosPadrao() throws Exception {

        CommandLineRunner runner =
                dataInitializer.initDatabase(usuarioRepository);

        runner.run();

        Usuario admin =
                usuarioRepository
                        .findByEmail("admin@admin.com")
                        .orElseThrow();

        Usuario salvaVidas =
                usuarioRepository
                        .findByEmail("salvavidas@salvavidas.com")
                        .orElseThrow();

        assertEquals(
                NivelAcesso.ADMIN,
                admin.getNivelAcesso()
        );

        assertEquals(
                NivelAcesso.PADRAO,
                salvaVidas.getNivelAcesso()
        );
    }

    @Test
    @DisplayName("Deve criptografar senha")
    void deveCriptografarSenha() throws Exception {

        CommandLineRunner runner =
                dataInitializer.initDatabase(usuarioRepository);

        runner.run();

        Usuario admin =
                usuarioRepository
                        .findByEmail("admin@admin.com")
                        .orElseThrow();

        assertTrue(
                passwordEncoder.matches(
                        "123456789",
                        admin.getSenha()
                )
        );
    }

    @Test
    @DisplayName("Deve atualizar usuário existente")
    void deveAtualizarUsuarioExistente() throws Exception {

        Usuario usuario = new Usuario();

        usuario.setEmail("admin@admin.com");
        usuario.setCpf("cpf-antigo");

        usuarioRepository.save(usuario);

        CommandLineRunner runner =
                dataInitializer.initDatabase(usuarioRepository);

        runner.run();

        Usuario atualizado =
                usuarioRepository
                        .findByEmail("admin@admin.com")
                        .orElseThrow();

        assertEquals(
                "00000000000",
                atualizado.getCpf()
        );

        assertEquals(
                NivelAcesso.ADMIN,
                atualizado.getNivelAcesso()
        );
    }

}