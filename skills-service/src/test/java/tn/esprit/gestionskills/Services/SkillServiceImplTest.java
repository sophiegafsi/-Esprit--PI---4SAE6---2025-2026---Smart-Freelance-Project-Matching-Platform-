package tn.esprit.gestionskills.Services;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.gestionskills.Entities.Skill;
import tn.esprit.gestionskills.Repositories.SkillRepository;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class SkillServiceImplTest {

    @Mock
    private SkillRepository skillRepository;

    @InjectMocks
    private SkillServiceImpl skillService;

    @Test
    void testAddSkill_Success() {
        Skill skill = new Skill();
        skill.setName("Java");
        
        when(skillRepository.existsByUserIdAndNameIgnoreCase(anyString(), anyString())).thenReturn(false);
        when(skillRepository.save(any(Skill.class))).thenReturn(skill);

        Skill result = skillService.addSkill(skill);

        assertNotNull(result);
        assertEquals("Java", result.getName());
        verify(skillRepository).save(any(Skill.class));
    }

    @Test
    void testGetBadge_Expert() {
        Skill skill = new Skill();
        skill.setId(1L);
        skill.setName("Java");
        skill.setLevel(tn.esprit.gestionskills.Entities.SkillLevel.EXPERT);
        skill.setYearsOfExperience(10); // 10*2 + 4*5 = 40. Wait, expert is score >= 70?
        // Let's check logic: years*2 + levelW*5 + validProofsCount*3
        // EXPERT weight = 4. 10*2 + 4*5 = 40. 
        // Need more years or proofs to reach 70.
        skill.setYearsOfExperience(25); // 25*2 + 4*5 = 70.
        
        when(skillRepository.findById(1L)).thenReturn(Optional.of(skill));
        
        String badge = skillService.getBadge(1L);
        
        assertEquals("EXPERT", badge);
    }

    @Test
    void testDeleteSkill_Success() {
        Skill skill = new Skill();
        skill.setId(1L);
        skill.setUserId("anonymousUser");
        
        when(skillRepository.findById(1L)).thenReturn(Optional.of(skill));
        
        skillService.deleteSkill(1L);
        
        verify(skillRepository).deleteById(1L);
    }
}
