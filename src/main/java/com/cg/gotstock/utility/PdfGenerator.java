package com.cg.gotstock.utility;

import com.cg.gotstock.dto.StockHoldingDTO;
import com.cg.gotstock.model.User;
import com.cg.gotstock.repository.UserRepository;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Table;
import com.lowagie.text.pdf.PdfWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Slf4j
public class PdfGenerator {

    @Autowired
    private UserRepository userRepository;

    /**
     * Generates a PDF report of stock holdings for a specific user.
     *
     * @param holdings The list of stock holdings to be included in the report.
     * @param value    The total portfolio value to be displayed in the summary.
     * @param username The username of the user requesting the report.
     * @return A byte array representing the generated PDF document.
     * @throws DocumentException If there is an issue during PDF creation.
     */
    public byte[] generatePDF(List<StockHoldingDTO> holdings, Float value, String username) throws DocumentException {
        // Create a new document for PDF generation
        Document document = new Document();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        // Get the current authenticated user's email
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName(); // Get user email from security context
        User user = userRepository.findByEmail(email); // Retrieve user details by email

        // Format the current date and time for report timestamp
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm:ss");
        String formattedDateTime = now.format(formatter); // Format current date and time

        try {
            // Initialize the PDF writer and open the document for writing
            PdfWriter.getInstance(document, byteArrayOutputStream);
            document.open();

            // Add report title with user's full name
            document.add(new Paragraph("Stock holdings Reports for " + user.getFirstname() + " " + user.getLastname()));
            document.add(new Paragraph("Last updated on: " + formattedDateTime));
            log.info("Added report title to PDF for user: {}", username);

            // Create and populate the stock holdings table
            Table table = new Table(5); // 5 columns for stock information
            table.addCell("Symbol");
            table.addCell("Quantity");
            table.addCell("Purchase Price");
            table.addCell("Current Price");
            table.addCell("Gain/Loss");

            // Populate table with stock holdings data
            for (StockHoldingDTO holding : holdings) {
                table.addCell(holding.getSymbol());
                table.addCell(String.valueOf(holding.getQuantity()));
                table.addCell(String.valueOf(holding.getPurchasePrice()));
                table.addCell(String.valueOf(holding.getCurrentPrice()));
                String formattedGainLoss = String.format("%.2f", holding.getGainLoss()); // Format gain/loss to 2 decimal places
                table.addCell(formattedGainLoss);
            }
            document.add(table); // Add the stock holdings table to the document

            // Add portfolio summary table with total portfolio value
            Table summaryTable = new Table(2); // 2 columns for total value
            summaryTable.addCell("Total Portfolio Value");
            summaryTable.addCell(String.valueOf(value));

            document.add(summaryTable); // Add the portfolio summary table

            // Close the document after writing
            document.close();
            log.info("PDF generation completed successfully for user: {}", username);

        } catch (DocumentException e) {
            // Log error if PDF generation fails
            log.error("Error occurred while generating PDF for user {}: {}", username, e.getMessage(), e);
            e.printStackTrace();
        }

        // Return the byte array representing the generated PDF
        return byteArrayOutputStream.toByteArray();
    }
}
