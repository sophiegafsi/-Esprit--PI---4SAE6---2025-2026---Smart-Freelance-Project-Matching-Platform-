package tn.esprit.gestionskills.Services;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.gestionskills.Entities.Skill;
import tn.esprit.gestionskills.Entities.SkillProof;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SkillPdfService {

    private final SkillService skillsService;

    // 🎨 Couleurs proches de ton UI
    private static final Color BLUE = new Color(17, 74, 158);
    private static final Color BLUE_LIGHT = new Color(232, 242, 255);
    private static final Color GRAY_LIGHT = new Color(245, 247, 250);
    private static final Color GREEN = new Color(46, 125, 50);
    private static final Color ORANGE = new Color(255, 152, 0);

    public byte[] exportSkillPdf(Long skillId) {
        Skill s = skillsService.getSkillById(skillId);
        if (s == null) throw new IllegalArgumentException("Skill introuvable id=" + skillId);

        int score = skillsService.getScore(skillId);
        String badge = skillsService.getBadge(skillId);

        java.util.List<SkillProof> validProofs = (s.getProofs() == null) ? java.util.List.of()
                : s.getProofs().stream()
                .filter(this::isValidProof)
                .sorted(java.util.Comparator.comparing(SkillProof::getId))
                .toList();

        try (java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4, 36, 36, 28, 36);
            PdfWriter.getInstance(doc, baos);
            doc.open();

            addHeader(doc);                    // ✅ logo + header bleu
            addInfoCards(doc, s, score, badge); // ✅ cartes infos + score + badge
            addProofsSection(doc, validProofs); // ✅ tableau preuves

            doc.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Erreur génération PDF", e);
        }
    }

    private boolean isValidProof(SkillProof p) {
        return p.getExpiresAt() == null || !p.getExpiresAt().isBefore(LocalDate.now());
    }

    private void addInfoCards(Document doc, Skill s, int score, String badge) throws Exception {
        PdfPTable grid = new PdfPTable(new float[]{3.6f, 2.4f});
        grid.setWidthPercentage(100);

        PdfPCell leftCard = cardCell();
        leftCard.addElement(sectionTitle("Informations"));
        leftCard.addElement(kv("Nom", safe(s.getName())));
        leftCard.addElement(kv("Niveau", safe(s.getLevel())));
        leftCard.addElement(kv("Années d'expérience", String.valueOf(s.getYearsOfExperience() == null ? 0 : s.getYearsOfExperience())));
        leftCard.addElement(kv("Description", safe(s.getDescription())));

        PdfPCell rightCard = cardCell();
        rightCard.addElement(sectionTitle("Score & Badge"));

        Font scoreFont = new Font(Font.HELVETICA, 28, Font.BOLD, BLUE);
        Paragraph scoreP = new Paragraph(String.valueOf(score), scoreFont);
        scoreP.setSpacingBefore(6);
        scoreP.setSpacingAfter(10);
        scoreP.setAlignment(Element.ALIGN_CENTER);
        rightCard.addElement(scoreP);

        PdfPTable chipTable = new PdfPTable(1);
        chipTable.setWidthPercentage(100);
        chipTable.addCell(chip(badge, badgeColor(badge)));
        rightCard.addElement(chipTable);

        Font hint = new Font(Font.HELVETICA, 9, Font.ITALIC, new Color(90, 90, 90));
        Paragraph note = new Paragraph("Badge calculé automatiquement (preuves expirées ignorées).", hint);
        note.setSpacingBefore(10);
        note.setAlignment(Element.ALIGN_CENTER);
        rightCard.addElement(note);

        grid.addCell(leftCard);
        grid.addCell(rightCard);

        doc.add(grid);
        doc.add(spacer(16));
    }

    private void addProofsSection(Document doc, java.util.List<SkillProof> validProofs) throws Exception {
        PdfPTable band = new PdfPTable(1);
        band.setWidthPercentage(100);

        PdfPCell bc = new PdfPCell(new Phrase(
                "Preuves valides (non expirées)",
                new Font(Font.HELVETICA, 12, Font.BOLD, BLUE)
        ));
        bc.setBackgroundColor(BLUE_LIGHT);
        bc.setBorderColor(new Color(220, 230, 245));
        bc.setPadding(10);
        band.addCell(bc);

        doc.add(band);
        doc.add(spacer(10));

        if (validProofs == null || validProofs.isEmpty()) {
            Font normal = new Font(Font.HELVETICA, 10, Font.NORMAL, new Color(40, 40, 40));
            doc.add(new Paragraph("Aucune preuve valide.", normal));
            return;
        }

        PdfPTable table = new PdfPTable(new float[]{2.2f, 1.2f, 1.5f, 2.3f});
        table.setWidthPercentage(100);

        table.addCell(th("Titre"));
        table.addCell(th("Type"));
        table.addCell(th("Expire le"));
        table.addCell(th("Aperçu"));

        boolean alt = false;
        for (SkillProof p : validProofs) {
            Color row = alt ? GRAY_LIGHT : Color.WHITE;

            table.addCell(td(safe(p.getTitle()), row));
            table.addCell(td(safe(p.getType()), row));
            table.addCell(td(p.getExpiresAt() == null ? "-" : p.getExpiresAt().toString(), row));
            table.addCell(imageTd(p.getFileUrl(), row));

            alt = !alt;
        }

        doc.add(table);
    }

    private PdfPCell th(String txt) {
        Font f = new Font(Font.HELVETICA, 10, Font.BOLD, Color.WHITE);
        PdfPCell c = new PdfPCell(new Phrase(txt, f));
        c.setBackgroundColor(BLUE);
        c.setPadding(8);
        c.setBorderColor(new Color(220, 230, 245));
        return c;
    }

    private PdfPCell td(String txt, Color bg) {
        Font f = new Font(Font.HELVETICA, 10, Font.NORMAL, new Color(40, 40, 40));
        PdfPCell c = new PdfPCell(new Phrase(txt == null ? "" : txt, f));
        c.setBackgroundColor(bg);
        c.setPadding(8);
        c.setBorderColor(new Color(220, 230, 245));
        return c;
    }

    private PdfPCell imageTd(String fileUrl, Color bg) {
        PdfPCell imgCell = new PdfPCell();
        imgCell.setBackgroundColor(bg);
        imgCell.setPadding(8);
        imgCell.setBorderColor(new Color(220, 230, 245));

        Font f = new Font(Font.HELVETICA, 9, Font.ITALIC, new Color(100, 100, 100));

        Image img = loadImageFromUploads(fileUrl);
        if (img != null) {
            img.scaleToFit(160, 110);
            img.setAlignment(Image.MIDDLE);
            imgCell.addElement(img);
        } else {
            imgCell.addElement(new Paragraph("(image non disponible)", f));
        }
        return imgCell;
    }

    private Image loadImageFromUploads(String fileUrl) {
        try {
            if (fileUrl == null || fileUrl.isBlank()) return null;

            // fileUrl: "/uploads/xxx.jpg" -> path: "uploads/xxx.jpg"
            String relative = fileUrl.startsWith("/uploads/") ? fileUrl.substring(1) : fileUrl;
            Path p = Paths.get(relative).normalize();
            if (!Files.exists(p)) return null;

            byte[] bytes = Files.readAllBytes(p);
            return Image.getInstance(bytes);
        } catch (Exception e) {
            return null;
        }
    }

    private String safe(Object v) {
        return v == null ? "" : String.valueOf(v);
    }

    private Paragraph spacer(int height) {
        Paragraph p = new Paragraph(" ");
        p.setSpacingBefore(height);
        return p;
    }
    private void addHeader(Document doc) throws Exception {
        PdfPTable header = new PdfPTable(1);
        header.setWidthPercentage(100);
        PdfPCell cell = new PdfPCell(new Phrase("REPORT - SKILL OVERVIEW", new Font(Font.HELVETICA, 16, Font.BOLD, Color.WHITE)));
        cell.setBackgroundColor(BLUE);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(20);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        header.addCell(cell);
        doc.add(header);
        doc.add(spacer(20));
    }

    private PdfPCell cardCell() {
        PdfPCell cell = new PdfPCell();
        cell.setBorderColor(new Color(220, 230, 245));
        cell.setPadding(15);
        cell.setBackgroundColor(Color.WHITE);
        return cell;
    }

    private Paragraph sectionTitle(String title) {
        Font f = new Font(Font.HELVETICA, 12, Font.BOLD, BLUE);
        Paragraph p = new Paragraph(title, f);
        p.setSpacingAfter(10);
        return p;
    }

    private Paragraph kv(String k, String v) {
        Font kf = new Font(Font.HELVETICA, 10, Font.BOLD, new Color(100, 100, 100));
        Font vf = new Font(Font.HELVETICA, 10, Font.NORMAL, new Color(40, 40, 40));
        Paragraph p = new Paragraph();
        p.add(new Chunk(k + ": ", kf));
        p.add(new Chunk(v, vf));
        p.setSpacingAfter(5);
        return p;
    }

    private PdfPCell chip(String txt, Color bg) {
        Font f = new Font(Font.HELVETICA, 10, Font.BOLD, Color.WHITE);
        PdfPCell c = new PdfPCell(new Phrase(txt, f));
        c.setBackgroundColor(bg);
        c.setPadding(5);
        c.setHorizontalAlignment(Element.ALIGN_CENTER);
        c.setBorder(Rectangle.NO_BORDER);
        return c;
    }

    private Color badgeColor(String badge) {
        if (badge == null) return Color.GRAY;
        return switch (badge) {
            case "CERTIFIED_EXPERT" -> GREEN;
            case "EXPERT" -> BLUE;
            case "ADVANCED" -> ORANGE;
            default -> Color.GRAY;
        };
    }
}