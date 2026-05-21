package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.dto.CheckoutDTO;
import com.example.demo.dto.CheckoutResponseDTO;
import com.example.demo.entity.Arquivo;
import com.example.demo.entity.Checkout;
import com.example.demo.entity.Posto;
import com.example.demo.repository.CheckoutRespository;
import com.example.demo.repository.PostoRepository;

@Service
public class CheckoutService {

    @Autowired
    private PostoRepository postoRepository;

    @Autowired
    private ArquivoService arquivoService;

    @Autowired
    private CheckoutRespository checkoutRespository;

    public CheckoutResponseDTO checkout(CheckoutDTO dto){
        
        Posto posto = postoRepository.findById(dto.getPostoId()).orElseThrow();

        Checkout checkout = new Checkout();
        checkout.setPosto(posto);

        Arquivo arquivo = arquivoService.upload(dto.getFoto());

        checkout.setFoto(arquivo);

        Checkout checkoutSalvo = checkoutRespository.save(checkout);
        CheckoutResponseDTO crd = new CheckoutResponseDTO();

        crd.setPosto(posto.getNome());
        crd.setHorario(checkoutSalvo.getCreatedAt());

        return crd;

    }

}
