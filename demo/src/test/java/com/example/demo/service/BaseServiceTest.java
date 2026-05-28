package com.example.demo.service;

import com.example.demo.dto.PostoDTO;
import com.example.demo.entity.Posto;
import com.example.demo.repository.PostoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BaseServiceTest {

    private PostoRepository repository;
    private PostoService service;

    @BeforeEach
    void setUp() {
        repository = mock(PostoRepository.class);
        service = new PostoService(repository);
    }

    @Test
    void createConverteDtoSalvaEDevolveDto() {
        when(repository.save(any(Posto.class))).thenAnswer(invocation -> {
            Posto posto = invocation.getArgument(0);
            posto.setId(10L);
            return posto;
        });

        PostoDTO dto = new PostoDTO(null, "Central", "Descricao");

        PostoDTO salvo = service.create(dto);

        assertThat(salvo.getId()).isEqualTo(10L);
        assertThat(salvo.getNome()).isEqualTo("Central");
        assertThat(salvo.getDescricao()).isEqualTo("Descricao");
    }

    @Test
    void updateMantemIdDaRota() {
        when(repository.save(any(Posto.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PostoDTO atualizado = service.update(7L, new PostoDTO(null, "Novo", "Desc"));

        assertThat(atualizado.getId()).isEqualTo(7L);
        assertThat(atualizado.getNome()).isEqualTo("Novo");
    }

    @Test
    void readPorIdConverteEntityParaDto() {
        Posto posto = new Posto();
        posto.setId(4L);
        posto.setNome("Norte");
        posto.setDescricao("Aberto");
        when(repository.findById(4L)).thenReturn(Optional.of(posto));

        PostoDTO dto = service.read(4L);

        assertThat(dto.getId()).isEqualTo(4L);
        assertThat(dto.getNome()).isEqualTo("Norte");
    }

    @Test
    void readListaSomenteRegistrosAtivosDoRepositorio() {
        Posto posto = new Posto();
        posto.setId(1L);
        posto.setNome("Sul");
        when(repository.findAll()).thenReturn(List.of(posto));

        assertThat(service.read()).extracting(PostoDTO::getNome).containsExactly("Sul");
    }

    @Test
    void deleteEsoftDeleteDelegamAoRepositorio() {
        service.delete(5L);
        service.softDelete(6L);

        verify(repository).deleteById(5L);
        verify(repository).softDeleteById(6L);
    }
}
