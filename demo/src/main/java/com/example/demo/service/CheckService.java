package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.dto.CheckinDTO;
import com.example.demo.entity.Checkin;
import com.example.demo.entity.Posto;
import com.example.demo.repository.PostoRepository;

@Service
public class CheckService {

    @Autowired
    private PostoRepository postoRepository;

    public CheckinDTO checkin(CheckinDTO dto){
        
        Posto posto = postoRepository.findById(dto.getPostoId()).orElseThrow();

        Checkin checkin = new Checkin();
        checkin.setPosto(posto);

    }

}
