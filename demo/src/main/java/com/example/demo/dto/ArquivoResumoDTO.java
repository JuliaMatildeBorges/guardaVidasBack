package com.example.demo.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArquivoResumoDTO {

    private UUID id;

    private String nome;

    private String url;
}
