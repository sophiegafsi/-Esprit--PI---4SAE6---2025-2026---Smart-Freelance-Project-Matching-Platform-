package tn.esprit.GestionPortfolio.Client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import tn.esprit.GestionPortfolio.DTO.SkillDTO;

import java.util.List;

@FeignClient(name = "skills-service")
public interface SkillClient {

    @GetMapping("/skills/{id}")
    SkillDTO getSkillById(@PathVariable("id") Long id);

    @GetMapping("/skills/getall")
    List<SkillDTO> getAllSkills();
}