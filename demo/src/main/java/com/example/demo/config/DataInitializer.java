package com.example.demo.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.demo.entity.Usuario;
import com.example.demo.enums.NivelAcesso;
import com.example.demo.repository.UsuarioRepository;

@Configuration
public class DataInitializer {

    private final PasswordEncoder passwordEncoder;

    public DataInitializer(PasswordEncoder passwordEncoder){
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    public CommandLineRunner initDatabase(UsuarioRepository repository){
        return args -> {
            salvarUsuario(repository, "admin@admin.com", "00000000000", NivelAcesso.ADMIN, "0");
            salvarUsuario(repository, "salvavidas@salvavidas.com", "11111111111", NivelAcesso.PADRAO, "1");
            System.out.println("Usuário ADMIN disponível: admin@admin.com / 123456789");
            System.out.println("Usuário SALVA-VIDAS disponível: salvavidas@salvavidas.com / 123456789");
        };
    }

    private void salvarUsuario(UsuarioRepository repository, String email, String cpf, NivelAcesso nivel, String tipoUsuario) {
        Usuario usuario = repository.findByEmail(email).orElseGet(Usuario::new);

        usuario.setEmail(email);
        usuario.setCpf(cpf);
        usuario.setNivelAcesso(nivel);
        usuario.setTipoUsuario(tipoUsuario);
        usuario.setSenha(passwordEncoder.encode("123456789"));
        usuario.setAtivo(true);

        repository.save(usuario);
    }
    

}
