package com.example.demo.controller;


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;

import com.example.demo.annotations.Admin;
import com.example.demo.dto.CheckinDTO;
import com.example.demo.dto.CheckinResponseDTO;
import com.example.demo.dto.CheckoutDTO;
import com.example.demo.dto.CheckoutResponseDTO;
import com.example.demo.dto.PostoCheckResumoDTO;
import com.example.demo.service.CheckResumoService;
import com.example.demo.service.CheckService;
import com.example.demo.service.CheckoutService;
import com.example.demo.service.RelatorioService;

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

    @Autowired
    private RelatorioService relatorioService;

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

    @GetMapping("/meus-checks-hoje")
    public List<PostoCheckResumoDTO> meusChecksHoje() {
        return checkResumoService.meusChecksHoje();
    }

    /**
     * Limpa todos os check-ins, check-outs e fotos físicas/lógicas registradas no aplicativo.
     * Restrito a Administradores.
     */
    @DeleteMapping("/limpar-dados")
    @Admin
    public void limparDados() {
        checkService.limparTodosOsDados();
    }

    /**
     * Gera e envia o relatório consolidado de prevenções em formato XLS/XLSX.
     * Restrito a Administradores.
     */
    @GetMapping("/relatorio")
    @Admin
    public void baixarRelatorio(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            jakarta.servlet.http.HttpServletResponse response) throws java.io.IOException {
        
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=relatorio_prevencoes.xlsx");
        
        byte[] relatorioBytes = relatorioService.gerarRelatorioExcel(inicio, fim);
        response.getOutputStream().write(relatorioBytes);
        response.getOutputStream().flush();
    }

}
