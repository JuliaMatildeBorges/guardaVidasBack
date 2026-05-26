package com.example.demo.service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dto.CheckoutDTO;
import com.example.demo.dto.CheckoutResponseDTO;
import com.example.demo.entity.Arquivo;
import com.example.demo.entity.Checkout;
import com.example.demo.entity.Posto;
import com.example.demo.entity.Usuario;
import com.example.demo.repository.CheckoutRespository;
import com.example.demo.repository.PostoRepository;
import com.example.demo.repository.UsuarioRepository;

@Service
public class CheckoutService {

    @Autowired
    private PostoRepository postoRepository;

    @Autowired
    private ArquivoService arquivoService;

    @Autowired
    private CheckoutRespository checkoutRespository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    public CheckoutResponseDTO checkout(CheckoutDTO dto){
        
        Posto posto = postoRepository.findById(dto.getPostoId()).orElseThrow();
        Usuario usuario = usuarioRepository.findByEmail(usuarioLogado()).orElseThrow();
        List<MultipartFile> fotos = normalizarFotos(dto.getFotos(), dto.getFoto());

        Checkout checkout = new Checkout();
        checkout.setPosto(posto);
        checkout.setUsuario(usuario);
        checkout.setPrevencoesManha(dto.getPrevencoesManha());
        checkout.setPrevencoesTarde(dto.getPrevencoesTarde());
        checkout.setLesoesAguaVivaManha(dto.getLesoesAguaVivaManha());
        checkout.setLesoesAguaVivaTarde(dto.getLesoesAguaVivaTarde());

        List<Arquivo> arquivos = fotos.stream().map(arquivoService::upload).toList();

        checkout.setFoto(arquivos.get(0));
        checkout.setFotos(arquivos);

        Checkout checkoutSalvo = checkoutRespository.save(checkout);
        CheckoutResponseDTO crd = new CheckoutResponseDTO();

        crd.setPosto(posto.getNome());
        crd.setHorario(checkoutSalvo.getCreatedAt());
        crd.setStatus(statusCheckout(checkoutSalvo.getCreatedAt()));
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

    private String statusCheckout(LocalDateTime horario) {
        return horario.toLocalTime().isBefore(LocalTime.of(19, 0)) ? "AMARELO" : "VERDE";
    }

    private String usuarioLogado() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

}
