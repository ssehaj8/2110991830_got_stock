package com.cg.gotstock.service;

import com.cg.gotstock.dto.StockHoldingDTO;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Table;
import com.lowagie.text.pdf.PdfWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
@Slf4j
public class PdfGenerator {

    public byte[] generatePDF(List<StockHoldingDTO> holdings, String username) throws DocumentException {
        Document document = new Document();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, byteArrayOutputStream);
            document.open();

            document.add(new Paragraph("Stock holdings Reports for " + username));
            log.info("Added report title to PDF for user: {}", username);

            Table table = new Table(4);
            table.addCell("Symbol");
            table.addCell("Quantity");
            table.addCell("Purchase Price");
            table.addCell("Current Price");

            for (StockHoldingDTO holding : holdings) {
                table.addCell(holding.getSymbol());
                table.addCell(String.valueOf(holding.getQuantity()));
                table.addCell(String.valueOf(holding.getPurchasePrice()));
                table.addCell(String.valueOf(holding.getCurrentPrice()));
            }
            document.add(table);
            document.close();
            log.info("PDF generation completed successfully for user: {}", username);

        } catch (
                DocumentException e) {
            log.error("Error occurred while generating PDF for user {}: {}", username, e.getMessage(), e);
            e.printStackTrace();
        }
        return byteArrayOutputStream.toByteArray();
    }
}