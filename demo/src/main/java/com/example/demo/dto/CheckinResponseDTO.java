package com.example.demo.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import lombok.Data;

@Data
public class CheckinResponseDTO {

    private String posto;

    private LocalDateTime horario;

    private String status;

    private List<UUID> fotos;
}
