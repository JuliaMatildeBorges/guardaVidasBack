package com.example.demo.controller;

import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.annotations.Public;
import com.example.demo.config.JwtUtil;
import com.example.demo.dto.AuthDTO;

import com.example.demo.entity.Usuario;
import com.example.demo.repository.UsuarioRepository;
import com.example.demo.service.UsuarioService;
import com.example.demo.util.CpfUtil;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UsuarioService usuarioService;

    @PostMapping("/login")
    @Public
    public ResponseEntity<?> login(@RequestBody @Valid AuthDTO dto) {
        String cpf = CpfUtil.somenteNumeros(dto.getCpf());
        String senha = dto.getSenha(); // TEXTO PURO

        Optional<Usuario> usuarioOpt = usuarioRepository.findByCpf(cpf);

        if (usuarioOpt.isPresent() && passwordEncoder.matches(senha, usuarioOpt.get().getSenha())) {
            String nivelAcesso = usuarioOpt.get().getNivelAcesso().toString();

            String token = jwtUtil.generateToken(cpf, nivelAcesso);

            return ResponseEntity.ok(Map.of(
                "token", token,
                "tipo", nivelAcesso,
                "cpf", CpfUtil.formatar(usuarioOpt.get().getCpf()),
                "nome", usuarioOpt.get().getNome()
            ));
        }

        return ResponseEntity.status(401).body(Map.of("message", "CPF ou senha incorretos."));
    }

    @GetMapping("/ping")    
    @Public
    public ResponseEntity<?> pong(){
        return ResponseEntity.ok(Map.of("message", "Pong!"));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Usuario usuario = usuarioRepository.findByCpf(authentication.getName()).orElseThrow();

        return ResponseEntity.ok(Map.of(
            "cpf", CpfUtil.formatar(usuario.getCpf()),
            "nome", usuario.getNome(),
            "tipo", authentication.getAuthorities().stream()
                .findFirst()
                .map(authority -> authority.getAuthority().replace("ROLE_", ""))
                .orElse("")
        ));
    }



}
