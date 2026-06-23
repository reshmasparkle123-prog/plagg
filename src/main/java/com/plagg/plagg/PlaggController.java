package com.plagg.plagg;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@CrossOrigin(origins = "*")
public class PlaggController {

    @GetMapping("/score")
    public String getScore(
        @RequestParam String name,
        @RequestParam String github,
        @RequestParam int leetcode,
        @RequestParam String linkedin,
        @RequestParam int hackathons) {

        // GitHub Score
        RestTemplate rt = new RestTemplate();
        String url = "https://api.github.com/users/" + github;
        String ghBody = rt.getForObject(url, String.class);
        
        int githubScore = 10;
        if(ghBody.contains("\"public_repos\":2")) githubScore += 20;
        if(ghBody.contains("\"public_repos\":3")) githubScore += 30;
        if(!ghBody.contains("\"followers\":0")) githubScore += 20;
        
        int leetcodeScore = Math.min(leetcode * 2, 100);
        int linkedinScore = linkedin.equals("yes") ? 100 : 30;
        int hackathonScore = Math.min(hackathons * 25, 100);
        int total = (githubScore + leetcodeScore + linkedinScore + hackathonScore) / 4;
        
        String status;
        if(total >= 70) status = "HIGHLY ELIGIBLE";
        else if(total >= 50) status = "ELIGIBLE";
        else if(total >= 30) status = "BORDERLINE";
        else status = "NOT ELIGIBLE";
        
        return "PLAGG REPORT\n" +
               "Candidate: " + name + "\n" +
               "GitHub Score: " + githubScore + "/100\n" +
               "LeetCode Score: " + leetcodeScore + "/100\n" +
               "LinkedIn Score: " + linkedinScore + "/100\n" +
               "Hackathon Score: " + hackathonScore + "/100\n" +
               "TOTAL: " + total + "/100\n" +
               "Status: " + status;
    }
}