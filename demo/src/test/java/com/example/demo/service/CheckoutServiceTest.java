package com.example.demo.service;

import com.example.demo.dto.CheckoutDTO;
import com.example.demo.dto.CheckoutResponseDTO;
import com.example.demo.entity.Arquivo;
import com.example.demo.entity.Checkout;
import com.example.demo.entity.Posto;
import com.example.demo.entity.Usuario;
import com.example.demo.repository.CheckoutRespository;
import com.example.demo.repository.PostoRepository;
import com.example.demo.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CheckoutServiceTest {

    private CheckoutService service;
    private PostoRepository postoRepository;
    private UsuarioRepository usuarioRepository;
    private CheckoutRespository checkoutRespository;

    @BeforeEach
    void setUp() {
        service = new CheckoutService();
        postoRepository = mock(PostoRepository.class);
        usuarioRepository = mock(UsuarioRepository.class);
        checkoutRespository = mock(CheckoutRespository.class);
        ArquivoService arquivoService = mock(ArquivoService.class);

        ReflectionTestUtils.setField(service, "postoRepository", postoRepository);
        ReflectionTestUtils.setField(service, "usuarioRepository", usuarioRepository);
        ReflectionTestUtils.setField(service, "checkoutRespository", checkoutRespository);
        ReflectionTestUtils.setField(service, "arquivoService", arquivoService);

        Posto posto = new Posto();
        posto.setId(1L);
        posto.setNome("Posto Sul");
        Usuario usuario = new Usuario();
        usuario.setEmail("salva@vidas.com");

        when(postoRepository.findById(1L)).thenReturn(Optional.of(posto));
        when(usuarioRepository.findByEmail("salva@vidas.com")).thenReturn(Optional.of(usuario));
        when(arquivoService.upload(any(MultipartFile.class))).thenAnswer(invocation -> arquivo(invocation.getArgument(0)));
        when(checkoutRespository.save(any(Checkout.class))).thenAnswer(invocation -> {
            Checkout checkout = invocation.getArgument(0);
            checkout.setId(99L);
            checkout.setCreatedAt(LocalDateTime.of(2026, 1, 1, 19, 0));
            return checkout;
        });

        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken("salva@vidas.com", null, List.of())
        );
    }

    @Test
    void checkoutComFotoUnicaRetornaResumoVerdeEIndicadores() {
        CheckoutResponseDTO response = service.checkout(dto(foto("saida.jpg")));

        assertThat(response.getPosto()).isEqualTo("Posto Sul");
        assertThat(response.getStatus()).isEqualTo("VERDE");
        assertThat(response.getHorario()).isEqualTo(LocalDateTime.of(2026, 1, 1, 19, 0));
        assertThat(response.getFotos()).hasSize(1);
    }

    @Test
    void checkoutComTresFotosRetornaTresIds() {
        CheckoutDTO dto = dto(null);
        dto.setFotos(new MultipartFile[] { foto("a.jpg"), foto("b.jpg"), foto("c.jpg") });

        assertThat(service.checkout(dto).getFotos()).hasSize(3);
    }

    @Test
    void checkoutRejeitaSemFotosEPostoInexistente() {
        CheckoutDTO semFoto = dto(null);

        assertThatThrownBy(() -> service.checkout(semFoto))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Envie de 1 até 3 fotos.");

        CheckoutDTO postoInexistente = dto(foto("foto.jpg"));
        postoInexistente.setPostoId(2L);

        assertThatThrownBy(() -> service.checkout(postoInexistente))
            .isInstanceOf(java.util.NoSuchElementException.class);
    }

    @Test
    void statusCheckoutAntesDasDezenoveEhAmarelo() throws Exception {
        var method = CheckoutService.class.getDeclaredMethod("statusCheckout", LocalDateTime.class);
        method.setAccessible(true);

        assertThat(method.invoke(service, LocalDateTime.of(2026, 1, 1, 18, 59))).isEqualTo("AMARELO");
    }

    private CheckoutDTO dto(MultipartFile foto) {
        CheckoutDTO dto = new CheckoutDTO();
        dto.setPostoId(1L);
        dto.setFoto(foto);
        dto.setPrevencoesManha(1);
        dto.setPrevencoesTarde(2);
        dto.setLesoesAguaVivaManha(3);
        dto.setLesoesAguaVivaTarde(4);
        return dto;
    }

    private MultipartFile foto(String nome) {
        return new MockMultipartFile("foto", nome, "image/jpeg", new byte[] { 1, 2, 3 });
    }

    private Arquivo arquivo(MultipartFile file) {
        Arquivo arquivo = new Arquivo();
        arquivo.setId(UUID.randomUUID());
        arquivo.setNome(file.getOriginalFilename());
        arquivo.setTipo(file.getContentType());
        arquivo.setTamanho(file.getSize());
        arquivo.setCaminho("/tmp/" + file.getOriginalFilename());
        return arquivo;
    }
}
