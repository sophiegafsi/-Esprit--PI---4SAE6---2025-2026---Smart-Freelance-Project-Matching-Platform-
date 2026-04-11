package tn.esprit.GestionPortfolio.Services;

import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.GestionPortfolio.DTO.AchievementInsightDto;
import tn.esprit.GestionPortfolio.DTO.AchievementTimelineDto;
import tn.esprit.GestionPortfolio.DTO.ContributionDistributionDto;
import tn.esprit.GestionPortfolio.DTO.ProfileStatisticsDto;
import tn.esprit.GestionPortfolio.DTO.ProfileStrengthDto;
import tn.esprit.GestionPortfolio.DTO.SkillCredibilityDto;
import tn.esprit.GestionPortfolio.DTO.SkillDTO;
import tn.esprit.GestionPortfolio.DTO.SkillRankingDto;
import tn.esprit.GestionPortfolio.Entities.Achievement;
import tn.esprit.GestionPortfolio.Entities.AchievementMetric;
import tn.esprit.GestionPortfolio.Entities.AchievementSkill;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PortfolioAnalyticsPdfService {

    private static final Color NAVY = new Color(11, 36, 64);
    private static final Color CARD_BG = new Color(246, 249, 253);
    private static final Color BORDER = new Color(214, 224, 238);
    private static final Color TEXT = new Color(33, 49, 71);
    private static final Color MUTED = new Color(96, 112, 132);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final PortfolioAnalyticsLoaderService loaderService;
    private final PortfolioSkillAnalyticsService skillAnalyticsService;
    private final PortfolioProfileAnalyticsService profileAnalyticsService;

    public byte[] exportReport(Long freelancerId) {
        AnalyticsSnapshot snapshot = loaderService.loadSnapshot(freelancerId);
        List<SkillCredibilityDto> credibilityRows = skillAnalyticsService.buildCredibilityRows(snapshot);
        List<SkillRankingDto> rankingRows = skillAnalyticsService.buildRankingRows(credibilityRows);
        ProfileStrengthDto strength = profileAnalyticsService.buildProfileStrength(freelancerId, snapshot);
        ProfileStatisticsDto statistics = profileAnalyticsService.buildProfileStatistics(
                freelancerId,
                snapshot,
                credibilityRows,
                rankingRows
        );

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 32, 32, 28, 32);
            PdfWriter.getInstance(document, baos);
            document.open();

            addHeader(document, freelancerId, snapshot.achievements().size(), strength);
            addSummary(document, strength, statistics);
            addInsightSection(document, statistics);
            addRankingTable(document, rankingRows);
            addCredibilityTable(document, credibilityRows);
            addContributionDistribution(document, statistics.contributionDistribution());
            addTimeline(document, statistics.timeline());
            addPortfolioData(document, snapshot);

            document.close();
            return baos.toByteArray();
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to generate the portfolio analytics PDF.", ex);
        }
    }

    private void addHeader(
            Document document,
            Long freelancerId,
            int achievementsCount,
            ProfileStrengthDto strength
    ) throws Exception {
        PdfPTable header = new PdfPTable(new float[]{3.5f, 1.5f});
        header.setWidthPercentage(100);

        PdfPCell titleCell = new PdfPCell();
        titleCell.setBackgroundColor(NAVY);
        titleCell.setBorder(Rectangle.NO_BORDER);
        titleCell.setPadding(16);

        Paragraph eyebrow = new Paragraph("FreeLink Portfolio Report", font(11, Font.BOLD, Color.WHITE));
        eyebrow.setSpacingAfter(6);
        titleCell.addElement(eyebrow);

        Paragraph title = new Paragraph("Portfolio analytics and project data", font(20, Font.BOLD, Color.WHITE));
        title.setSpacingAfter(8);
        titleCell.addElement(title);

        Paragraph subtitle = new Paragraph(
                "This report consolidates the analytics dashboard with the portfolio items currently attached to the freelancer profile.",
                font(10, Font.NORMAL, new Color(232, 240, 255))
        );
        subtitle.setLeading(15);
        titleCell.addElement(subtitle);

        PdfPCell metaCell = new PdfPCell();
        metaCell.setBackgroundColor(new Color(21, 53, 89));
        metaCell.setBorder(Rectangle.NO_BORDER);
        metaCell.setPadding(16);
        metaCell.addElement(metaLine("Freelancer", "#" + freelancerId));
        metaCell.addElement(metaLine("Generated", DATE_FORMATTER.format(LocalDate.now())));
        metaCell.addElement(metaLine("Achievements", String.valueOf(achievementsCount)));
        metaCell.addElement(metaLine("Profile level", safe(strength.profileLevel())));

        header.addCell(titleCell);
        header.addCell(metaCell);
        document.add(header);
        document.add(spacer(14));
    }

    private void addSummary(
            Document document,
            ProfileStrengthDto strength,
            ProfileStatisticsDto statistics
    ) throws Exception {
        addSectionTitle(document, "Executive summary", "Core indicators of the portfolio strength.");

        PdfPTable grid = new PdfPTable(new float[]{1f, 1f, 1f, 1f});
        grid.setWidthPercentage(100);
        grid.setSpacingBefore(8);
        grid.setSpacingAfter(14);

        grid.addCell(summaryCard("Overall score", formatNumber(strength.overallScore()) + "/100", profileLabel(strength.profileLevel())));
        grid.addCell(summaryCard("Project quality", formatNumber(strength.averageProjectQuality()) + "/10", "Average quality across achievements"));
        grid.addCell(summaryCard("Distinct skills", String.valueOf(strength.distinctSkillsCount()), "Skills used in the portfolio"));
        grid.addCell(summaryCard("Average contribution", formatNumber(strength.averageContributionWeight()), "Average weight of linked skills"));

        document.add(grid);

        PdfPTable breakdown = new PdfPTable(new float[]{2.8f, 1f, 2.8f, 1f});
        breakdown.setWidthPercentage(100);
        breakdown.addCell(labelCell("Achievements component"));
        breakdown.addCell(valueCell(formatNumber(strength.achievementsComponent())));
        breakdown.addCell(labelCell("Diversity component"));
        breakdown.addCell(valueCell(formatNumber(strength.diversityComponent())));
        breakdown.addCell(labelCell("Contribution component"));
        breakdown.addCell(valueCell(formatNumber(strength.contributionComponent())));
        breakdown.addCell(labelCell("Quality component"));
        breakdown.addCell(valueCell(formatNumber(strength.qualityComponent())));
        breakdown.addCell(labelCell("Average complexity"));
        breakdown.addCell(valueCell(formatNumber(statistics.averageComplexityScore()) + "/10"));
        breakdown.addCell(labelCell("Average impact"));
        breakdown.addCell(valueCell(formatNumber(statistics.averageImpactScore()) + "/10"));
        breakdown.addCell(labelCell("Average duration"));
        breakdown.addCell(valueCell(formatNumber(statistics.averageDurationDays()) + " days"));
        document.add(breakdown);
        document.add(spacer(10));
    }

    private void addInsightSection(Document document, ProfileStatisticsDto statistics) throws Exception {
        addSectionTitle(document, "Key insights", "The strongest signals detected in the portfolio analytics.");

        PdfPTable insights = new PdfPTable(new float[]{1f, 1f, 1f});
        insights.setWidthPercentage(100);
        insights.setSpacingBefore(8);
        insights.setSpacingAfter(14);

        insights.addCell(summaryCard(
                "Top ranked skill",
                statistics.topRankedSkill() == null ? "N/A" : safe(statistics.topRankedSkill().skillName()),
                statistics.topRankedSkill() == null
                        ? "No ranking available yet"
                        : "Score " + formatNumber(statistics.topRankedSkill().rankingScore())
        ));
        insights.addCell(summaryCard(
                "Most credible skill",
                statistics.mostCredibleSkill() == null ? "N/A" : safe(statistics.mostCredibleSkill().skillName()),
                statistics.mostCredibleSkill() == null
                        ? "No credibility data yet"
                        : "Score " + formatNumber(statistics.mostCredibleSkill().credibilityScore())
        ));
        insights.addCell(summaryCard(
                "Strongest achievement",
                statistics.strongestAchievement() == null ? "N/A" : safe(statistics.strongestAchievement().title()),
                strongestAchievementHint(statistics.strongestAchievement())
        ));

        document.add(insights);
    }

    private void addRankingTable(Document document, List<SkillRankingDto> rankingRows) throws Exception {
        addSectionTitle(document, "Smart skill ranking", "Ranking based on credibility, usage frequency and project quality.");

        if (rankingRows.isEmpty()) {
            document.add(emptyState("No ranked skills are available for this freelancer yet."));
            document.add(spacer(10));
            return;
        }

        PdfPTable table = new PdfPTable(new float[]{0.9f, 2.5f, 1.2f, 1.2f, 1.2f});
        table.setWidthPercentage(100);
        table.setSpacingBefore(8);
        table.addCell(headerCell("Rank"));
        table.addCell(headerCell("Skill"));
        table.addCell(headerCell("Occurrences"));
        table.addCell(headerCell("Credibility"));
        table.addCell(headerCell("Score"));

        boolean alternate = false;
        for (SkillRankingDto row : rankingRows.stream().limit(10).toList()) {
            Color bg = alternate ? CARD_BG : Color.WHITE;
            table.addCell(dataCell("Top " + row.rank(), bg));
            table.addCell(dataCell(row.skillName(), bg));
            table.addCell(dataCell(String.valueOf(row.occurrences()), bg));
            table.addCell(dataCell(formatNumber(row.credibilityScore()), bg));
            table.addCell(dataCell(formatNumber(row.rankingScore()), bg));
            alternate = !alternate;
        }

        document.add(table);
        document.add(spacer(10));
    }

    private void addCredibilityTable(Document document, List<SkillCredibilityDto> credibilityRows) throws Exception {
        addSectionTitle(document, "Skill credibility", "How consistently each skill is backed by concrete portfolio evidence.");

        if (credibilityRows.isEmpty()) {
            document.add(emptyState("No skill credibility data is available for this freelancer yet."));
            document.add(spacer(10));
            return;
        }

        PdfPTable table = new PdfPTable(new float[]{2.2f, 1f, 1f, 1f, 1f, 1f});
        table.setWidthPercentage(100);
        table.setSpacingBefore(8);
        table.addCell(headerCell("Skill"));
        table.addCell(headerCell("Uses"));
        table.addCell(headerCell("Contribution"));
        table.addCell(headerCell("Complexity"));
        table.addCell(headerCell("Impact"));
        table.addCell(headerCell("Credibility"));

        boolean alternate = false;
        for (SkillCredibilityDto row : credibilityRows.stream().limit(12).toList()) {
            Color bg = alternate ? CARD_BG : Color.WHITE;
            table.addCell(dataCell(row.skillName(), bg));
            table.addCell(dataCell(String.valueOf(row.occurrences()), bg));
            table.addCell(dataCell(formatNumber(row.averageContributionWeight()), bg));
            table.addCell(dataCell(formatNumber(row.averageComplexityScore()), bg));
            table.addCell(dataCell(formatNumber(row.averageImpactScore()), bg));
            table.addCell(dataCell(formatNumber(row.credibilityScore()), bg));
            alternate = !alternate;
        }

        document.add(table);
        document.add(spacer(10));
    }

    private void addContributionDistribution(
            Document document,
            List<ContributionDistributionDto> contributionDistribution
    ) throws Exception {
        addSectionTitle(document, "Contribution distribution", "Breakdown of LOW, MEDIUM and HIGH skill usage inside the portfolio.");

        if (contributionDistribution == null || contributionDistribution.isEmpty()) {
            document.add(emptyState("No contribution distribution is available yet."));
            document.add(spacer(10));
            return;
        }

        PdfPTable table = new PdfPTable(new float[]{2f, 1f, 1f});
        table.setWidthPercentage(100);
        table.setSpacingBefore(8);
        table.addCell(headerCell("Level"));
        table.addCell(headerCell("Count"));
        table.addCell(headerCell("Share"));

        boolean alternate = false;
        for (ContributionDistributionDto row : contributionDistribution) {
            Color bg = alternate ? CARD_BG : Color.WHITE;
            table.addCell(dataCell(readableContribution(row.level()), bg));
            table.addCell(dataCell(String.valueOf(row.count()), bg));
            table.addCell(dataCell(formatNumber(row.percentage()) + "%", bg));
            alternate = !alternate;
        }

        document.add(table);
        document.add(spacer(10));
    }

    private void addTimeline(Document document, List<AchievementTimelineDto> timeline) throws Exception {
        addSectionTitle(document, "Achievement timeline", "Periods and average quality of completed portfolio items.");

        if (timeline == null || timeline.isEmpty()) {
            document.add(emptyState("No timeline data is available yet."));
            document.add(spacer(10));
            return;
        }

        PdfPTable table = new PdfPTable(new float[]{1.4f, 1f, 1.2f});
        table.setWidthPercentage(100);
        table.setSpacingBefore(8);
        table.addCell(headerCell("Period"));
        table.addCell(headerCell("Achievements"));
        table.addCell(headerCell("Avg quality"));

        boolean alternate = false;
        for (AchievementTimelineDto row : timeline) {
            Color bg = alternate ? CARD_BG : Color.WHITE;
            table.addCell(dataCell(safe(row.period()), bg));
            table.addCell(dataCell(String.valueOf(row.achievementsCount()), bg));
            table.addCell(dataCell(formatNumber(row.averageQualityScore()) + "/10", bg));
            alternate = !alternate;
        }

        document.add(table);
        document.add(spacer(10));
    }

    private void addPortfolioData(Document document, AnalyticsSnapshot snapshot) throws Exception {
        addSectionTitle(document, "Portfolio data", "Achievements, linked skills and attached project metrics.");

        List<Achievement> achievements = snapshot.achievements().stream()
                .filter(Objects::nonNull)
                .sorted(
                        Comparator.comparing(Achievement::getCompletionDate, Comparator.nullsLast(Comparator.reverseOrder()))
                                .thenComparing(Achievement::getTitle, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER))
                )
                .toList();

        if (achievements.isEmpty()) {
            document.add(emptyState("No achievements are currently attached to this freelancer."));
            return;
        }

        Map<Long, SkillDTO> skillsById = snapshot.skillsById();
        for (Achievement achievement : achievements) {
            PdfPTable card = new PdfPTable(1);
            card.setWidthPercentage(100);
            card.setSpacingBefore(8);
            card.setSpacingAfter(8);

            PdfPCell cell = new PdfPCell();
            cell.setPadding(14);
            cell.setBorderColor(BORDER);
            cell.setBackgroundColor(Color.WHITE);

            Paragraph title = new Paragraph(safe(achievement.getTitle()), font(14, Font.BOLD, NAVY));
            title.setSpacingAfter(6);
            cell.addElement(title);

            cell.addElement(paragraph(
                    "Date: " + formatDate(achievement.getCompletionDate()) + "   |   Freelancer: #" + safe(achievement.getFreelancerId()),
                    10,
                    Font.BOLD,
                    MUTED,
                    6
            ));
            cell.addElement(paragraph(safe(achievement.getDescription()), 10, Font.NORMAL, TEXT, 8));
            cell.addElement(paragraph("Linked skills", 10, Font.BOLD, NAVY, 4));
            cell.addElement(paragraph(skillsSummary(snapshot.achievementSkills(), skillsById, achievement.getId()), 9, Font.NORMAL, TEXT, 8));
            cell.addElement(paragraph("Project metric", 10, Font.BOLD, NAVY, 4));
            cell.addElement(paragraph(metricSummary(snapshot.metricsByAchievementId().get(achievement.getId())), 9, Font.NORMAL, TEXT, 0));

            card.addCell(cell);
            document.add(card);
        }
    }

    private PdfPCell summaryCard(String label, String value, String hint) {
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(CARD_BG);
        cell.setBorderColor(BORDER);
        cell.setPadding(12);

        Paragraph labelP = new Paragraph(label, font(9, Font.BOLD, MUTED));
        labelP.setSpacingAfter(8);
        cell.addElement(labelP);

        Paragraph valueP = new Paragraph(value, font(16, Font.BOLD, NAVY));
        valueP.setSpacingAfter(6);
        cell.addElement(valueP);

        Paragraph hintP = new Paragraph(hint, font(8, Font.NORMAL, MUTED));
        hintP.setLeading(12);
        cell.addElement(hintP);

        return cell;
    }

    private PdfPCell labelCell(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font(9, Font.BOLD, MUTED)));
        cell.setBackgroundColor(CARD_BG);
        cell.setBorderColor(BORDER);
        cell.setPadding(8);
        return cell;
    }

    private PdfPCell valueCell(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font(10, Font.BOLD, NAVY)));
        cell.setBackgroundColor(Color.WHITE);
        cell.setBorderColor(BORDER);
        cell.setPadding(8);
        return cell;
    }

    private PdfPCell headerCell(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font(9, Font.BOLD, Color.WHITE)));
        cell.setBackgroundColor(NAVY);
        cell.setBorderColor(BORDER);
        cell.setPadding(8);
        return cell;
    }

    private PdfPCell dataCell(String text, Color bg) {
        PdfPCell cell = new PdfPCell(new Phrase(safe(text), font(9, Font.NORMAL, TEXT)));
        cell.setBackgroundColor(bg);
        cell.setBorderColor(BORDER);
        cell.setPadding(8);
        return cell;
    }

    private Paragraph metaLine(String label, String value) {
        Paragraph paragraph = new Paragraph();
        paragraph.add(new Phrase(label + ": ", font(8, Font.BOLD, new Color(185, 206, 236))));
        paragraph.add(new Phrase(safe(value), font(10, Font.BOLD, Color.WHITE)));
        paragraph.setSpacingAfter(8);
        return paragraph;
    }

    private void addSectionTitle(Document document, String title, String subtitle) throws Exception {
        Paragraph titleP = new Paragraph(title, font(13, Font.BOLD, NAVY));
        titleP.setSpacingAfter(4);
        document.add(titleP);

        Paragraph subtitleP = new Paragraph(subtitle, font(9, Font.NORMAL, MUTED));
        subtitleP.setSpacingAfter(6);
        document.add(subtitleP);
    }

    private Paragraph emptyState(String text) {
        Paragraph paragraph = new Paragraph(text, font(9, Font.ITALIC, MUTED));
        paragraph.setSpacingBefore(8);
        return paragraph;
    }

    private Paragraph paragraph(String text, int size, int style, Color color, float spacingAfter) {
        Paragraph paragraph = new Paragraph(safe(text), font(size, style, color));
        paragraph.setLeading(size + 4);
        paragraph.setSpacingAfter(spacingAfter);
        return paragraph;
    }

    private Paragraph spacer(float height) {
        Paragraph spacer = new Paragraph(" ", font(1, Font.NORMAL, Color.WHITE));
        spacer.setSpacingBefore(height);
        return spacer;
    }

    private Font font(int size, int style, Color color) {
        return new Font(Font.HELVETICA, size, style, color);
    }

    private String profileLabel(String profileLevel) {
        String value = safe(profileLevel).toUpperCase();
        if ("ELITE".equals(value)) return "Elite profile";
        if ("STRONG".equals(value)) return "Strong profile";
        if ("DEVELOPING".equals(value)) return "Developing profile";
        return value.isBlank() ? "Profile level pending" : value;
    }

    private String strongestAchievementHint(AchievementInsightDto achievement) {
        if (achievement == null) {
            return "No highlighted achievement yet";
        }
        return "Quality " + formatNumber(achievement.qualityScore()) + " with "
                + safe(achievement.linkedSkillsCount()) + " linked skills";
    }

    private String readableContribution(String level) {
        String value = safe(level).toUpperCase();
        if ("HIGH".equals(value)) return "High";
        if ("MEDIUM".equals(value)) return "Medium";
        if ("LOW".equals(value)) return "Low";
        return value;
    }

    private String skillsSummary(List<AchievementSkill> achievementSkills, Map<Long, SkillDTO> skillsById, Long achievementId) {
        List<String> lines = new ArrayList<>();
        for (AchievementSkill row : achievementSkills) {
            if (row == null || row.getAchievement() == null || !Objects.equals(row.getAchievement().getId(), achievementId)) {
                continue;
            }

            SkillDTO skill = skillsById.get(row.getSkillId());
            String skillName = skill != null && skill.getName() != null && !skill.getName().isBlank()
                    ? skill.getName()
                    : "Skill #" + row.getSkillId();

            lines.add(skillName
                    + " [" + safe(row.getContributionLevel()) + "]"
                    + " - " + safe(row.getUsageDescription()));
        }

        if (lines.isEmpty()) {
            return "No linked skills for this achievement.";
        }

        return lines.stream().collect(Collectors.joining("\n"));
    }

    private String metricSummary(AchievementMetric metric) {
        if (metric == null) {
            return "No metric attached yet. The analytics uses neutral fallback values when needed.";
        }

        return "Complexity: " + safe(metric.getComplexityScore())
                + "/10 | Impact: " + safe(metric.getImpactScore())
                + "/10 | Duration: " + safe(metric.getDurationDays())
                + " days | Quality: " + formatNumber(PortfolioAnalyticsMath.projectQuality(metric)) + "/10";
    }

    private String formatDate(LocalDate date) {
        return date == null ? "N/A" : DATE_FORMATTER.format(date);
    }

    private String formatNumber(Number value) {
        if (value == null) {
            return "0";
        }
        return String.format(java.util.Locale.US, "%.2f", value.doubleValue());
    }

    private String safe(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}
