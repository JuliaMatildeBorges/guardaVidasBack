package com.example.demo.service;

import com.example.demo.entity.Arquivo;
import com.example.demo.entity.Checkin;
import com.example.demo.entity.Checkout;
import com.example.demo.repository.ArquivoRepository;
import com.example.demo.repository.CheckinRepository;
import com.example.demo.repository.CheckoutRespository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ArquivoServiceTest {

    @TempDir
    Path tempDir;

    private ArquivoService service;
    private ArquivoRepository arquivoRepository;
    private CheckinRepository checkinRepository;
    private CheckoutRespository checkoutRespository;

    @BeforeEach
    void setUp() {
        service = new ArquivoService();
        arquivoRepository = mock(ArquivoRepository.class);
        checkinRepository = mock(CheckinRepository.class);
        checkoutRespository = mock(CheckoutRespository.class);

        ReflectionTestUtils.setField(service, "path", tempDir.toString());
        ReflectionTestUtils.setField(service, "arquivoRepository", arquivoRepository);
        ReflectionTestUtils.setField(service, "checkinRepository", checkinRepository);
        ReflectionTestUtils.setField(service, "checkoutRespository", checkoutRespository);
    }

    @Test
    void uploadSanitizaNomeCopiaArquivoEPersisteMetadados() throws Exception {
        when(arquivoRepository.save(any(Arquivo.class))).thenAnswer(invocation -> {
            Arquivo arquivo = invocation.getArgument(0);
            arquivo.setId(UUID.randomUUID());
            return arquivo;
        });

        Arquivo arquivo = service.upload(new MockMultipartFile(
            "foto", "minha foto!.jpg", "image/jpeg", new byte[] { 1, 2, 3 }
        ));

        assertThat(arquivo.getId()).isNotNull();
        assertThat(arquivo.getNome()).isEqualTo("minha foto!.jpg");
        assertThat(arquivo.getTipo()).isEqualTo("image/jpeg");
        assertThat(arquivo.getTamanho()).isEqualTo(3L);
        assertThat(Files.exists(Path.of(arquivo.getCaminho()))).isTrue();
        assertThat(Path.of(arquivo.getCaminho()).getFileName().toString()).contains("minha_foto_.jpg");
    }

    @Test
    void buscarRetornaArquivoOuPropagaAusencia() {
        UUID id = UUID.randomUUID();
        Arquivo arquivo = arquivo(id, tempDir.resolve("foto.jpg"));
        when(arquivoRepository.findById(id)).thenReturn(Optional.of(arquivo));

        assertThat(service.buscar(id)).isSameAs(arquivo);

        assertThatThrownBy(() -> service.buscar(UUID.randomUUID()))
            .isInstanceOf(java.util.NoSuchElementException.class);
    }

    @Test
    void excluirDesvinculaCheckinsCheckoutsRemoveArquivoFisicoERegistro() throws Exception {
        Path arquivoFisico = Files.write(tempDir.resolve("foto.jpg"), new byte[] { 1 });
        UUID id = UUID.randomUUID();
        Arquivo alvo = arquivo(id, arquivoFisico);
        Arquivo remanescente = arquivo(UUID.randomUUID(), tempDir.resolve("outra.jpg"));
        Checkin checkin = new Checkin();
        checkin.setFoto(alvo);
        checkin.setFotos(new ArrayList<>(List.of(alvo, remanescente)));
        Checkout checkout = new Checkout();
        checkout.setFoto(alvo);
        checkout.setFotos(new ArrayList<>(List.of(alvo)));

        when(arquivoRepository.findById(id)).thenReturn(Optional.of(alvo));
        when(checkinRepository.findAll()).thenReturn(List.of(checkin));
        when(checkoutRespository.findAll()).thenReturn(List.of(checkout));

        service.excluir(id);

        assertThat(Files.exists(arquivoFisico)).isFalse();
        assertThat(checkin.getFoto()).isEqualTo(remanescente);
        assertThat(checkout.getFoto()).isNull();
        verify(checkinRepository).save(checkin);
        verify(checkoutRespository).save(checkout);
        verify(arquivoRepository).delete(alvo);
    }

    private Arquivo arquivo(UUID id, Path caminho) {
        Arquivo arquivo = new Arquivo();
        arquivo.setId(id);
        arquivo.setNome(caminho.getFileName().toString());
        arquivo.setTipo("image/jpeg");
        arquivo.setTamanho(1L);
        arquivo.setCaminho(caminho.toString());
        return arquivo;
    }
}
