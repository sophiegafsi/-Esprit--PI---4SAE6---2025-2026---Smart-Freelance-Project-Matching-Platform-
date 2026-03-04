package freelink.condidature.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import freelink.condidature.entity.Contract;
import freelink.condidature.entity.Project;
import freelink.condidature.repository.ContractRepository;
import freelink.condidature.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import java.time.LocalDateTime;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ContractService {

        private final ContractRepository contractRepository;
        private final ProjectRepository projectRepository;
        private final freelink.condidature.repository.CandidatureRepository candidatureRepository;
        private final org.springframework.web.client.RestTemplate restTemplate;

        private static final String USER_SERVICE_URL = "http://localhost:8082/api/users/";

        @Transactional
        public Contract createContract(Contract contract) {
                return contractRepository.save(contract);
        }

        public Contract getContract(UUID id) {
                return contractRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Contract not found"));
        }

        public List<Contract> getContractsByClient(UUID clientId) {
                return contractRepository.findByClientId(clientId);
        }

        public List<Contract> getContractsByFreelancer(UUID freelancerId) {
                return contractRepository.findByFreelancerId(freelancerId);
        }

        @Transactional
        public Contract signByClient(UUID id, String signature) {
                Contract contract = getContract(id);
                contract.setClientSignature(signature);
                if (contract.getStatus() == Contract.ContractStatus.PENDING) {
                        contract.setStatus(Contract.ContractStatus.ONESIDED);
                }
                return contractRepository.save(contract);
        }

        @Transactional
        public Contract signByFreelancer(UUID id, String signature) {
                Contract contract = getContract(id);
                contract.setFreelancerSignature(signature);
                if (contract.getStatus() == Contract.ContractStatus.ONESIDED) {
                        contract.setStatus(Contract.ContractStatus.COMPLETED);

                        // 1. Close the Project
                        projectRepository.findById(contract.getProjectId()).ifPresent(project -> {
                                project.setStatus(Project.ProjectStatus.CLOSED);
                                projectRepository.save(project);
                        });

                        // 2. Reject all other pending applications for this project
                        List<freelink.condidature.entity.Candidature> allApps = candidatureRepository
                                        .findByProjectId(contract.getProjectId());
                        for (freelink.condidature.entity.Candidature app : allApps) {
                                if (app.getId().equals(contract.getCandidatureId())) {
                                        continue; // Skip the winning application
                                }
                                if (app.getStatus() == freelink.condidature.entity.Candidature.Status.PENDING) {
                                        app.setStatus(freelink.condidature.entity.Candidature.Status.REJECTED);
                                        candidatureRepository.save(app);
                                }
                        }
                }
                return contractRepository.save(contract);
        }

        @Scheduled(fixedRate = 60000) // Run every 60 seconds for testing
        @Transactional
        public void abortExpiredContracts() {
                // For testing: abort if older than 1 minute.
                LocalDateTime threshold = LocalDateTime.now().minusMinutes(1);

                List<Contract> expiredContracts = contractRepository.findAll().stream()
                                .filter(c -> c.getStatus() == Contract.ContractStatus.ONESIDED)
                                .filter(c -> c.getCreatedAt() != null && c.getCreatedAt().isBefore(threshold))
                                .toList();

                for (Contract contract : expiredContracts) {
                        System.out.println("Auto-aborting contract: " + contract.getId());
                        contract.setStatus(Contract.ContractStatus.ABORTED);
                        contractRepository.save(contract);

                        // Reject associated candidature
                        candidatureRepository.findById(contract.getCandidatureId()).ifPresent(app -> {
                                app.setStatus(freelink.condidature.entity.Candidature.Status.REJECTED);
                                candidatureRepository.save(app);
                        });
                }
        }

        public List<Contract> getAllContracts() {
                return contractRepository.findAll();
        }

        private String fetchUserName(UUID userId) {
                try {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> user = restTemplate.getForObject(USER_SERVICE_URL + userId, Map.class);
                        if (user != null) {
                                String firstName = (String) user.get("firstName");
                                String lastName = (String) user.get("lastName");
                                return (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
                        }
                } catch (Exception e) {
                        System.err.println("Failed to fetch user name for " + userId + ": " + e.getMessage());
                }
                return "Unknown User";
        }

        public byte[] generateContractPdf(UUID contractId) throws IOException {
                Contract contract = getContract(contractId);
                String clientName = fetchUserName(contract.getClientId());
                String freelancerName = fetchUserName(contract.getFreelancerId());

                String projectTitle = "Professional Project";
                try {
                        UUID pId = contract.getProjectId();
                        if (pId != null) {
                                projectTitle = projectRepository.findById(pId)
                                                .map(Project::getTitle)
                                                .orElse("Unknown Project");
                        }
                } catch (Exception e) {
                        System.err.println("Failed to fetch project title for contract " + contractId + ": "
                                        + e.getMessage());
                }

                ByteArrayOutputStream out = new ByteArrayOutputStream();
                Document document = new Document(PageSize.A4, 50, 50, 50, 50);
                PdfWriter.getInstance(document, out);

                document.open();

                // 1. BRANDING HEADER
                Font brandFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 28, java.awt.Color.BLUE);
                Paragraph brand = new Paragraph("FreeLink", brandFont);
                brand.setAlignment(Element.ALIGN_RIGHT);
                document.add(brand);

                Paragraph slogan = new Paragraph("Connecting Excellence with Talent",
                                FontFactory.getFont(FontFactory.HELVETICA, 10, java.awt.Color.GRAY));
                slogan.setAlignment(Element.ALIGN_RIGHT);
                slogan.setSpacingAfter(30);
                document.add(slogan);

                // 2. TITLE
                Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20);
                Paragraph title = new Paragraph("FORMAL SERVICE AGREEMENT", titleFont);
                title.setAlignment(Element.ALIGN_CENTER);
                title.setSpacingAfter(10);
                document.add(title);

                Paragraph subTitle = new Paragraph(projectTitle.toUpperCase(),
                                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14));
                subTitle.setAlignment(Element.ALIGN_CENTER);
                subTitle.setSpacingAfter(40);
                document.add(subTitle);

                // 3. INFORMATION TABLE
                PdfPTable table = new PdfPTable(2);
                table.setWidthPercentage(100);
                table.setSpacingBefore(10f);
                table.setSpacingAfter(10f);

                addTableCell(table, "Contract ID:", contract.getId() != null ? contract.getId().toString() : "N/A");
                addTableCell(table, "Created Date:",
                                contract.getCreatedAt() != null
                                                ? contract.getCreatedAt()
                                                                .format(DateTimeFormatter.ofPattern("MMMM dd, yyyy"))
                                                : "N/A");
                addTableCell(table, "Application ID:",
                                contract.getCandidatureId() != null ? contract.getCandidatureId().toString()
                                                : "N/A");
                addTableCell(table, "Project Reference:", projectTitle);
                addTableCell(table, "Client ID:",
                                contract.getClientId() != null ? contract.getClientId().toString() : "N/A");
                addTableCell(table, "Freelancer ID:",
                                contract.getFreelancerId() != null ? contract.getFreelancerId().toString() : "N/A");

                document.add(table);
                document.add(new Paragraph(" "));

                // 4. PARTIES
                Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
                Paragraph partiesHeader = new Paragraph("1. THE PARTIES", sectionFont);
                partiesHeader.setSpacingAfter(10);
                document.add(partiesHeader);

                document.add(new Paragraph("This Service Agreement is entered into between:",
                                FontFactory.getFont(FontFactory.HELVETICA, 12)));
                document.add(new Paragraph("   - CLIENT: " + clientName + " (ID: " + contract.getClientId() + ")",
                                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));
                document.add(new Paragraph(
                                "   - FREELANCER: " + freelancerName + " (ID: " + contract.getFreelancerId() + ")",
                                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));
                document.add(new Paragraph(" "));

                // 5. BUDGET & TIMELINE
                Paragraph financialsHeader = new Paragraph("2. FINANCIALS & TIMELINE", sectionFont);
                financialsHeader.setSpacingAfter(10);
                document.add(financialsHeader);

                document.add(new Paragraph(
                                "The agreed total budget for this project is: " + (contract.getBudget() != null
                                                ? "$" + String.format("%.2f", contract.getBudget())
                                                : "TBD"),
                                FontFactory.getFont(FontFactory.HELVETICA, 12)));

                String start = contract.getStartDate() != null
                                ? contract.getStartDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
                                : "TBD";
                String end = contract.getEndDate() != null
                                ? contract.getEndDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
                                : "TBD";
                document.add(new Paragraph(
                                "Work is scheduled to commence on " + start + " and conclude by " + end + ".",
                                FontFactory.getFont(FontFactory.HELVETICA, 12)));
                document.add(new Paragraph(" "));

                // 6. SCOPE OF WORK
                Paragraph scopeHeader = new Paragraph("3. SCOPE OF WORK / TERMS", sectionFont);
                scopeHeader.setSpacingAfter(10);
                document.add(scopeHeader);

                document.add(new Paragraph(contract.getTerms() != null ? contract.getTerms() : "No terms provided",
                                FontFactory.getFont(FontFactory.HELVETICA, 11)));
                document.add(new Paragraph(" "));
                document.add(new Paragraph(" "));
                document.add(new Paragraph(" "));

                // 7. SIGNATURES
                PdfPTable sigTable = new PdfPTable(2);
                sigTable.setWidthPercentage(100);

                // Helper to add signature or placeholder
                addSignatureCell(sigTable, contract.getClientSignature(), "Client Signature (" + clientName + ")");
                addSignatureCell(sigTable, contract.getFreelancerSignature(),
                                "Freelancer Signature (" + freelancerName + ")");

                document.add(sigTable);

                document.close();
                return out.toByteArray();
        }

        private void addSignatureCell(PdfPTable table, String signatureBase64, String label) {
                try {
                        PdfPCell cell = new PdfPCell();
                        cell.setBorder(Rectangle.NO_BORDER);
                        cell.setHorizontalAlignment(Element.ALIGN_CENTER);

                        if (signatureBase64 != null && !signatureBase64.isEmpty()) {
                                // Remove data:image/png;base64, prefix if present
                                String data = signatureBase64.contains(",") ? signatureBase64.split(",")[1]
                                                : signatureBase64;
                                byte[] decoded = java.util.Base64.getDecoder().decode(data);
                                Image img = Image.getInstance(decoded);
                                img.scaleToFit(120, 60);
                                img.setAlignment(Element.ALIGN_CENTER);
                                cell.addElement(img);
                        } else {
                                cell.addElement(new Phrase("__________________________"));
                        }

                        Paragraph pLabel = new Paragraph(label, FontFactory.getFont(FontFactory.HELVETICA, 10));
                        pLabel.setAlignment(Element.ALIGN_CENTER);
                        cell.addElement(pLabel);

                        table.addCell(cell);
                } catch (Exception e) {
                        System.err.println("Error adding signature to PDF: " + e.getMessage());
                        PdfPCell cell = new PdfPCell(new Phrase("Signature Error\n" + label));
                        cell.setBorder(Rectangle.NO_BORDER);
                        table.addCell(cell);
                }
        }

        private void addTableCell(PdfPTable table, String label, String value) {
                Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
                Font valueFont = FontFactory.getFont(FontFactory.HELVETICA, 10);

                PdfPCell cellLabel = new PdfPCell(new Phrase(label, labelFont));
                cellLabel.setBorder(Rectangle.NO_BORDER);
                cellLabel.setPadding(5);

                PdfPCell cellValue = new PdfPCell(new Phrase(value, valueFont));
                cellValue.setBorder(Rectangle.NO_BORDER);
                cellValue.setPadding(5);

                table.addCell(cellLabel);
                table.addCell(cellValue);
        }
}
