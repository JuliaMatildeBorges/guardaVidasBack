package com.example.demo.controller;

import com.example.demo.config.JwtUtil;
import com.example.demo.dto.AuthDTO;
import com.example.demo.entity.Usuario;
import com.example.demo.enums.NivelAcesso;
import com.example.demo.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthControllerUnitTest {

    private AuthController controller;
    private UsuarioRepository usuarioRepository;
    private JwtUtil jwtUtil;
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        controller = new AuthController();
        usuarioRepository = mock(UsuarioRepository.class);
        jwtUtil = mock(JwtUtil.class);
        passwordEncoder = new BCryptPasswordEncoder();

        ReflectionTestUtils.setField(controller, "usuarioRepository", usuarioRepository);
        ReflectionTestUtils.setField(controller, "jwtUtil", jwtUtil);
        ReflectionTestUtils.setField(controller, "passwordEncoder", passwordEncoder);
    }

    @Test
    void loginValidoRetornaTokenETipo() {
        Usuario usuario = new Usuario();
        usuario.setEmail("admin@teste.com");
        usuario.setSenha(passwordEncoder.encode("123456"));
        usuario.setNivelAcesso(NivelAcesso.ADMIN);
        when(usuarioRepository.findByEmail("admin@teste.com")).thenReturn(Optional.of(usuario));
        when(jwtUtil.generateToken("admin@teste.com", "ADMIN")).thenReturn("jwt");

        ResponseEntity<?> response = controller.login(new AuthDTO("admin@teste.com", "123456"));

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(Map.of("token", "jwt", "tipo", "ADMIN"));
    }

    @Test
    void loginInvalidoRetornaUnauthorized() {
        Usuario usuario = new Usuario();
        usuario.setSenha(passwordEncoder.encode("correta"));
        usuario.setNivelAcesso(NivelAcesso.PADRAO);
        when(usuarioRepository.findByEmail("user@teste.com")).thenReturn(Optional.of(usuario));

        ResponseEntity<?> response = controller.login(new AuthDTO("user@teste.com", "errada"));

        assertThat(response.getStatusCode().value()).isEqualTo(401);
        assertThat(response.getBody()).isEqualTo("Credenciais Inválidas!");
    }

    @Test
    void pingEMeRetornamPayloadEsperado() {
        assertThat(controller.pong().getBody()).isEqualTo(Map.of("message", "Pong!"));

        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(
                "admin@teste.com",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
            )
        );

        assertThat(controller.me().getBody()).isEqualTo(Map.of("email", "admin@teste.com", "tipo", "ADMIN"));
    }
}
