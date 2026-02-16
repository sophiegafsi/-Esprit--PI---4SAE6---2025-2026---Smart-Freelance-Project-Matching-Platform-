package freelink.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FreelancerProfileDTO {
    private String jobTitle;
    private String bio;
    private String skills;
    private Double hourlyRate;
    private String portfolioUrl;
}
