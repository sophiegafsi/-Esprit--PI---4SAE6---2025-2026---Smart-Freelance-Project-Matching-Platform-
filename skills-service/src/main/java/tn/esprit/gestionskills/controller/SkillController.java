package tn.esprit.gestionskills.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;
import tn.esprit.gestionskills.Entities.SkillLevel;
import tn.esprit.gestionskills.Entities.Skill;
import tn.esprit.gestionskills.Services.SkillService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import tn.esprit.gestionskills.Services.SkillPdfService;

import java.util.List;

@RestController
@RequestMapping("/skills")
@RequiredArgsConstructor
public class SkillController {

    private final SkillService skillsService;
    private final SkillPdfService skillPdfService;

    @PostMapping("/add")
    public Skill addSkill(@RequestBody Skill s) {
        return skillsService.addSkill(s);
    }

    @PutMapping("/update")
    public Skill updateSkill(@RequestBody Skill s) {
        return skillsService.updateSkill(s);
    }

    @GetMapping("/{id}")
    public Skill getSkillById(@PathVariable Long id) {
        return skillsService.getSkillById(id);
    }

    @GetMapping("/getall")
    public java.util.List<Skill> getAllSkills() {
        return skillsService.getAllSkills();
    }

    @DeleteMapping("/{id}")
    public void deleteSkill(@PathVariable Long id) {
        skillsService.deleteSkill(id);
    }

    @GetMapping("/search")
    public Page<Skill> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) SkillLevel level,
            @PageableDefault(size = 6) Pageable pageable
    ) {
        return skillsService.search(q, level, pageable);
    }

    @GetMapping("/{id}/score")
    public int getScore(@PathVariable Long id) {
        return skillsService.getScore(id);
    }

    @GetMapping("/{id}/badge")
    public String getBadge(@PathVariable Long id) {
        return skillsService.getBadge(id);
    }

    @GetMapping("/scoreboard")
    public List<tn.esprit.gestionskills.dto.SkillScoreDto> scoreboard(
            @RequestParam(defaultValue = "10") int size
    ) {
        return skillsService.getScoreboard(size);
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> exportSkillPdf(@PathVariable Long id) {
        byte[] pdf = skillPdfService.exportSkillPdf(id);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=skill_" + id + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }


}
