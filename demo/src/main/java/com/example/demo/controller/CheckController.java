package com.example.demo.controller;


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.annotations.Admin;
import com.example.demo.dto.CheckinDTO;
import com.example.demo.dto.CheckinResponseDTO;
import com.example.demo.dto.CheckoutDTO;
import com.example.demo.dto.CheckoutResponseDTO;
import com.example.demo.dto.PostoCheckResumoDTO;
import com.example.demo.service.CheckResumoService;
import com.example.demo.service.CheckService;
import com.example.demo.service.CheckoutService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/check")
public class CheckController {

    @Autowired
    private CheckService checkService;

    @Autowired
    private CheckoutService checkoutService;

    @Autowired
    private CheckResumoService checkResumoService;

    @PostMapping("/in")
    public CheckinResponseDTO checkin(@ModelAttribute @Valid CheckinDTO dto) {
        return checkService.checkin(dto);
    }

    @PostMapping("/out")
    public CheckoutResponseDTO checkout(@ModelAttribute @Valid CheckoutDTO dto) {
        return checkoutService.checkout(dto);
    }

    @GetMapping("/status-hoje")
    @Admin
    public List<PostoCheckResumoDTO> statusHoje() {
        return checkResumoService.statusHoje();
    }

}
