package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.Local;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.dto.RecuperacaoSolicitacaoDTO;
import com.example.demo.dto.UsuarioDTO;
import com.example.demo.entity.Usuario;
import com.example.demo.repository.UsuarioRepository;

import jakarta.transaction.Transactional;

@Service
public class UsuarioService extends BaseService<Usuario, UsuarioDTO> {

    private UsuarioRepository repository;

    @Autowired
    private EmailService emailService;

    public UsuarioService(UsuarioRepository repository){
        super(repository);
        this.repository = repository;
    }

    // Recuperacao de senha
    @Transactional
    public void solicitarCodigo(RecuperacaoSolicitacaoDTO dto){
        String email = dto.getEmail();

        Usuario usuario = repository.findByEmail(email).orElseThrow();

        String codigo = String.valueOf(10000000 + new Random().nextInt(90000000));

        usuario.setCodigoRecuperacao(codigo);
        usuario.setCodigoRecuperacaoExpiracao(LocalDateTime.now().plusMinutes(20));

        repository.save(usuario);

        try{
            emailService.enviarEmail(email, "SOLICITACAO DE RECUPERACAO DE SENHA", "SEU CÓDIGO: " + codigo);
        } catch (Exception exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "ERRO NO ENDEREÇO DE EMAIL");
        }


    }

}
