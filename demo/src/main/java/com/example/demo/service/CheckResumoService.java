package com.example.demo.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.example.demo.dto.AcaoCheckResumoDTO;
import com.example.demo.dto.ArquivoResumoDTO;
import com.example.demo.dto.PostoCheckResumoDTO;
import com.example.demo.entity.Arquivo;
import com.example.demo.entity.Checkin;
import com.example.demo.entity.Checkout;
import com.example.demo.entity.Posto;
import com.example.demo.repository.CheckinRepository;
import com.example.demo.repository.CheckoutRespository;
import com.example.demo.repository.PostoRepository;
import com.example.demo.repository.UsuarioRepository;

@Service
public class CheckResumoService {

    @Autowired
    private PostoRepository postoRepository;

    @Autowired
    private CheckinRepository checkinRepository;

    @Autowired
    private CheckoutRespository checkoutRespository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    public List<PostoCheckResumoDTO> statusHoje() {
        LocalDate hoje = LocalDate.now();
        LocalDateTime inicio = hoje.atStartOfDay();
        LocalDateTime fim = hoje.atTime(LocalTime.MAX);

        List<Checkin> checkins = checkinRepository.findByCreatedAtBetween(inicio, fim);
        List<Checkout> checkouts = checkoutRespository.findByCreatedAtBetween(inicio, fim);

        return postoRepository.findAll().stream()
            .map(posto -> montarResumo(posto, checkins, checkouts))
            .toList();
    }

    public List<PostoCheckResumoDTO> meusChecksHoje() {
        LocalDate hoje = LocalDate.now();
        LocalDateTime inicio = hoje.atStartOfDay();
        LocalDateTime fim = hoje.atTime(LocalTime.MAX);
        var usuario = usuarioRepository.findByCpf(SecurityContextHolder.getContext().getAuthentication().getName()).orElseThrow();

        List<Checkin> checkins = checkinRepository.findByUsuarioAndCreatedAtBetween(usuario, inicio, fim);
        List<Checkout> checkouts = checkoutRespository.findByUsuarioAndCreatedAtBetween(usuario, inicio, fim);

        return postoRepository.findAll().stream()
            .map(posto -> montarResumo(posto, checkins, checkouts))
            .toList();
    }

    private PostoCheckResumoDTO montarResumo(Posto posto, List<Checkin> checkins, List<Checkout> checkouts) {
        PostoCheckResumoDTO dto = new PostoCheckResumoDTO();
        dto.setPostoId(posto.getId());
        dto.setPosto(posto.getNome());

        checkins.stream()
            .filter(checkin -> checkin.getPosto().getId().equals(posto.getId()))
            .max(Comparator.comparing(Checkin::getCreatedAt))
            .ifPresent(checkin -> dto.setCheckin(montarCheckin(checkin)));

        checkouts.stream()
            .filter(checkout -> checkout.getPosto().getId().equals(posto.getId()))
            .max(Comparator.comparing(Checkout::getCreatedAt))
            .ifPresent(checkout -> dto.setCheckout(montarCheckout(checkout)));

        return dto;
    }

    private AcaoCheckResumoDTO montarCheckin(Checkin checkin) {
        AcaoCheckResumoDTO dto = new AcaoCheckResumoDTO();
        dto.setHorario(checkin.getCreatedAt());
        dto.setStatus(checkin.getCreatedAt().toLocalTime().isAfter(LocalTime.of(8, 0)) ? "AMARELO" : "VERDE");
        dto.setUsuario(checkin.getUsuario() != null ? checkin.getUsuario().getNome() : "");
        dto.setFotos(mapearFotos(checkin.getFotos()));
        return dto;
    }

    private AcaoCheckResumoDTO montarCheckout(Checkout checkout) {
        AcaoCheckResumoDTO dto = new AcaoCheckResumoDTO();
        dto.setHorario(checkout.getCreatedAt());
        dto.setStatus(checkout.getCreatedAt().toLocalTime().isBefore(LocalTime.of(19, 0)) ? "AMARELO" : "VERDE");
        dto.setUsuario(checkout.getUsuario() != null ? checkout.getUsuario().getNome() : "");
        dto.setFotos(mapearFotos(checkout.getFotos()));
        dto.setPrevencoesManha(checkout.getPrevencoesManha());
        dto.setPrevencoesTarde(checkout.getPrevencoesTarde());
        dto.setLesoesAguaVivaManha(checkout.getLesoesAguaVivaManha());
        dto.setLesoesAguaVivaTarde(checkout.getLesoesAguaVivaTarde());
        return dto;
    }

    private List<ArquivoResumoDTO> mapearFotos(List<Arquivo> fotos) {
        if (fotos == null) {
            return List.of();
        }

        return fotos.stream()
            .map(foto -> new ArquivoResumoDTO(foto.getId(), foto.getNome(), "/arquivos/" + foto.getId()))
            .toList();
    }
}
