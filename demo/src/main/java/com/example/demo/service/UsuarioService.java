package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.Local;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.dto.RecuperacaoSolicitacaoDTO;
import com.example.demo.dto.RecuperarSenhaDTO;
import com.example.demo.dto.UsuarioDTO;
import com.example.demo.entity.Usuario;
import com.example.demo.repository.UsuarioRepository;

import jakarta.transaction.Transactional;

@Service
public class UsuarioService extends BaseService<Usuario, UsuarioDTO> {

    private UsuarioRepository repository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;
 
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

    @Transactional
    public void trocarSenha(RecuperarSenhaDTO dto){
        String email = dto.getEmail();
        String codigo = dto.getCodigo();
        String novaSenha = dto.getNovaSenha();

        // verificar se o email esta cadastrado

        Usuario usuario = repository.findByEmail(email).orElseThrow();


        // verificações se  o codigo esta correto
        if(usuario.getCodigoRecuperacao() == null || usuario.getCodigoRecuperacaoExpiracao() == null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nenhuma solicitação de recuperação foi feita");
        }

        if(!usuario.getCodigoRecuperacao().equals(codigo)){
           throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Codigo incorreto");
        }

        if(usuario.getCodigoRecuperacaoExpiracao().isBefore(LocalDateTime.now())){
             throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Codigo expirado");

        }


        String novaSenhaCritografada = passwordEncoder.encode(novaSenha);
        usuario.setSenha(novaSenhaCritografada);

        usuario.setCodigoRecuperacao(null);
        usuario.setCodigoRecuperacaoExpiracao(null);

        repository.save(usuario);



    }


}
