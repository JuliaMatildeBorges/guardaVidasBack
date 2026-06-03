package com.example.demo.dto;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutDTO {

    @NotNull(message = "o ID do posto é obrigatorio")
    private Long postoId;

    private MultipartFile foto;

    private MultipartFile[] fotos;

    private String timestampCaptura;

    @NotNull(message = "Informe as prevenções da manhã.")
    @Min(value = 0, message = "O valor não pode ser negativo.")
    private Integer prevencoesManha;

    @NotNull(message = "Informe as prevenções da tarde.")
    @Min(value = 0, message = "O valor não pode ser negativo.")
    private Integer prevencoesTarde;

    @NotNull(message = "Informe as lesões por água-viva da manhã.")
    @Min(value = 0, message = "O valor não pode ser negativo.")
    private Integer lesoesAguaVivaManha;

    @NotNull(message = "Informe as lesões por água-viva da tarde.")
    @Min(value = 0, message = "O valor não pode ser negativo.")
    private Integer lesoesAguaVivaTarde;

}
