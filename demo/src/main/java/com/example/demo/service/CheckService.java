package com.example.demo.service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dto.CheckinDTO;
import com.example.demo.dto.CheckinResponseDTO;
import com.example.demo.entity.Arquivo;
import com.example.demo.entity.Checkin;
import com.example.demo.entity.Posto;
import com.example.demo.entity.Usuario;
import com.example.demo.repository.CheckinRepository;
import com.example.demo.repository.PostoRepository;
import com.example.demo.repository.UsuarioRepository;

@Service
public class CheckService {

    @Autowired
    private PostoRepository postoRepository;

    @Autowired
    private ArquivoService arquivoService;

    @Autowired
    private CheckinRepository checkinRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    public CheckinResponseDTO checkin(CheckinDTO dto){
        
        Posto posto = postoRepository.findById(dto.getPostoId()).orElseThrow();
        Usuario usuario = usuarioRepository.findByEmail(usuarioLogado()).orElseThrow();
        List<MultipartFile> fotos = normalizarFotos(dto.getFotos(), dto.getFoto());

        Checkin checkin = new Checkin();
        checkin.setPosto(posto);
        checkin.setUsuario(usuario);

        List<Arquivo> arquivos = fotos.stream().map(arquivoService::upload).toList();

        checkin.setFoto(arquivos.get(0));
        checkin.setFotos(arquivos);

        Checkin checkinSalvo = checkinRepository.save(checkin);
        CheckinResponseDTO crd = new CheckinResponseDTO();

        crd.setPosto(posto.getNome());
        crd.setHorario(checkinSalvo.getCreatedAt());
        crd.setStatus(statusCheckin(checkinSalvo.getCreatedAt()));
        crd.setFotos(arquivos.stream().map(Arquivo::getId).toList());

        return crd;

    }

    private List<MultipartFile> normalizarFotos(MultipartFile[] fotos, MultipartFile foto) {
        List<MultipartFile> arquivos = fotos != null ? Arrays.stream(fotos).filter(f -> f != null && !f.isEmpty()).toList() : List.of();

        if (arquivos.isEmpty() && foto != null && !foto.isEmpty()) {
            arquivos = List.of(foto);
        }

        if (arquivos.isEmpty() || arquivos.size() > 3) {
            throw new IllegalArgumentException("Envie de 1 até 3 fotos.");
        }

        return arquivos;
    }

    private String statusCheckin(LocalDateTime horario) {
        return horario.toLocalTime().isAfter(LocalTime.of(8, 0)) ? "AMARELO" : "VERDE";
    }

    private String usuarioLogado() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

}
