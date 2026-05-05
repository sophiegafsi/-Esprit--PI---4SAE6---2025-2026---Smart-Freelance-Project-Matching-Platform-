package tn.esprit.gestionskills.Services;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.gestionskills.Entities.skills;
import tn.esprit.gestionskills.Entities.skillsproof;

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

    private final IskillsInterface skillsService;

    // 🎨 Couleurs proches de ton UI
    private static final Color BLUE = new Color(17, 74, 158);
    private static final Color BLUE_LIGHT = new Color(232, 242, 255);
    private static final Color GRAY_LIGHT = new Color(245, 247, 250);
    private static final Color GREEN = new Color(46, 125, 50);
    private static final Color ORANGE = new Color(255, 152, 0);

    public byte[] exportSkillPdf(Long skillId) {
        skills s = skillsService.getSkillById(skillId);
        if (s == null) throw new IllegalArgumentException("Skill introuvable id=" + skillId);

        int score = skillsService.getScore(skillId);
        String badge = skillsService.getBadge(skillId);

        List<skillsproof> validProofs = (s.getProofs() == null) ? List.of()
                : s.getProofs().stream()
                .filter(this::isValidProof)
                .sorted(Comparator.comparing(skillsproof::getId))
                .toList();

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
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

    // -------------------------
    // Expiration
    // -------------------------
    private boolean isValidProof(skillsproof p) {
        return p.getExpiresAt() == null || !p.getExpiresAt().isBefore(LocalDate.now());
    }

    // -------------------------
    // HEADER (logo + titre)
    // -------------------------
    private void addHeader(Document doc) throws Exception {
        PdfPTable header = new PdfPTable(new float[]{1.2f, 4.8f});
        header.setWidthPercentage(100);

        PdfPCell logoCell = new PdfPCell();
        logoCell.setBackgroundColor(BLUE);
        logoCell.setBorder(Rectangle.NO_BORDER);
        logoCell.setPadding(12);
        logoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);

        Image logo = loadLogo(); // ✅ static/images.jpg
        if (logo != null) {
            logoCell.addElement(logo);
        } else {
            Font wf = new Font(Font.HELVETICA, 18, Font.BOLD, Color.WHITE);
            logoCell.addElement(new Paragraph("FreeLink", wf));
        }

        PdfPCell textCell = new PdfPCell();
        textCell.setBackgroundColor(BLUE);
        textCell.setBorder(Rectangle.NO_BORDER);
        textCell.setPadding(14);
        textCell.setVerticalAlignment(Element.ALIGN_MIDDLE);

        Font title = new Font(Font.HELVETICA, 20, Font.BOLD, Color.WHITE);
        Font sub = new Font(Font.HELVETICA, 11, Font.NORMAL, Color.WHITE);

        textCell.addElement(new Paragraph("Profil de compétence", title));
        textCell.addElement(new Paragraph("Généré le : " + LocalDate.now(), sub));

        header.addCell(logoCell);
        header.addCell(textCell);

        doc.add(header);
        doc.add(spacer(14));
    }

    private Image loadLogo() {
        try (InputStream is = getClass().getResourceAsStream("/static/images.jpg")) {
            if (is == null) return null;

            byte[] bytes = is.readAllBytes();
            Image logo = Image.getInstance(bytes);
            logo.scaleToFit(90, 90);
            logo.setAlignment(Image.MIDDLE);
            return logo;
        } catch (Exception e) {
            return null;
        }
    }

    // -------------------------
    // Cartes infos
    // -------------------------
    private void addInfoCards(Document doc, skills s, int score, String badge) throws Exception {
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

    private PdfPCell cardCell() {
        PdfPCell c = new PdfPCell();
        c.setBorderColor(new Color(220, 230, 245));
        c.setBorderWidth(1);
        c.setPadding(12);
        c.setBackgroundColor(Color.WHITE);
        return c;
    }

    private Paragraph sectionTitle(String t) {
        Font f = new Font(Font.HELVETICA, 13, Font.BOLD, BLUE);
        Paragraph p = new Paragraph(t, f);
        p.setSpacingAfter(10);
        return p;
    }

    private Paragraph kv(String k, String v) {
        Font key = new Font(Font.HELVETICA, 10, Font.BOLD, new Color(60, 60, 60));
        Font val = new Font(Font.HELVETICA, 10, Font.NORMAL, new Color(40, 40, 40));
        Paragraph p = new Paragraph();
        p.add(new Chunk(k + " : ", key));
        p.add(new Chunk(v == null ? "" : v, val));
        p.setSpacingAfter(6);
        return p;
    }

    private Color badgeColor(String badge) {
        if (badge == null) return BLUE;
        return switch (badge) {
            case "CERTIFIED_EXPERT" -> GREEN;
            case "EXPERT" -> BLUE;
            case "ADVANCED" -> ORANGE;
            default -> new Color(120, 120, 120);
        };
    }

    private PdfPCell chip(String text, Color bg) {
        Font f = new Font(Font.HELVETICA, 10, Font.BOLD, Color.WHITE);
        PdfPCell c = new PdfPCell(new Phrase("  " + safe(text) + "  ", f));
        c.setBackgroundColor(bg);
        c.setBorder(Rectangle.NO_BORDER);
        c.setHorizontalAlignment(Element.ALIGN_CENTER);
        c.setPadding(8);
        return c;
    }

    // -------------------------
    // Section preuves (table)
    // -------------------------
    private void addProofsSection(Document doc, List<skillsproof> validProofs) throws Exception {
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
        for (skillsproof p : validProofs) {
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
}