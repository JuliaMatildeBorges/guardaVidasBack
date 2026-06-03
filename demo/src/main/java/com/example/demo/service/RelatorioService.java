package com.example.demo.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.entity.Checkout;
import com.example.demo.repository.CheckoutRespository;

@Service
public class RelatorioService {

    @Autowired
    private CheckoutRespository checkoutRespository;

    /**
     * Gera um relatório em formato Excel (XLSX) contendo as prevenções de checkouts
     * divididas por posto e período.
     */
    public byte[] gerarRelatorioExcel(LocalDate dataInicio, LocalDate dataFim) throws IOException {
        LocalDateTime inicio = dataInicio != null ? dataInicio.atStartOfDay() : LocalDateTime.of(1970, 1, 1, 0, 0);
        LocalDateTime fim = dataFim != null ? dataFim.atTime(LocalTime.MAX) : LocalDateTime.now();

        List<Checkout> checkouts = checkoutRespository.findByCreatedAtBetween(inicio, fim);

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            
            // Estilos para o documento
            CellStyle headerStyle = criarEstiloCabecalho(workbook);
            CellStyle dataStyle = criarEstiloDados(workbook);
            CellStyle totalStyle = criarEstiloTotal(workbook);

            // ==========================================
            // ABA 1: Prevenções Detalhadas
            // ==========================================
            Sheet sheetDetalhado = workbook.createSheet("Prevenções Detalhadas");
            sheetDetalhado.setFitToPage(true);

            String[] cabecalhosDetalhados = {
                "Posto", "Data", "Horário", "Salva-vidas", 
                "Prev. Manhã", "Prev. Tarde", "Total Prev.", 
                "Água-Viva Manhã", "Água-Viva Tarde", "Total Água-Viva"
            };

            // Criar linha de cabeçalho
            Row headerRowDet = sheetDetalhado.createRow(0);
            for (int i = 0; i < cabecalhosDetalhados.length; i++) {
                Cell cell = headerRowDet.createCell(i);
                cell.setCellValue(cabecalhosDetalhados[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            int totalPrevManha = 0;
            int totalPrevTarde = 0;
            int totalLesoesManha = 0;
            int totalLesoesTarde = 0;

            DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm");

            for (Checkout c : checkouts) {
                Row row = sheetDetalhado.createRow(rowIdx++);

                // Posto
                Cell c0 = row.createCell(0);
                c0.setCellValue(c.getPosto() != null ? c.getPosto().getNome() : "N/D");
                c0.setCellStyle(dataStyle);

                // Data
                Cell c1 = row.createCell(1);
                c1.setCellValue(c.getCreatedAt() != null ? c.getCreatedAt().format(dateFormat) : "-");
                c1.setCellStyle(dataStyle);

                // Horário
                Cell c2 = row.createCell(2);
                c2.setCellValue(c.getCreatedAt() != null ? c.getCreatedAt().format(timeFormat) : "-");
                c2.setCellStyle(dataStyle);

                // Salva-vidas
                Cell c3 = row.createCell(3);
                c3.setCellValue(c.getUsuario() != null ? c.getUsuario().getEmail() : "N/D");
                c3.setCellStyle(dataStyle);

                // Prevenções Manhã
                Cell c4 = row.createCell(4);
                int pm = c.getPrevencoesManha() != null ? c.getPrevencoesManha() : 0;
                c4.setCellValue(pm);
                c4.setCellStyle(dataStyle);
                totalPrevManha += pm;

                // Prevenções Tarde
                Cell c5 = row.createCell(5);
                int pt = c.getPrevencoesTarde() != null ? c.getPrevencoesTarde() : 0;
                c5.setCellValue(pt);
                c5.setCellStyle(dataStyle);
                totalPrevTarde += pt;

                // Total Prevenções
                Cell c6 = row.createCell(6);
                c6.setCellValue(pm + pt);
                c6.setCellStyle(dataStyle);

                // Água-Viva Manhã
                Cell c7 = row.createCell(7);
                int am = c.getLesoesAguaVivaManha() != null ? c.getLesoesAguaVivaManha() : 0;
                c7.setCellValue(am);
                c7.setCellStyle(dataStyle);
                totalLesoesManha += am;

                // Água-Viva Tarde
                Cell c8 = row.createCell(8);
                int at = c.getLesoesAguaVivaTarde() != null ? c.getLesoesAguaVivaTarde() : 0;
                c8.setCellValue(at);
                c8.setCellStyle(dataStyle);
                totalLesoesTarde += at;

                // Total Água-Viva
                Cell c9 = row.createCell(9);
                c9.setCellValue(am + at);
                c9.setCellStyle(dataStyle);
            }

            // Linha de Totais da primeira aba
            Row totalRowDet = sheetDetalhado.createRow(rowIdx);
            Cell cellTotalLabel = totalRowDet.createCell(0);
            cellTotalLabel.setCellValue("TOTAL GERAL");
            cellTotalLabel.setCellStyle(totalStyle);
            
            // Preencher células em branco com estilo de total
            for (int i = 1; i <= 3; i++) {
                Cell cellBlank = totalRowDet.createCell(i);
                cellBlank.setCellValue("");
                cellBlank.setCellStyle(totalStyle);
            }

            Cell cellTPM = totalRowDet.createCell(4);
            cellTPM.setCellValue(totalPrevManha);
            cellTPM.setCellStyle(totalStyle);

            Cell cellTPT = totalRowDet.createCell(5);
            cellTPT.setCellValue(totalPrevTarde);
            cellTPT.setCellStyle(totalStyle);

            Cell cellTPTotal = totalRowDet.createCell(6);
            cellTPTotal.setCellValue(totalPrevManha + totalPrevTarde);
            cellTPTotal.setCellStyle(totalStyle);

            Cell cellLAM = totalRowDet.createCell(7);
            cellLAM.setCellValue(totalLesoesManha);
            cellLAM.setCellStyle(totalStyle);

            Cell cellLAT = totalRowDet.createCell(8);
            cellLAT.setCellValue(totalLesoesTarde);
            cellLAT.setCellStyle(totalStyle);

            Cell cellLATotal = totalRowDet.createCell(9);
            cellLATotal.setCellValue(totalLesoesManha + totalLesoesTarde);
            cellLATotal.setCellStyle(totalStyle);

            // Autoajustar colunas da primeira aba
            for (int i = 0; i < cabecalhosDetalhados.length; i++) {
                sheetDetalhado.autoSizeColumn(i);
            }

            // ==========================================
            // ABA 2: Resumo por Posto
            // ==========================================
            Sheet sheetResumo = workbook.createSheet("Resumo por Posto");
            
            String[] cabecalhosResumo = {
                "Nome do Posto", "Total de Prevenções", "Total Lesões Água-Viva", "Total de Checkouts"
            };

            Row headerRowRes = sheetResumo.createRow(0);
            for (int i = 0; i < cabecalhosResumo.length; i++) {
                Cell cell = headerRowRes.createCell(i);
                cell.setCellValue(cabecalhosResumo[i]);
                cell.setCellStyle(headerStyle);
            }

            // Agrupando checkouts por posto
            Map<String, List<Checkout>> checkoutsPorPosto = checkouts.stream()
                .filter(c -> c.getPosto() != null)
                .collect(Collectors.groupingBy(c -> c.getPosto().getNome()));

            int rowResIdx = 1;
            int totalGeralPrev = 0;
            int totalGeralLesoes = 0;
            int totalGeralChecks = 0;

            for (Map.Entry<String, List<Checkout>> entry : checkoutsPorPosto.entrySet()) {
                Row row = sheetResumo.createRow(rowResIdx++);
                String postoNome = entry.getKey();
                List<Checkout> list = entry.getValue();

                int somaPrev = list.stream().mapToInt(c -> 
                    (c.getPrevencoesManha() != null ? c.getPrevencoesManha() : 0) + 
                    (c.getPrevencoesTarde() != null ? c.getPrevencoesTarde() : 0)
                ).sum();

                int somaLesoes = list.stream().mapToInt(c -> 
                    (c.getLesoesAguaVivaManha() != null ? c.getLesoesAguaVivaManha() : 0) + 
                    (c.getLesoesAguaVivaTarde() != null ? c.getLesoesAguaVivaTarde() : 0)
                ).sum();

                int qtdChecks = list.size();

                Cell r0 = row.createCell(0);
                r0.setCellValue(postoNome);
                r0.setCellStyle(dataStyle);

                Cell r1 = row.createCell(1);
                r1.setCellValue(somaPrev);
                r1.setCellStyle(dataStyle);
                totalGeralPrev += somaPrev;

                Cell r2 = row.createCell(2);
                r2.setCellValue(somaLesoes);
                r2.setCellStyle(dataStyle);
                totalGeralLesoes += somaLesoes;

                Cell r3 = row.createCell(3);
                r3.setCellValue(qtdChecks);
                r3.setCellStyle(dataStyle);
                totalGeralChecks += qtdChecks;
            }

            // Linha de Totais da segunda aba
            Row totalRowRes = sheetResumo.createRow(rowResIdx);
            Cell cellTotalResLabel = totalRowRes.createCell(0);
            cellTotalResLabel.setCellValue("TOTAL GERAL");
            cellTotalResLabel.setCellStyle(totalStyle);

            Cell cellTRPrev = totalRowRes.createCell(1);
            cellTRPrev.setCellValue(totalGeralPrev);
            cellTRPrev.setCellStyle(totalStyle);

            Cell cellTRLesoes = totalRowRes.createCell(2);
            cellTRLesoes.setCellValue(totalGeralLesoes);
            cellTRLesoes.setCellStyle(totalStyle);

            Cell cellTRChecks = totalRowRes.createCell(3);
            cellTRChecks.setCellValue(totalGeralChecks);
            cellTRChecks.setCellStyle(totalStyle);

            // Autoajustar colunas da segunda aba
            for (int i = 0; i < cabecalhosResumo.length; i++) {
                sheetResumo.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

    private CellStyle criarEstiloCabecalho(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        // Cor de fundo do cabeçalho: Vermelho Escuro CBMSC (#C41E2A)
        style.setFillForegroundColor(IndexedColors.DARK_RED.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        // Alinhamento centralizado
        style.setAlignment(HorizontalAlignment.CENTER);
        
        // Bordas finas
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        
        // Fonte em negrito branca
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        font.setFontHeightInPoints((short) 11);
        font.setFontName("Calibri");
        style.setFont(font);
        
        return style;
    }

    private CellStyle criarEstiloDados(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        
        // Alinhamento à esquerda
        style.setAlignment(HorizontalAlignment.LEFT);
        
        // Bordas finas cinza claro
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBottomBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setTopBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setLeftBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setRightBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 10);
        font.setFontName("Calibri");
        style.setFont(font);
        
        return style;
    }

    private CellStyle criarEstiloTotal(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        // Cor de fundo cinza claro
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        style.setBorderBottom(BorderStyle.DOUBLE); // Linha dupla inferior para totais
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 10);
        font.setFontName("Calibri");
        style.setFont(font);
        
        return style;
    }
}
