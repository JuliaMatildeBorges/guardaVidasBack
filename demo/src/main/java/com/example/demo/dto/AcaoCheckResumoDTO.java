package com.example.demo.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class AcaoCheckResumoDTO {

    private String status = "VERMELHO";

    private LocalDateTime horario;

    private String usuario;

    private List<ArquivoResumoDTO> fotos = new ArrayList<>();

    private Integer prevencoesManha;

    private Integer prevencoesTarde;

    private Integer lesoesAguaVivaManha;

    private Integer lesoesAguaVivaTarde;
}
