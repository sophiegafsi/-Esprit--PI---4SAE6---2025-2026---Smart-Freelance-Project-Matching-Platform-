const fs = require('fs');
const path = require('path');

function walk(dir, callback) {
    fs.readdirSync(dir).forEach(f => {
        let dirPath = path.join(dir, f);
        let isDirectory = fs.statSync(dirPath).isDirectory();
        isDirectory ? walk(dirPath, callback) : callback(path.join(dir, f));
    });
}

const targetDir = 'c:/Users/yusff/Desktop/4eme SAE/spring-boot-pidev/Desktop/4eme SAE/pi/portfolio-service/src/main/java';

walk(targetDir, (filePath) => {
    if (filePath.endsWith('.java')) {
        let content = fs.readFileSync(filePath, 'utf8');
        let newContent = content
            .replace(/Long freelancerId/g, 'String freelancerId')
            .replace(/freelancerId == null \|\| freelancerId <= 0/g, 'freelancerId == null || freelancerId.trim().isEmpty()')
            .replace(/@Positive\(message = "Freelancer ID must be greater than 0"\)(\r?\n)/g, '')
            .replace(/@NotNull\(message = "Freelancer ID is required"\)/g, '@NotBlank(message = "Freelancer ID is required")')
            .replace(/private Long freelancerId/g, 'private String freelancerId')
            .replace(/freelancerId <= 0/g, 'freelancerId.trim().isEmpty()')
            .replace(/Long getFreelancerId\(/g, 'String getFreelancerId(')
            .replace(/freelancerId \+ ""/g, 'freelancerId')
            .replace(/freelancerId: " \+ freelancerId/g, 'freelancerId: " + freelancerId')
            .replace(/Long\.valueOf\(freelancerId\)/g, 'freelancerId');

        if (content !== newContent) {
            fs.writeFileSync(filePath, newContent);
            console.log('Updated: ' + filePath);
        }
    }
});
