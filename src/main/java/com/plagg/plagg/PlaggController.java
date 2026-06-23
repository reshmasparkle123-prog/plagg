package com.plagg.plagg;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@CrossOrigin(origins = "*")
public class PlaggController {

    @GetMapping("/")
    public ResponseEntity<String> home() {
        String html = """
            <!DOCTYPE html>
            <html>
            <head>
                <title>Plagg - AI Recruiting Assistant</title>
                <style>
                    body { font-family: Arial; background: #0d0d0d; color: white; text-align: center; padding: 50px; }
                    h1 { color: #00ff88; font-size: 3em; margin-bottom: 5px; }
                    .subtitle { color: #aaa; font-size: 1.2em; margin-bottom: 40px; }
                    input, select { padding: 12px; margin: 8px; width: 300px; border-radius: 8px; border: none; font-size: 1em; }
                    button { background: #00ff88; color: black; padding: 15px 40px; border: none; border-radius: 8px; font-size: 1.2em; cursor: pointer; font-weight: bold; margin-top: 20px; }
                    button:hover { background: #00cc66; }
                </style>
            </head>
            <body>
                <h1>🔍 PLAGG</h1>
                <p class="subtitle">AI-Powered Recruiting Assistant</p>
                <form action="/score" method="get">
                    <br><input name="name" placeholder="Candidate Name" required/>
                    <br><input name="github" placeholder="GitHub Username" required/>
                    <br><input name="leetcode" type="number" placeholder="LeetCode Problems Solved" required/>
                    <br><select name="linkedin">
                        <option value="yes">LinkedIn Complete ✅</option>
                        <option value="no">LinkedIn Incomplete ❌</option>
                    </select>
                    <br><input name="hackathons" type="number" placeholder="Hackathons Participated" required/>
                    <br><button type="submit">Generate Report 🚀</button>
                </form>
            </body>
            </html>
            """;
        return ResponseEntity.ok().header("Content-Type", "text/html").body(html);
    }

    @GetMapping("/score")
    public ResponseEntity<String> getScore(
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
        if(ghBody.contains("\"public_repos\":4")) githubScore += 40;
        if(!ghBody.contains("\"followers\":0")) githubScore += 20;
        githubScore = Math.min(githubScore, 100);

        int leetcodeScore = Math.min(leetcode * 2, 100);
        int linkedinScore = linkedin.equals("yes") ? 100 : 30;
        int hackathonScore = Math.min(hackathons * 25, 100);
        int total = (githubScore + leetcodeScore + linkedinScore + hackathonScore) / 4;

        String status;
        String emoji;
        String color;
        if(total >= 70) { status = "HIGHLY ELIGIBLE"; emoji = "🌟"; color = "#00ff88"; }
        else if(total >= 50) { status = "ELIGIBLE"; emoji = "✅"; color = "#00cc66"; }
        else if(total >= 30) { status = "BORDERLINE"; emoji = "⚠️"; color = "#ffaa00"; }
        else { status = "NOT ELIGIBLE"; emoji = "❌"; color = "#ff4444"; }

        String html = """
            <!DOCTYPE html>
            <html>
            <head>
                <title>Plagg Report - %s</title>
                <style>
                    body { font-family: Arial; background: #0d0d0d; color: white; text-align: center; padding: 50px; }
                    h1 { color: #00ff88; font-size: 3em; }
                    .card { background: #1a1a1a; border-radius: 16px; padding: 30px; max-width: 500px; margin: 0 auto; }
                    .score-row { display: flex; justify-content: space-between; padding: 12px 0; border-bottom: 1px solid #333; font-size: 1.1em; }
                    .total { font-size: 1.5em; font-weight: bold; color: #00ff88; margin-top: 20px; }
                    .status { font-size: 2em; font-weight: bold; color: %s; margin-top: 10px; }
                    .back { background: #00ff88; color: black; padding: 12px 30px; border: none; border-radius: 8px; font-size: 1em; cursor: pointer; font-weight: bold; margin-top: 20px; text-decoration: none; display: inline-block; }
                </style>
            </head>
            <body>
                <h1>🔍 PLAGG REPORT</h1>
                <div class="card">
                    <h2>%s</h2>
                    <p style="color:#aaa">@%s</p>
                    <div class="score-row"><span>GitHub Score</span><span>%d/100</span></div>
                    <div class="score-row"><span>LeetCode Score</span><span>%d/100</span></div>
                    <div class="score-row"><span>LinkedIn Score</span><span>%d/100</span></div>
                    <div class="score-row"><span>Hackathon Score</span><span>%d/100</span></div>
                    <div class="total">TOTAL: %d/100</div>
                    <div class="status">%s %s</div>
                    <br><a class="back" href="/">← Evaluate Another</a>
                </div>
            </body>
            </html>
            """.formatted(name, color, name, github, githubScore, leetcodeScore, linkedinScore, hackathonScore, total, emoji, status);

        return ResponseEntity.ok().header("Content-Type", "text/html").body(html);
    }
}