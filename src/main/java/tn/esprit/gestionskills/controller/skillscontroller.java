package tn.esprit.gestionskills.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.esprit.gestionskills.Entities.skills;
import tn.esprit.gestionskills.Services.IskillsInterface;

import java.util.List;

@RestController
@RequestMapping("/skills")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class skillscontroller {

    private final IskillsInterface skillsService;

    @PostMapping("/add")
    public skills addSkill(@RequestBody skills s) {
        return skillsService.addSkill(s);
    }

    @PutMapping("/update")
    public skills updateSkill(@RequestBody skills s) {
        return skillsService.updateSkill(s);
    }

    @GetMapping("/{id}")
    public skills getSkillById(@PathVariable Long id) {
        return skillsService.getSkillById(id);
    }

    @GetMapping("/getall")
    public List<skills> getAllSkills() {
        return skillsService.getAllSkills();
    }

    @DeleteMapping("/{id}")
    public void deleteSkill(@PathVariable Long id) {
        skillsService.deleteSkill(id);
    }
}
