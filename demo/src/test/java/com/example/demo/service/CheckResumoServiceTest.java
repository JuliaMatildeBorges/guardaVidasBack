package com.example.demo.service;

import com.example.demo.dto.PostoCheckResumoDTO;
import com.example.demo.entity.Arquivo;
import com.example.demo.entity.Checkin;
import com.example.demo.entity.Checkout;
import com.example.demo.entity.Posto;
import com.example.demo.entity.Usuario;
import com.example.demo.repository.CheckinRepository;
import com.example.demo.repository.CheckoutRespository;
import com.example.demo.repository.PostoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CheckResumoServiceTest {

    private CheckResumoService service;
    private Posto posto;

    @BeforeEach
    void setUp() {
        service = new CheckResumoService();
        PostoRepository postoRepository = mock(PostoRepository.class);
        CheckinRepository checkinRepository = mock(CheckinRepository.class);
        CheckoutRespository checkoutRespository = mock(CheckoutRespository.class);

        posto = new Posto();
        posto.setId(1L);
        posto.setNome("Posto Central");

        when(postoRepository.findAll()).thenReturn(List.of(posto, posto(2L, "Posto Sem Acao")));
        when(checkinRepository.findByCreatedAtBetween(any(), any())).thenReturn(List.of(
            checkin(LocalDateTime.now().withHour(7), "antigo.jpg"),
            checkin(LocalDateTime.now().withHour(9), "recente.jpg")
        ));
        when(checkoutRespository.findByCreatedAtBetween(any(), any())).thenReturn(List.of(
            checkout(LocalDateTime.now().withHour(18), null),
            checkout(LocalDateTime.now().withHour(19), "saida.jpg")
        ));

        ReflectionTestUtils.setField(service, "postoRepository", postoRepository);
        ReflectionTestUtils.setField(service, "checkinRepository", checkinRepository);
        ReflectionTestUtils.setField(service, "checkoutRespository", checkoutRespository);
    }

    @Test
    void statusHojeUsaUltimoCheckinECheckoutDeCadaPosto() {
        List<PostoCheckResumoDTO> resumo = service.statusHoje();

        assertThat(resumo).hasSize(2);
        PostoCheckResumoDTO central = resumo.get(0);

        assertThat(central.getPosto()).isEqualTo("Posto Central");
        assertThat(central.getCheckin().getStatus()).isEqualTo("AMARELO");
        assertThat(central.getCheckin().getUsuario()).isEqualTo("usuario@teste.com");
        assertThat(central.getCheckin().getFotos()).extracting("nome").containsExactly("recente.jpg");
        assertThat(central.getCheckout().getStatus()).isEqualTo("VERDE");
        assertThat(central.getCheckout().getPrevencoesManha()).isEqualTo(1);
        assertThat(resumo.get(1).getCheckin().getHorario()).isNull();
        assertThat(resumo.get(1).getCheckin().getFotos()).isEmpty();
    }

    private Checkin checkin(LocalDateTime horario, String fotoNome) {
        Checkin checkin = new Checkin();
        checkin.setPosto(posto);
        checkin.setUsuario(usuario());
        checkin.setCreatedAt(horario);
        checkin.setFotos(List.of(arquivo(fotoNome)));
        return checkin;
    }

    private Checkout checkout(LocalDateTime horario, String fotoNome) {
        Checkout checkout = new Checkout();
        checkout.setPosto(posto);
        checkout.setUsuario(usuario());
        checkout.setCreatedAt(horario);
        checkout.setFotos(fotoNome == null ? null : List.of(arquivo(fotoNome)));
        checkout.setPrevencoesManha(1);
        checkout.setPrevencoesTarde(2);
        checkout.setLesoesAguaVivaManha(3);
        checkout.setLesoesAguaVivaTarde(4);
        return checkout;
    }

    private Usuario usuario() {
        Usuario usuario = new Usuario();
        usuario.setEmail("usuario@teste.com");
        return usuario;
    }

    private Posto posto(Long id, String nome) {
        Posto outro = new Posto();
        outro.setId(id);
        outro.setNome(nome);
        return outro;
    }

    private Arquivo arquivo(String nome) {
        Arquivo arquivo = new Arquivo();
        arquivo.setId(UUID.randomUUID());
        arquivo.setNome(nome);
        arquivo.setTipo("image/jpeg");
        arquivo.setTamanho(1L);
        arquivo.setCaminho("/tmp/" + nome);
        return arquivo;
    }
}
