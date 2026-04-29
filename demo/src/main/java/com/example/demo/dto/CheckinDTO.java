package com.example.demo.dto;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckinDTO {

    @NotNull(message = "o ID do posto é obrigatorio")
    private Long postoId;

    private MultipartFile foto;

}
