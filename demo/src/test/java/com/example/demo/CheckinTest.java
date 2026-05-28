package com.example.demo;

import com.example.demo.dto.CheckinDTO;
import com.example.demo.dto.CheckinResponseDTO;
import com.example.demo.entity.Arquivo;
import com.example.demo.entity.Posto;
import com.example.demo.entity.Usuario;
import com.example.demo.enums.NivelAcesso;
import com.example.demo.repository.PostoRepository;
import com.example.demo.repository.ArquivoRepository;
import com.example.demo.repository.UsuarioRepository;
import com.example.demo.service.ArquivoService;
import com.example.demo.service.CheckService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class CheckServiceTest {

    @Autowired
    private CheckService checkService;

    @Autowired
    private PostoRepository postoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // ------------------------------------------------------------------
    // Único fake necessário: ArquivoService (evita acesso ao disco)
    // Repositórios rodam normalmente contra H2 em memória.
    // ------------------------------------------------------------------

    @TestConfiguration
    static class FakeArquivoService {

        @Bean
        @Primary
        ArquivoService arquivoService(ArquivoRepository arquivoRepository) {
            return new ArquivoService() {
                @Override
                public Arquivo upload(MultipartFile file) {
                    Arquivo a = new Arquivo();
                    a.setNome(file.getOriginalFilename() != null ? file.getOriginalFilename() : "foto");
                    a.setTipo(file.getContentType() != null ? file.getContentType() : "image/jpeg");
                    a.setTamanho(file.getSize());
                    a.setCaminho("/fake/" + a.getNome());
                    return arquivoRepository.save(a);
                }
            };
        }
    }

    // ------------------------------------------------------------------
    // Setup: cria posto e usuário reais no H2 antes de cada teste
    // ------------------------------------------------------------------

    private Posto posto;
    private static final String EMAIL_USUARIO = "usuario@teste.com";

    @BeforeEach
    void setup() {
        // Posto
        Posto novoPosto = new Posto();
        novoPosto.setNome("Posto Central");
        posto = postoRepository.save(novoPosto);

        // Usuário (só salva se ainda não existir)
        if (usuarioRepository.findByEmail(EMAIL_USUARIO).isEmpty()) {
            Usuario u = new Usuario();
            u.setEmail(EMAIL_USUARIO);
            u.setSenha(passwordEncoder.encode("senha123"));
            u.setNivelAcesso(NivelAcesso.PADRAO);
            usuarioRepository.save(u);
        }

        autenticarComo(EMAIL_USUARIO);
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private void autenticarComo(String email) {
        var auth = new UsernamePasswordAuthenticationToken(email, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private MultipartFile fotoValida(String nome) {
        return new MockMultipartFile("foto", nome, "image/jpeg", new byte[]{1, 2, 3});
    }

    private CheckinDTO dtoComUmaFoto() {
        CheckinDTO dto = new CheckinDTO();
        dto.setPostoId(posto.getId());
        dto.setFoto(fotoValida("foto1.jpg"));
        return dto;
    }

    // ------------------------------------------------------------------
    // Testes
    // ------------------------------------------------------------------

    @Test
    @DisplayName("Checkin com foto única retorna DTO preenchido corretamente")
    void checkinComFotoUnica_retornaResponsePreenchido() {
        CheckinResponseDTO response = checkService.checkin(dtoComUmaFoto());

        assertThat(response).isNotNull();
        assertThat(response.getPosto()).isEqualTo("Posto Central");
        assertThat(response.getHorario()).isNotNull();
        assertThat(response.getStatus()).isIn("VERDE", "AMARELO");
        assertThat(response.getFotos()).hasSize(1);
    }

    @Test
    @DisplayName("Checkin com array de 3 fotos retorna lista com 3 IDs")
    void checkinComArrayDeFotos_retornaTresFotos() {
        CheckinDTO dto = new CheckinDTO();
        dto.setPostoId(posto.getId());
        dto.setFotos(new MultipartFile[]{
                fotoValida("a.jpg"),
                fotoValida("b.jpg"),
                fotoValida("c.jpg")
        });

        CheckinResponseDTO response = checkService.checkin(dto);

        assertThat(response.getFotos()).hasSize(3);
    }

    @Test
    @DisplayName("Array de fotos tem precedência sobre foto individual")
    void arrayDeFotos_temPrecedenciaSobreFotoIndividual() {
        CheckinDTO dto = new CheckinDTO();
        dto.setPostoId(posto.getId());
        dto.setFoto(fotoValida("ignorada.jpg"));
        dto.setFotos(new MultipartFile[]{
                fotoValida("p1.jpg"),
                fotoValida("p2.jpg")
        });

        CheckinResponseDTO response = checkService.checkin(dto);

        assertThat(response.getFotos()).hasSize(2);
    }

    @Test
    @DisplayName("Status VERDE quando horário é antes das 08:00")
    void statusVerde_quandoAntesDasOito() throws Exception {
        var method = CheckService.class.getDeclaredMethod("statusCheckin", LocalDateTime.class);
        method.setAccessible(true);

        String status = (String) method.invoke(checkService, LocalDateTime.of(2024, 1, 1, 7, 59));

        assertThat(status).isEqualTo("VERDE");
    }

    @Test
    @DisplayName("Status AMARELO quando horário é depois das 08:00")
    void statusAmarelo_quandoDepoisDasOito() throws Exception {
        var method = CheckService.class.getDeclaredMethod("statusCheckin", LocalDateTime.class);
        method.setAccessible(true);

        String status = (String) method.invoke(checkService, LocalDateTime.of(2024, 1, 1, 9, 0));

        assertThat(status).isEqualTo("AMARELO");
    }

    @Test
    @DisplayName("Status VERDE para horário exatamente às 08:00 (não é depois)")
    void statusVerde_quandoExatamenteOito() throws Exception {
        var method = CheckService.class.getDeclaredMethod("statusCheckin", LocalDateTime.class);
        method.setAccessible(true);

        String status = (String) method.invoke(checkService, LocalDateTime.of(2024, 1, 1, 8, 0));

        assertThat(status).isEqualTo("VERDE");
    }

    @Test
    @DisplayName("Lança IllegalArgumentException quando nenhuma foto enviada")
    void semFotos_lancaExcecao() {
        CheckinDTO dto = new CheckinDTO();
        dto.setPostoId(posto.getId());

        assertThatThrownBy(() -> checkService.checkin(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Envie de 1 até 3 fotos.");
    }

    @Test
    @DisplayName("Lança IllegalArgumentException quando mais de 3 fotos enviadas")
    void maisDeTreesFotos_lancaExcecao() {
        CheckinDTO dto = new CheckinDTO();
        dto.setPostoId(posto.getId());
        dto.setFotos(new MultipartFile[]{
                fotoValida("1.jpg"), fotoValida("2.jpg"),
                fotoValida("3.jpg"), fotoValida("4.jpg")
        });

        assertThatThrownBy(() -> checkService.checkin(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Envie de 1 até 3 fotos.");
    }

    @Test
    @DisplayName("Fotos vazias são filtradas e lança exceção")
    void fotosVazias_saoFiltradas_lancaExcecao() {
        MockMultipartFile vazia = new MockMultipartFile("foto", "vazia.jpg", "image/jpeg", new byte[0]);
        CheckinDTO dto = new CheckinDTO();
        dto.setPostoId(posto.getId());
        dto.setFotos(new MultipartFile[]{vazia});

        assertThatThrownBy(() -> checkService.checkin(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Envie de 1 até 3 fotos.");
    }

    @Test
    @DisplayName("Lança exceção quando posto não existe")
    void postoInexistente_lancaExcecao() {
        CheckinDTO dto = new CheckinDTO();
        dto.setPostoId(999999L);
        dto.setFoto(fotoValida("foto.jpg"));

        assertThatThrownBy(() -> checkService.checkin(dto))
                .isInstanceOf(java.util.NoSuchElementException.class);
    }

    @Test
    @DisplayName("Lança exceção quando usuário autenticado não existe na base")
    void usuarioInexistente_lancaExcecao() {
        autenticarComo("naoexiste@teste.com");

        assertThatThrownBy(() -> checkService.checkin(dtoComUmaFoto()))
                .isInstanceOf(java.util.NoSuchElementException.class);
    }

    @Test
    @DisplayName("IDs das fotos retornadas são distintos")
    void fotos_possuemIdsDistintos() {
        CheckinDTO dto = new CheckinDTO();
        dto.setPostoId(posto.getId());
        dto.setFotos(new MultipartFile[]{
                fotoValida("x.jpg"),
                fotoValida("y.jpg")
        });

        CheckinResponseDTO response = checkService.checkin(dto);

        assertThat(response.getFotos()).doesNotHaveDuplicates();
    }
}
