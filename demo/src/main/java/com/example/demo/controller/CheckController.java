package com.example.demo.controller;

import org.hibernate.annotations.DialectOverride.Check;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.CheckinDTO;

@RestController
@RequestMapping("/check")
public class CheckController {

    @PostMapping("/in")
     public CheckinDTO checkin(@ModelAttribute @Valid @CheckinDTO dto){



     }



    }
