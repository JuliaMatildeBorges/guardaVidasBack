package com.example.demo.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.demo.dto.UsuarioDTO;
import com.example.demo.entity.Usuario;
import com.example.demo.enums.NivelAcesso;
import com.example.demo.repository.UsuarioRepository;
import com.example.demo.util.CpfUtil;

import jakarta.transaction.Transactional;

@Service
public class UsuarioService extends BaseService<Usuario, UsuarioDTO> {

    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository repository, PasswordEncoder passwordEncoder){
        super(repository);
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public UsuarioDTO create(UsuarioDTO dto) {
        String cpf = CpfUtil.somenteNumeros(dto.getCpf());
        validarCadastro(dto, cpf, null);

        Usuario usuario = new Usuario();
        usuario.setNome(dto.getNome().trim());
        usuario.setCpf(cpf);
        usuario.setEmail(cpf + "@local");
        usuario.setSenha(passwordEncoder.encode(dto.getSenha()));
        usuario.setNivelAcesso(dto.getPerfil());
        usuario.setTipoUsuario(dto.getPerfil() == NivelAcesso.ADMIN ? "0" : "1");
        usuario.setAtivo(true);

        return toDto(repository.save(usuario));
    }

    @Override
    @Transactional
    public UsuarioDTO update(Long id, UsuarioDTO dto) {
        Usuario usuario = repository.findById(id).orElseThrow();
        String cpf = CpfUtil.somenteNumeros(dto.getCpf());
        validarCadastro(dto, cpf, id);

        usuario.setNome(dto.getNome().trim());
        usuario.setCpf(cpf);
        usuario.setEmail(cpf + "@local");
        usuario.setNivelAcesso(dto.getPerfil());
        usuario.setTipoUsuario(dto.getPerfil() == NivelAcesso.ADMIN ? "0" : "1");

        if (dto.getSenha() != null && !dto.getSenha().isBlank()) {
            validarSenha(dto);
            usuario.setSenha(passwordEncoder.encode(dto.getSenha()));
        }

        return toDto(repository.save(usuario));
    }

    @Override
    public UsuarioDTO toDto(Usuario usuario) {
        UsuarioDTO dto = new UsuarioDTO();
        dto.setId(usuario.getId());
        dto.setNome(usuario.getNome());
        dto.setCpf(CpfUtil.formatar(usuario.getCpf()));
        dto.setPerfil(usuario.getNivelAcesso());
        return dto;
    }

    private void validarCadastro(UsuarioDTO dto, String cpf, Long usuarioId) {
        if (dto.getNome() == null || dto.getNome().isBlank()) {
            throw new IllegalArgumentException("O nome deve ser preenchido.");
        }

        if (cpf.isBlank()) {
            throw new IllegalArgumentException("O CPF deve ser preenchido.");
        }

        if (!CpfUtil.valido(cpf)) {
            throw new IllegalArgumentException("CPF inválido. Verifique os números informados e tente novamente.");
        }

        if (dto.getPerfil() == null) {
            throw new IllegalArgumentException("O perfil deve ser selecionado.");
        }

        repository.findByCpf(cpf).ifPresent(usuario -> {
            if (usuarioId == null || !usuario.getId().equals(usuarioId)) {
                throw new IllegalArgumentException("Já existe um usuário cadastrado com este CPF.");
            }
        });

        if (usuarioId == null) {
            validarSenha(dto);
        }
    }

    private void validarSenha(UsuarioDTO dto) {
        if (dto.getSenha() == null || dto.getSenha().isBlank()) {
            throw new IllegalArgumentException("A senha deve ser preenchida.");
        }


    }

}

