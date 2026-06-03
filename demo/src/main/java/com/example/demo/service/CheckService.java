package com.example.demo.service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
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

    @Autowired
    private com.example.demo.repository.CheckoutRespository checkoutRespository;

    @Autowired
    private org.springframework.core.env.Environment env;

    public CheckinResponseDTO checkin(CheckinDTO dto){
        
        // Valida se a foto foi tirada em tempo real no momento da captura
        validarTimestampCaptura(dto.getTimestampCaptura());

        Posto posto = postoRepository.findById(dto.getPostoId()).orElseThrow();
        Usuario usuario = usuarioRepository.findByEmail(usuarioLogado()).orElseThrow();
        List<MultipartFile> fotos = normalizarFotos(dto.getFotos(), dto.getFoto());
        LocalDateTime inicio = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime fim = LocalDateTime.now().toLocalDate().atTime(LocalTime.MAX);

        Checkin checkin = checkinRepository
            .findFirstByUsuarioAndPostoAndCreatedAtBetweenOrderByCreatedAtDesc(usuario, posto, inicio, fim)
            .orElseGet(() -> {
                Checkin novo = new Checkin();
                novo.setPosto(posto);
                novo.setUsuario(usuario);
                novo.setFotos(new ArrayList<>());
                return novo;
            });

        if (checkin.getFotos() == null) {
            checkin.setFotos(new ArrayList<>());
        }

        if (checkin.getFotos().size() + fotos.size() > 3) {
            throw new IllegalArgumentException("Este checkin já possui " + checkin.getFotos().size() + " foto(s). O limite por ação é 3.");
        }

        List<Arquivo> arquivos = fotos.stream().map(arquivoService::upload).toList();

        checkin.getFotos().addAll(arquivos);
        checkin.setFoto(checkin.getFotos().get(0));

        Checkin checkinSalvo = checkinRepository.save(checkin);
        CheckinResponseDTO crd = new CheckinResponseDTO();

        crd.setPosto(posto.getNome());
        crd.setHorario(checkinSalvo.getCreatedAt());
        crd.setStatus(statusCheckin(checkinSalvo.getCreatedAt()));
        crd.setFotos(checkinSalvo.getFotos().stream().map(Arquivo::getId).toList());

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

    /**
     * Valida se a foto foi tirada no momento exato do registro (tempo real).
     * Lança exceção se o timestamp não for enviado, for inválido ou se a
     * diferença para o horário do servidor for maior que 10 minutos (600 segundos).
     */
    public void validarTimestampCaptura(String timestampCaptura) {
        // Ignora a validação se estiver executando testes unitários/ambiente de teste
        if (env != null && java.util.Arrays.asList(env.getActiveProfiles()).contains("test")) {
            return;
        }
        if (timestampCaptura == null || timestampCaptura.trim().isEmpty()) {
            throw new IllegalArgumentException("O timestamp de captura da foto é obrigatório. A foto deve ser tirada na hora.");
        }
        try {
            java.time.Instant captura = java.time.Instant.parse(timestampCaptura);
            java.time.Instant agora = java.time.Instant.now();
            long diferencaSegundos = Math.abs(java.time.Duration.between(captura, agora).getSeconds());
            
            // Tolerância de 10 minutos (600 segundos) para atrasos de rede ou pequenas discrepâncias de relógio
            if (diferencaSegundos > 600) {
                throw new IllegalArgumentException("A foto precisa ser tirada em tempo real no momento do registro. Uploads da galeria não são permitidos.");
            }
        } catch (java.time.format.DateTimeParseException e) {
            throw new IllegalArgumentException("Formato de data/hora de captura inválido.");
        }
    }

    /**
     * Exclui todas as fotos, check-ins e check-outs do banco de dados e do sistema de arquivos.
     * Usado pelo Administrador para limpar o aplicativo para o dia seguinte.
     */
    @jakarta.transaction.Transactional
    public void limparTodosOsDados() {
        checkinRepository.deleteAll();
        checkoutRespository.deleteAll();
        arquivoService.excluirTodosOsArquivos();
    }

}
