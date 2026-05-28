package com.example.demo.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class JwtFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private JwtFilter jwtFilter;

    @BeforeEach
    void setup() {

        MockitoAnnotations.openMocks(this);

        jwtFilter = new JwtFilter(jwtUtil);

        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Deve autenticar usuário com token válido")
    void deveAutenticarUsuario()
            throws ServletException, IOException {

        String token = "token-valido";

        when(request.getHeader("Authorization"))
                .thenReturn("Bearer " + token);

        when(jwtUtil.validateToken(token))
                .thenReturn(true);

        when(jwtUtil.extractUsername(token))
                .thenReturn("admin@email.com");

        when(jwtUtil.extractRole(token))
                .thenReturn("ADMIN");

        jwtFilter.doFilterInternal(
                request,
                response,
                filterChain
        );

        Authentication auth =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        assertNotNull(auth);

        assertEquals(
                "admin@email.com",
                auth.getName()
        );

        assertTrue(
                auth.getAuthorities()
                        .stream()
                        .anyMatch(a ->
                                a.getAuthority()
                                        .equals("ROLE_ADMIN"))
        );

        verify(filterChain)
                .doFilter(request, response);
    }

    @Test
    @DisplayName("Não deve autenticar token inválido")
    void naoDeveAutenticarTokenInvalido()
            throws ServletException, IOException {

        String token = "token-invalido";

        when(request.getHeader("Authorization"))
                .thenReturn("Bearer " + token);

        when(jwtUtil.validateToken(token))
                .thenReturn(false);

        jwtFilter.doFilterInternal(
                request,
                response,
                filterChain
        );

        Authentication auth =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        assertEquals(null, auth);

        verify(filterChain)
                .doFilter(request, response);
    }

    @Test
    @DisplayName("Não deve autenticar sem header")
    void naoDeveAutenticarSemHeader()
            throws ServletException, IOException {

        when(request.getHeader("Authorization"))
                .thenReturn(null);

        jwtFilter.doFilterInternal(
                request,
                response,
                filterChain
        );

        Authentication auth =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        assertEquals(null, auth);

        verify(filterChain)
                .doFilter(request, response);
    }

    @Test
    @DisplayName("Deve criar authorities vazias quando role for null")
    void deveCriarAuthoritiesVazias()
            throws ServletException, IOException {

        String token = "token";

        when(request.getHeader("Authorization"))
                .thenReturn("Bearer " + token);

        when(jwtUtil.validateToken(token))
                .thenReturn(true);

        when(jwtUtil.extractUsername(token))
                .thenReturn("user@email.com");

        when(jwtUtil.extractRole(token))
                .thenReturn(null);

        jwtFilter.doFilterInternal(
                request,
                response,
                filterChain
        );

        Authentication auth =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        assertNotNull(auth);

        assertEquals(
                0,
                auth.getAuthorities().size()
        );
    }

}