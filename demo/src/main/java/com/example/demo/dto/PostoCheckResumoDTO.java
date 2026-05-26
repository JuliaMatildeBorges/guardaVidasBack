package com.example.demo.dto;

import lombok.Data;

@Data
public class PostoCheckResumoDTO {

    private Long postoId;

    private String posto;

    private AcaoCheckResumoDTO checkin = new AcaoCheckResumoDTO();

    private AcaoCheckResumoDTO checkout = new AcaoCheckResumoDTO();
}
