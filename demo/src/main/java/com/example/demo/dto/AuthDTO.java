package com.example.demo.dto;


import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthDTO {

    @NotBlank(message = "O CPF deve ser preenchido.")
    private String cpf;

    @NotBlank(message = "A senha deve ser preenchido.")
    private String senha;

}
