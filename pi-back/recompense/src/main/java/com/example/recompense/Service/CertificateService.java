package com.example.recompense.Service;

import com.example.recompense.DTO.RewardEvaluationSyncRequest;
import com.example.recompense.Entity.RewardHistory;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
public class CertificateService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public byte[] generateCertificate(RewardEvaluationSyncRequest request, String badgeName) {
        return generateCertificate(
                request.getFreelancerName(),
                request.getFreelancerEmail(),
                badgeName,
                request.getEvaluatedAt(),
                request.getAverageScore(),
                request.getTotalPoints(),
                request.getCurrentScore()
        );
    }

    public byte[] generateCertificate(RewardHistory history) {
        return generateCertificate(
                history.getUserName(),
                history.getUserEmail(),
                history.getRewardName(),
                history.getEventDate(),
                history.getAverageScoreSnapshot(),
                history.getTotalPointsSnapshot(),
                history.getScoreSnapshot()
        );
    }

    private byte[] generateCertificate(String userName,
                                       String userEmail,
                                       String badgeName,
                                       LocalDateTime issuedAt,
                                       Double averageScore,
                                       Integer totalPoints,
                                       Integer currentScore) {

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4.rotate(), 48, 48, 48, 48);

        try {
            PdfWriter.getInstance(document, output);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 28);
            Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 18);
            Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 14);
            Font strongFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);

            Paragraph title = new Paragraph("Certificate of Excellence", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(24f);
            document.add(title);

            Paragraph subtitle = new Paragraph(
                    "This certificate recognizes outstanding freelance performance.",
                    subtitleFont
            );
            subtitle.setAlignment(Element.ALIGN_CENTER);
            subtitle.setSpacingAfter(28f);
            document.add(subtitle);

            Paragraph awardedTo = new Paragraph(
                    "Awarded to: " + safe(userName, userEmail),
                    strongFont
            );
            awardedTo.setAlignment(Element.ALIGN_CENTER);
            awardedTo.setSpacingAfter(20f);
            document.add(awardedTo);

            document.add(centered("Badge: " + safe(badgeName, "N/A"), strongFont, 14f));
            document.add(centered("Issued at: " + formatDate(issuedAt), bodyFont, 8f));
            document.add(centered("Average score: " + formatDecimal(averageScore), bodyFont, 6f));
            document.add(centered("Current evaluation score: " + formatInteger(currentScore), bodyFont, 6f));
            document.add(centered("Total reward points: " + formatInteger(totalPoints), bodyFont, 16f));

            Paragraph footer = new Paragraph(
                    "Freelink rewards engine generated this certificate automatically.",
                    bodyFont
            );
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to generate certificate PDF", ex);
        } finally {
            document.close();
        }

        return output.toByteArray();
    }

    private Paragraph centered(String text, Font font, float spacingAfter) {
        Paragraph paragraph = new Paragraph(text, font);
        paragraph.setAlignment(Element.ALIGN_CENTER);
        paragraph.setSpacingAfter(spacingAfter);
        return paragraph;
    }

    private String safe(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private String formatDate(LocalDateTime value) {
        return value == null ? "N/A" : DATE_FORMATTER.format(value);
    }

    private String formatDecimal(Double value) {
        return value == null ? "0.00" : String.format(Locale.US, "%.2f / 5.00", value);
    }

    private String formatInteger(Integer value) {
        return value == null ? "0" : String.valueOf(value);
    }
}
