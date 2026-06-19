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
            salvarUsuario(repository, "Administrador", "11111111111", "111111", NivelAcesso.ADMIN, "0");
            salvarUsuario(repository, "Usuário Comum", "22222222222", "222222", NivelAcesso.USUARIO, "1");
            System.out.println("Usuário ADMIN disponível: 11111111111 / 111111");
            System.out.println("Usuário COMUM disponível: 22222222222 / 222222");
        };
    }

    private void salvarUsuario(UsuarioRepository repository, String nome, String cpf, String senha, NivelAcesso nivel, String tipoUsuario) {
        Usuario usuario = repository.findByCpf(cpf).orElseGet(Usuario::new);

        usuario.setNome(nome);
        usuario.setCpf(cpf);
        usuario.setEmail(cpf + "@local");
        usuario.setNivelAcesso(nivel);
        usuario.setTipoUsuario(tipoUsuario);
        usuario.setSenha(passwordEncoder.encode(senha));
        usuario.setAtivo(true);

        repository.save(usuario);
    }
    

}
