package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.test.context.ActiveProfiles;

import com.example.demo.dto.AcaoCheckResumoDTO;
import com.example.demo.dto.ArquivoResumoDTO;
import com.example.demo.dto.PostoCheckResumoDTO;
import com.example.demo.entity.Arquivo;
import com.example.demo.entity.Checkin;
import com.example.demo.entity.Checkout;
import com.example.demo.entity.Posto;
import com.example.demo.entity.Usuario;
import com.example.demo.repository.ArquivoRepository;
import com.example.demo.repository.CheckinRepository;
import com.example.demo.repository.CheckoutRespository;
import com.example.demo.repository.PostoRepository;
import com.example.demo.repository.UsuarioRepository;

@SpringBootTest
@ActiveProfiles("test")
public class CheckResumoServiceTest {

    @Autowired
    private CheckResumoService checkResumoService;

    @Autowired
    private PostoRepository postoRepository;

    @Autowired
    private CheckinRepository checkinRepository;

    @Autowired
    private CheckoutRespository checkoutRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ArquivoRepository arquivoRepository;

    @BeforeEach
    void setup() {

        checkoutRepository.deleteAll();
        checkinRepository.deleteAll();
        arquivoRepository.deleteAll();
        usuarioRepository.deleteAll();
        postoRepository.deleteAll();
    }

    @Test
    @DisplayName("Deve retornar resumo vazio quando não houver checkins e checkouts")
    void deveRetornarResumoVazio() {

        Posto posto = new Posto();
        posto.setNome("Posto Central");

        posto = postoRepository.save(posto);

        List<PostoCheckResumoDTO> resultado = checkResumoService.statusHoje();

        assertEquals(1, resultado.size());

        PostoCheckResumoDTO dto = resultado.get(0);

        assertEquals(posto.getId(), dto.getPostoId());
        assertEquals("Posto Central", dto.getPosto());
    }

    @Test
    @DisplayName("Deve retornar checkin verde")
    void deveRetornarCheckinVerde() {

        Usuario usuario = new Usuario();
        usuario.setEmail("teste@email.com");
        usuario.setSenha("123");

        usuario = usuarioRepository.save(usuario);

        Posto posto = new Posto();
        posto.setNome("Posto Verde");

        posto = postoRepository.save(posto);

        Checkin checkin = new Checkin();

        checkin.setUsuario(usuario);
        checkin.setPosto(posto);
        checkin.setCreatedAt(LocalDateTime.now().withHour(7).withMinute(30));

        checkinRepository.save(checkin);

        List<PostoCheckResumoDTO> resultado = checkResumoService.statusHoje();

        AcaoCheckResumoDTO dto = resultado.get(0).getCheckin();

        assertNotNull(dto);
        assertEquals("VERDE", dto.getStatus());
        assertEquals("teste@email.com", dto.getUsuario());
    }

    @Test
    @DisplayName("Deve retornar checkin amarelo")
    void deveRetornarCheckinAmarelo() {

        Usuario usuario = new Usuario();
        usuario.setEmail("amarelo@email.com");
        usuario.setSenha("123");

        usuario = usuarioRepository.save(usuario);

        Posto posto = new Posto();
        posto.setNome("Posto Amarelo");

        posto = postoRepository.save(posto);

        Checkin checkin = new Checkin();

        checkin.setUsuario(usuario);
        checkin.setPosto(posto);
        checkin.setCreatedAt(LocalDateTime.now().withHour(9).withMinute(0));

        checkinRepository.save(checkin);

        List<PostoCheckResumoDTO> resultado = checkResumoService.statusHoje();

        AcaoCheckResumoDTO dto = resultado.get(0).getCheckin();

        assertEquals("AMARELO", dto.getStatus());
    }

    @Test
    @DisplayName("Deve retornar checkout verde")
    void deveRetornarCheckoutVerde() {

        Usuario usuario = new Usuario();
        usuario.setEmail("checkout@email.com");
        usuario.setSenha("123");

        usuario = usuarioRepository.save(usuario);

        Posto posto = new Posto();
        posto.setNome("Posto Checkout");

        posto = postoRepository.save(posto);

        Checkout checkout = new Checkout();

        checkout.setUsuario(usuario);
        checkout.setPosto(posto);

        checkout.setCreatedAt(
                LocalDateTime.now()
                        .withHour(20)
                        .withMinute(0));

        checkoutRepository.save(checkout);

        List<PostoCheckResumoDTO> resultado = checkResumoService.statusHoje();

        AcaoCheckResumoDTO dto = resultado.get(0).getCheckout();

        assertEquals("VERDE", dto.getStatus());
    }

    @Test
    @DisplayName("Deve retornar checkout amarelo")
    void deveRetornarCheckoutAmarelo() {

        Usuario usuario = new Usuario();
        usuario.setEmail("checkout2@email.com");
        usuario.setSenha("123");

        usuario = usuarioRepository.save(usuario);

        Posto posto = new Posto();
        posto.setNome("Posto Checkout 2");

        posto = postoRepository.save(posto);

        Checkout checkout = new Checkout();

        checkout.setUsuario(usuario);
        checkout.setPosto(posto);

        checkout.setCreatedAt(
                LocalDateTime.now()
                        .withHour(18)
                        .withMinute(0));

        checkoutRepository.save(checkout);

        List<PostoCheckResumoDTO> resultado = checkResumoService.statusHoje();

        AcaoCheckResumoDTO dto = resultado.get(0).getCheckout();

        assertEquals("AMARELO", dto.getStatus());
    }

    @Test
    @DisplayName("Deve pegar checkin mais recente")
    void devePegarCheckinMaisRecente() {

        Usuario usuario = new Usuario();
        usuario.setEmail("recente@email.com");
        usuario.setSenha("123");

        usuario = usuarioRepository.save(usuario);

        Posto posto = new Posto();
        posto.setNome("Posto Recente");

        posto = postoRepository.save(posto);

        Checkin antigo = new Checkin();

        antigo.setUsuario(usuario);
        antigo.setPosto(posto);
        antigo.setCreatedAt(LocalDateTime.now().withHour(7));

        checkinRepository.save(antigo);

        Checkin recente = new Checkin();

        recente.setUsuario(usuario);
        recente.setPosto(posto);
        recente.setCreatedAt(LocalDateTime.now().withHour(10));

        checkinRepository.save(recente);

        List<PostoCheckResumoDTO> resultado = checkResumoService.statusHoje();

        AcaoCheckResumoDTO dto = resultado.get(0).getCheckin();

        assertEquals("AMARELO", dto.getStatus());
        assertEquals(10, dto.getHorario().getHour());
    }

    @Test
    @DisplayName("Deve mapear fotos corretamente")
    void deveMapearFotos() {

        Usuario usuario = new Usuario();
        usuario.setEmail("foto@email.com");
        usuario.setSenha("123");

        usuario = usuarioRepository.save(usuario);

        Posto posto = new Posto();
        posto.setNome("Posto Fotos");

        posto = postoRepository.save(posto);

        Arquivo arquivo = new Arquivo();

        arquivo.setNome("imagem.png");
        arquivo.setTipo("image/png");
        arquivo.setCaminho("/uploads/imagem.png");

        arquivo = arquivoRepository.save(arquivo);

        Checkin checkin = new Checkin();

        checkin.setUsuario(usuario);
        checkin.setPosto(posto);
        checkin.setCreatedAt(LocalDateTime.now());

        List<Arquivo> fotos = new ArrayList<>();
        fotos.add(arquivo);

        checkin.setFotos(fotos);

        checkinRepository.save(checkin);

        List<PostoCheckResumoDTO> resultado = checkResumoService.statusHoje();

        List<ArquivoResumoDTO> fotosResultado =
                resultado.get(0).getCheckin().getFotos();

        assertEquals(1, fotosResultado.size());

        ArquivoResumoDTO dto = fotosResultado.get(0);

        assertEquals(arquivo.getId(), dto.getId());
        assertEquals("imagem.png", dto.getNome());
        assertEquals("/arquivos/" + arquivo.getId(), dto.getUrl());
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando fotos forem null")
    void deveRetornarListaVaziaQuandoFotosNull() {

        Usuario usuario = new Usuario();
        usuario.setEmail("null@email.com");
        usuario.setSenha("123");

        usuario = usuarioRepository.save(usuario);

        Posto posto = new Posto();
        posto.setNome("Posto Null");

        posto = postoRepository.save(posto);

        Checkin checkin = new Checkin();

        checkin.setUsuario(usuario);
        checkin.setPosto(posto);
        checkin.setCreatedAt(LocalDateTime.now());

        checkin.setFotos(null);

        checkinRepository.save(checkin);

        List<PostoCheckResumoDTO> resultado = checkResumoService.statusHoje();

        assertEquals(
                0,
                resultado.get(0).getCheckin().getFotos().size());
    }

    @Test
    @DisplayName("Deve retornar usuário vazio quando usuário for null")
    void deveRetornarUsuarioVazio() {

        Posto posto = new Posto();
        posto.setNome("Posto Sem Usuario");

        posto = postoRepository.save(posto);

        Checkin checkin = new Checkin();

        checkin.setUsuario(null);
        checkin.setPosto(posto);
        checkin.setCreatedAt(LocalDateTime.now());

        checkinRepository.save(checkin);

        List<PostoCheckResumoDTO> resultado = checkResumoService.statusHoje();

        assertEquals(
                "",
                resultado.get(0).getCheckin().getUsuario());
    }

    @Test
    @DisplayName("Deve mapear dados de prevenções e lesões")
    void deveMapearDadosPrevençõesELesoes() {

        Usuario usuario = new Usuario();
        usuario.setEmail("prevencao@email.com");
        usuario.setSenha("123");

        usuario = usuarioRepository.save(usuario);

        Posto posto = new Posto();
        posto.setNome("Posto Saúde");

        posto = postoRepository.save(posto);

        Checkout checkout = new Checkout();

        checkout.setUsuario(usuario);
        checkout.setPosto(posto);

        checkout.setCreatedAt(LocalDateTime.now().withHour(20));

        checkout.setPrevencoesManha(10);
        checkout.setPrevencoesTarde(20);

        checkout.setLesoesAguaVivaManha(2);
        checkout.setLesoesAguaVivaTarde(3);

        checkoutRepository.save(checkout);

        List<PostoCheckResumoDTO> resultado = checkResumoService.statusHoje();

        AcaoCheckResumoDTO dto = resultado.get(0).getCheckout();

        assertEquals(10, dto.getPrevencoesManha());
        assertEquals(20, dto.getPrevencoesTarde());

        assertEquals(2, dto.getLesoesAguaVivaManha());
        assertEquals(3, dto.getLesoesAguaVivaTarde());
    }

    @Test
    @DisplayName("Deve ignorar checkins de outros postos")
    void deveIgnorarCheckinsDeOutrosPostos() {

        Usuario usuario = new Usuario();
        usuario.setEmail("posto@email.com");
        usuario.setSenha("123");

        usuario = usuarioRepository.save(usuario);

        Posto posto1 = new Posto();
        posto1.setNome("Posto 1");

        posto1 = postoRepository.save(posto1);

        Posto posto2 = new Posto();
        posto2.setNome("Posto 2");

        posto2 = postoRepository.save(posto2);

        Checkin checkin = new Checkin();

        checkin.setUsuario(usuario);
        checkin.setPosto(posto2);
        checkin.setCreatedAt(LocalDateTime.now());

        checkinRepository.save(checkin);

        List<PostoCheckResumoDTO> resultado = checkResumoService.statusHoje();

        PostoCheckResumoDTO dtoPosto1 = resultado.stream()
                .filter(dto -> dto.getPostoId().equals(posto1.getId()))
                .findFirst()
                .orElseThrow();

        assertEquals(null, dtoPosto1.getCheckin());
    }

}