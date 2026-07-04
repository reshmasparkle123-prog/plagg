package com.plagg.plagg;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@CrossOrigin(origins = "*")
public class PlaggController {

    static final List<Candidate> LEADERBOARD = new CopyOnWriteArrayList<>();

    static {
        LEADERBOARD.add(new Candidate("Aarav Sharma", "aaravdev", 18, 22, 15, 8, 17, 9, "Consistently active GitHub profile with strong LeetCode fundamentals. Ready for technical rounds."));
        LEADERBOARD.add(new Candidate("Diya Patel", "diyacodes", 16, 19, 15, 6, 15, 8, "Solid all-round profile. Hackathon exposure suggests good team collaboration skills."));
        LEADERBOARD.add(new Candidate("Kabir Singh", "kabirs", 13, 15, 10, 4, 10, 6, "Growing profile, recommend a technical screening call before final decision."));
        LEADERBOARD.add(new Candidate("Ananya Rao", "ananyar", 17, 21, 15, 8, 16, 9, "Strong problem-solving track record paired with active open-source contributions."));
        LEADERBOARD.add(new Candidate("Vivaan Mehta", "vivaanm", 10, 9, 10, 2, 8, 5, "Early-stage profile. Potential is there but needs more demonstrated project work."));
        LEADERBOARD.add(new Candidate("Ishaan Gupta", "ishaang", 15, 18, 15, 6, 14, 8, "Well-rounded candidate with consistent progress across all evaluation areas."));
        LEADERBOARD.add(new Candidate("Myra Nair", "myran", 17, 22, 15, 8, 17, 9, "Top-tier algorithmic skills. Strong candidate for technically demanding roles."));
        LEADERBOARD.add(new Candidate("Reyansh Kumar", "reyanshk", 8, 7, 6, 2, 6, 5, "Profile is still early. Recommend follow-up in a few months as it develops."));
        LEADERBOARD.add(new Candidate("Sara Iyer", "sarai", 14, 17, 15, 4, 13, 7, "Good balance of coding skill and networking. Comfortable pace of growth."));
        LEADERBOARD.add(new Candidate("Aditya Verma", "adityav", 15, 19, 10, 6, 14, 8, "Reliable performer across categories with room to grow LinkedIn presence."));
        LEADERBOARD.add(new Candidate("Kiara Joshi", "kiaraj", 18, 21, 15, 8, 17, 9, "Excellent well-rounded profile, one of the strongest in this batch."));
        LEADERBOARD.add(new Candidate("Arjun Reddy", "arjunr", 12, 14, 10, 4, 10, 6, "Moderate profile. Coding fundamentals are fine, needs more visible projects."));
        LEADERBOARD.add(new Candidate("Zara Khan", "zarak", 16, 17, 15, 6, 15, 8, "Strong professional presence with steady, consistent skill growth."));
    }

    static class Candidate {
        String name, github, insight;
        int githubScore, leetcodeScore, linkedinScore, hackathonScore, resumeScore, aiBonus;

        Candidate(String name, String github, int githubScore, int leetcodeScore,
                  int linkedinScore, int hackathonScore, int resumeScore, int aiBonus, String insight) {
            this.name = name;
            this.github = github;
            this.githubScore = githubScore;
            this.leetcodeScore = leetcodeScore;
            this.linkedinScore = linkedinScore;
            this.hackathonScore = hackathonScore;
            this.resumeScore = resumeScore;
            this.aiBonus = aiBonus;
            this.insight = insight;
        }

        int total() {
            return githubScore + leetcodeScore + linkedinScore + hackathonScore + resumeScore + aiBonus;
        }
    }

    static final String[] RESUME_KEYWORDS = {
        "java", "python", "javascript", "typescript", "react", "node", "sql", "aws",
        "docker", "kubernetes", "machine learning", "spring", "git", "api", "cloud",
        "c++", "html", "css", "agile", "leadership", "project", "internship",
        "hackathon", "open source", "data structures", "algorithms"
    };

    static int computeResumeScore(String resume) {
        if (resume == null || resume.isBlank()) return 4;
        String text = resume.toLowerCase();
        int score = 5;
        int matches = 0;
        for (String k : RESUME_KEYWORDS) {
            if (text.contains(k)) matches++;
        }
        score += Math.min(matches, 10);
        if (text.matches("(?s).*\\d+%.*") || text.matches("(?s).*\\b\\d{2,}\\+.*")) score += 3;
        if (resume.trim().length() > 150) score += 2;
        return Math.min(score, 20);
    }

    static String resumeFeedback(String resume, int resumeScore) {
        if (resume == null || resume.isBlank()) {
            return "No resume text provided - add one next time for a deeper AI screening score.";
        }
        if (resumeScore >= 16) {
            return "Strong resume: good technical keyword coverage and quantified achievements.";
        }
        if (resumeScore >= 10) {
            return "Decent resume, but could use more specific metrics or technical keywords.";
        }
        return "Resume is thin on technical keywords and measurable achievements - consider expanding it.";
    }

    static String generateInsight(int githubScore, int leetcodeScore, int linkedinScore,
                                   int hackathonScore, int resumeScore, int total) {
        if (total >= 70) {
            return "Standout candidate, strong scores across the board. Fast-track to interview.";
        } else if (total >= 50) {
            if (leetcodeScore >= 18) {
                return "Solid algorithmic foundation with a well-balanced overall profile. Interview-ready.";
            }
            return "Good overall profile with consistent activity. Recommended for the next round.";
        } else if (total >= 30) {
            if (githubScore < 10) {
                return "Profile shows potential but GitHub activity is limited. Suggest a portfolio review.";
            }
            return "Borderline profile, worth a short screening call before deciding.";
        } else {
            return "Profile is still early-stage. Recommend revisiting after more projects/practice.";
        }
    }

    static String errorPage(List<String> errors) {
        StringBuilder items = new StringBuilder();
        for (String e : errors) {
            items.append("<li>").append(e).append("</li>");
        }
        String html = """
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>PLAGG - Invalid Input</title>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=Space+Grotesk:wght@500;700&display=swap" rel="stylesheet">
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body { font-family: 'Inter', sans-serif; background: #0a0a0a; color: #fff; min-height: 100vh; display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 24px; }
        .err-card { background: #141414; border: 1px solid #3a1414; border-radius: 18px; padding: 28px; max-width: 420px; }
        h1 { font-family: 'Space Grotesk', sans-serif; font-size: 1.4em; color: #ef4444; margin-bottom: 14px; }
        ul { margin: 0 0 20px 20px; color: #ddd; font-size: 0.92em; line-height: 1.8; }
        .back { display: inline-block; background: #00ff88; color: #000; padding: 12px 26px; border-radius: 10px; font-weight: 700; text-decoration: none; }
    </style>
</head>
<body>
    <div class="err-card">
        <h1>&#9888; Please fix the following</h1>
        <ul>%s</ul>
        <a class="back" href="/">&#8592; Back to form</a>
    </div>
</body>
</html>
        """.formatted(items.toString());
        return html;
    }

    @GetMapping("/")
    public ResponseEntity<String> home() {
        String html = """
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>PLAGG - AI Recruiting Assistant</title>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=Space+Grotesk:wght@500;700&display=swap" rel="stylesheet">
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body { font-family: 'Inter', sans-serif; background: #0a0a0a; color: #fff; min-height: 100vh; display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 24px; }
        .logo-wrap { position: relative; display: inline-block; }
        .logo-icon { width: 56px; height: 56px; background: radial-gradient(circle at 30% 30%, #00ff88, #00aa55); border-radius: 16px; display: flex; align-items: center; justify-content: center; font-size: 28px; margin-bottom: 12px; box-shadow: 0 0 30px rgba(0,255,136,0.3); }
        .logo-dot { width: 10px; height: 10px; background: #00ff88; border-radius: 50%; position: absolute; top: -3px; right: -3px; box-shadow: 0 0 8px #00ff88; }
        h1 { font-family: 'Space Grotesk', sans-serif; font-size: 3.2em; font-weight: 700; letter-spacing: 0.08em; text-transform: uppercase; }
        .subtitle { font-size: 0.8em; color: #00ff88; letter-spacing: 0.2em; text-transform: uppercase; font-weight: 500; margin-bottom: 8px; }
        .tagline { color: #555; font-size: 0.9em; margin-bottom: 24px; }
        .nav-link { color: #00ff88; text-decoration: none; font-size: 0.85em; margin-bottom: 24px; display: inline-block; border: 1px solid rgba(0,255,136,0.25); padding: 8px 18px; border-radius: 20px; }
        .nav-link:hover { background: rgba(0,255,136,0.08); }
        .form-card { background: #141414; border: 1px solid #222; border-radius: 20px; padding: 32px; width: 100%; max-width: 440px; }
        .field-group { margin-bottom: 16px; }
        .field-label { display: block; font-size: 0.75em; color: #777; letter-spacing: 0.08em; text-transform: uppercase; margin-bottom: 8px; font-weight: 500; }
        .field-wrapper { display: flex; align-items: center; background: #1a1a1a; border: 1px solid #2a2a2a; border-radius: 10px; transition: border-color 0.2s; }
        .field-wrapper:focus-within { border-color: #00ff88; box-shadow: 0 0 0 3px rgba(0,255,136,0.08); }
        .field-icon { padding: 0 12px; font-size: 15px; opacity: 0.5; align-self: flex-start; margin-top: 13px; }
        input, select, textarea { background: transparent; border: none; color: #fff; font-family: 'Inter', sans-serif; font-size: 0.92em; padding: 13px 13px 13px 0; width: 100%; outline: none; resize: vertical; }
        textarea { min-height: 80px; }
        select { padding: 13px; cursor: pointer; }
        input::placeholder, textarea::placeholder { color: #3a3a3a; }
        select option { background: #1a1a1a; }
        .hint { color: #555; font-size: 0.7em; margin-top: 6px; }
        .submit-btn { width: 100%; background: #00ff88; color: #000; border: none; border-radius: 10px; padding: 15px; font-size: 0.95em; font-weight: 700; font-family: 'Inter', sans-serif; cursor: pointer; margin-top: 8px; letter-spacing: 0.04em; transition: all 0.2s; }
        .submit-btn:hover { background: #00ffaa; transform: translateY(-1px); box-shadow: 0 8px 24px rgba(0,255,136,0.2); }
    </style>
</head>
<body>
    <div style="display:flex;flex-direction:column;align-items:center;margin-bottom:8px;">
        <div class="logo-wrap">
            <div class="logo-icon">&#9889;</div>
            <div class="logo-dot"></div>
        </div>
        <h1>PLAGG</h1>
        <p class="subtitle">AI-Powered Recruiting Assistant</p>
    </div>
    <p class="tagline">Enter candidate details for AI evaluation</p>
    <a href="/leaderboard" class="nav-link">&#128101; View Leaderboard &#8594;</a>
    <div class="form-card">
        <form action="/score" method="post">
            <div class="field-group">
                <label class="field-label">Candidate Name</label>
                <div class="field-wrapper">
                    <span class="field-icon">&#128100;</span>
                    <input name="name" placeholder="e.g. Reshma K" required/>
                </div>
            </div>
            <div class="field-group">
                <label class="field-label">GitHub Username</label>
                <div class="field-wrapper">
                    <span class="field-icon">&#128031;</span>
                    <input name="github" placeholder="e.g. torvalds" required/>
                </div>
                <div class="hint">Must be a real, existing GitHub username.</div>
            </div>
            <div class="field-group">
                <label class="field-label">LeetCode Problems Solved</label>
                <div class="field-wrapper">
                    <span class="field-icon">&lt;/&gt;</span>
                    <input name="leetcode" type="number" min="0" max="5000" placeholder="e.g. 342" required/>
                </div>
            </div>
            <div class="field-group">
                <label class="field-label">LinkedIn Profile URL</label>
                <div class="field-wrapper">
                    <span class="field-icon">&#128188;</span>
                    <input name="linkedinUrl" type="text" placeholder="linkedin.com/in/you"/>
                </div>
            </div>
            <div class="field-group">
                <label class="field-label">Hackathons Participated</label>
                <div class="field-wrapper">
                    <span class="field-icon">&#127942;</span>
                    <input name="hackathons" type="number" min="0" max="50" placeholder="e.g. 7" required/>
                </div>
            </div>
            <div class="field-group">
                <label class="field-label">Resume Text (paste for AI screening)</label>
                <div class="field-wrapper">
                    <span class="field-icon">&#128196;</span>
                    <textarea name="resume" placeholder="Paste resume text here for AI keyword + quality screening..."></textarea>
                </div>
            </div>
            <button type="submit" class="submit-btn">&#9889; Evaluate Candidate</button>
        </form>
    </div>
</body>
</html>
        """;
        return ResponseEntity.ok().header("Content-Type", "text/html; charset=UTF-8").body(html);
    }

    @PostMapping("/score")
    public ResponseEntity<String> getScore(
        @RequestParam String name,
        @RequestParam String github,
        @RequestParam(defaultValue = "0") int leetcode,
        @RequestParam(defaultValue = "") String linkedinUrl,
        @RequestParam(defaultValue = "0") int hackathons,
        @RequestParam(defaultValue = "") String resume) {

        List<String> errors = new ArrayList<>();

        String trimmedName = name == null ? "" : name.trim();
        String trimmedGithub = github == null ? "" : github.trim();

        if (!trimmedName.matches("[A-Za-z][A-Za-z .]{1,49}")) {
            errors.add("Name must be 2-50 letters (letters, spaces, dots only).");
        }
        if (!trimmedGithub.matches("^[a-zA-Z0-9](?:[a-zA-Z0-9]|-(?=[a-zA-Z0-9])){0,38}$")) {
            errors.add("GitHub username format looks invalid.");
        }
        if (leetcode < 0 || leetcode > 5000) {
            errors.add("LeetCode problems solved must be between 0 and 5000.");
        }
        if (hackathons < 0 || hackathons > 50) {
            errors.add("Hackathons participated must be between 0 and 50.");
        }
        if (linkedinUrl != null && !linkedinUrl.isBlank() && !linkedinUrl.toLowerCase().contains("linkedin.com/in/")) {
            errors.add("LinkedIn URL should look like linkedin.com/in/yourname.");
        }

        int repos = 0, followers = 0;
        if (errors.isEmpty()) {
            try {
                RestTemplate rt = new RestTemplate();
                String ghBody = rt.getForObject("https://api.github.com/users/" + trimmedGithub, String.class);
                if (ghBody != null) {
                    Matcher rm = Pattern.compile("\"public_repos\":(\\d+)").matcher(ghBody);
                    if (rm.find()) repos = Integer.parseInt(rm.group(1));
                    Matcher fm = Pattern.compile("\"followers\":(\\d+)").matcher(ghBody);
                    if (fm.find()) followers = Integer.parseInt(fm.group(1));
                }
            } catch (HttpClientErrorException.NotFound nf) {
                errors.add("GitHub username '" + trimmedGithub + "' doesn't exist. Please check and re-enter.");
            } catch (Exception e) {
                repos = 1;
                followers = 1;
            }
        }

        if (!errors.isEmpty()) {
            return ResponseEntity.status(400).header("Content-Type", "text/html; charset=UTF-8").body(errorPage(errors));
        }

        int githubScore = 8;
        githubScore += Math.min(repos, 20) / 2;
        if (followers > 0) githubScore += 2;
        githubScore = Math.min(githubScore, 20);

        int leetcodeScore = Math.min((leetcode * 25) / 300, 25);
        boolean hasLinkedin = linkedinUrl != null && !linkedinUrl.isBlank();
        int linkedinScore = hasLinkedin ? 15 : 5;
        int hackathonScore = Math.min(hackathons * 2, 10);
        int resumeScore = computeResumeScore(resume);
        int aiBonus = (githubScore >= 12 && leetcodeScore >= 15) ? 10 : 5;
        int total = githubScore + leetcodeScore + linkedinScore + hackathonScore + resumeScore + aiBonus;

        String insight = generateInsight(githubScore, leetcodeScore, linkedinScore, hackathonScore, resumeScore, total);
        String resumeNote = resumeFeedback(resume, resumeScore);

        Candidate newCandidate = new Candidate(trimmedName, trimmedGithub, githubScore, leetcodeScore, linkedinScore, hackathonScore, resumeScore, aiBonus, insight);
        LEADERBOARD.add(newCandidate);

        List<Candidate> sortedAll = new ArrayList<>(LEADERBOARD);
        sortedAll.sort((a, b) -> b.total() - a.total());
        int rank = sortedAll.indexOf(newCandidate) + 1;
        int totalCandidates = sortedAll.size();

        StringBuilder top5 = new StringBuilder();
        int limit = Math.min(5, sortedAll.size());
        for (int i = 0; i < limit; i++) {
            Candidate c = sortedAll.get(i);
            String rowColor = i == 0 ? "#00ff88" : i == 1 ? "#a78bfa" : i == 2 ? "#fbbf24" : "#666";
            String highlight = c == newCandidate ? "background:rgba(0,255,136,0.06);" : "";
            top5.append("""
                <div class="top5-row" style="%s">
                    <span class="top5-rank" style="color:%s">#%d</span>
                    <span class="top5-name">%s</span>
                    <span class="top5-score">%d</span>
                </div>
                """.formatted(highlight, rowColor, i + 1, c.name, c.total()));
        }
        String top5Html = top5.toString();

        String status, statusColor, eligibility;
        if (total >= 50) { status = "QUALIFIED"; statusColor = "#7c3aed"; eligibility = "Interview Ready"; }
        else if (total >= 30) { status = "BORDERLINE"; statusColor = "#f59e0b"; eligibility = "Needs Review"; }
        else { status = "NOT QUALIFIED"; statusColor = "#ef4444"; eligibility = "Below Threshold"; }

        double circumference = 2 * Math.PI * 54;
        double dashOffset = circumference - (total / 100.0) * circumference;
        double ghPct = (githubScore / 20.0) * 100;
        double lcPct = (leetcodeScore / 25.0) * 100;
        double lnPct = (linkedinScore / 15.0) * 100;
        double hkPct = (hackathonScore / 10.0) * 100;
        double resPct = (resumeScore / 20.0) * 100;
        double aiPct = (aiBonus / 10.0) * 100;

        String html = """
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>PLAGG Report</title>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=Space+Grotesk:wght@500;700&display=swap" rel="stylesheet">
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body { font-family: 'Inter', sans-serif; background: #0a0a0a; color: #fff; min-height: 100vh; padding: 24px; }
        .top-bar { display: flex; align-items: center; justify-content: space-between; max-width: 420px; margin: 0 auto 20px; }
        .back-btn { color: #666; text-decoration: none; font-size: 0.88em; }
        .back-btn:hover { color: #00ff88; }
        .page-title { font-size: 0.72em; letter-spacing: 0.15em; text-transform: uppercase; color: #00ff88; font-weight: 600; }
        .board-link { color: #555; font-size: 0.82em; text-decoration: none; }
        .board-link:hover { color: #00ff88; }
        .card { background: #141414; border: 1px solid #1e1e1e; border-radius: 18px; padding: 22px; max-width: 420px; margin: 0 auto 14px; }
        .candidate-row { display: flex; align-items: center; gap: 14px; }
        .avatar { width: 44px; height: 44px; background: linear-gradient(135deg, #00ff88, #00aa55); border-radius: 12px; display: flex; align-items: center; justify-content: center; font-size: 1.3em; font-weight: 700; color: #000; flex-shrink: 0; }
        .candidate-name { font-weight: 600; font-size: 0.95em; }
        .candidate-handle { color: #444; font-size: 0.78em; margin-top: 2px; }
        .check { color: #00ff88; }
        .score-section { display: flex; align-items: center; gap: 20px; padding: 20px 0 8px; }
        .circular-score { position: relative; flex-shrink: 0; }
        .score-svg { transform: rotate(-90deg); }
        .score-track { fill: none; stroke: #1e1e1e; stroke-width: 8; }
        .score-fill { fill: none; stroke: #00ff88; stroke-width: 8; stroke-linecap: round; stroke-dasharray: %s; stroke-dashoffset: %s; }
        .score-text { position: absolute; top: 50%%; left: 50%%; transform: translate(-50%%, -50%%); text-align: center; }
        .score-number { font-family: 'Space Grotesk', sans-serif; font-size: 1.9em; font-weight: 700; line-height: 1; }
        .score-denom { font-size: 0.6em; color: #444; }
        .total-label { font-size: 0.7em; color: #444; letter-spacing: 0.1em; text-transform: uppercase; margin-bottom: 4px; }
        .status-badge { font-family: 'Space Grotesk', sans-serif; font-size: 1.5em; font-weight: 700; color: %s; }
        .eligibility-pill { display: inline-flex; align-items: center; gap: 5px; background: rgba(0,255,136,0.08); border: 1px solid rgba(0,255,136,0.15); border-radius: 20px; padding: 4px 12px; font-size: 0.75em; color: #00ff88; margin-top: 8px; }
        .rank-card { text-align: center; }
        .rank-label { font-size: 0.7em; color: #444; letter-spacing: 0.1em; text-transform: uppercase; margin-bottom: 6px; }
        .rank-value { font-family: 'Space Grotesk', sans-serif; font-size: 1.6em; font-weight: 700; color: #00ff88; }
        .breakdown-title { font-size: 0.7em; color: #444; letter-spacing: 0.1em; text-transform: uppercase; margin-bottom: 16px; }
        .bar-row { display: flex; align-items: center; margin-bottom: 14px; gap: 10px; }
        .bar-icon { font-size: 0.9em; width: 18px; }
        .bar-label { font-size: 0.85em; color: #bbb; flex: 1; }
        .bar-score { font-size: 0.8em; color: #666; min-width: 44px; text-align: right; }
        .bar-track { position: relative; height: 4px; background: #1e1e1e; border-radius: 2px; flex: 2; overflow: hidden; }
        .bar-fill { height: 100%%; border-radius: 2px; }
        .bg-purple { background: #a78bfa; }
        .bg-blue { background: #60a5fa; }
        .bg-green { background: #34d399; }
        .bg-orange { background: #fb923c; }
        .bg-pink { background: #f472b6; }
        .bg-yellow { background: #fbbf24; }
        .total-row { display: flex; justify-content: space-between; padding-top: 14px; border-top: 1px solid #1e1e1e; margin-top: 2px; }
        .total-big { font-family: 'Space Grotesk', sans-serif; font-size: 1.05em; font-weight: 700; color: #a78bfa; }
        .insight-card { background: linear-gradient(135deg, rgba(0,255,136,0.06), rgba(124,58,237,0.06)); border: 1px solid rgba(0,255,136,0.15); }
        .insight-title { font-size: 0.7em; color: #00ff88; letter-spacing: 0.1em; text-transform: uppercase; margin-bottom: 10px; font-weight: 600; }
        .insight-text { font-size: 0.9em; color: #ddd; line-height: 1.5; }
        .top5-card .breakdown-title { margin-bottom: 8px; }
        .top5-row { display: flex; align-items: center; gap: 10px; padding: 8px 6px; border-radius: 8px; font-size: 0.85em; }
        .top5-rank { font-family: 'Space Grotesk', sans-serif; font-weight: 700; width: 30px; }
        .top5-name { flex: 1; color: #ddd; }
        .top5-score { font-weight: 700; color: #00ff88; }
        .eval-btn { display: block; width: 100%%; max-width: 420px; margin: 0 auto; background: #00ff88; color: #000; border: none; border-radius: 12px; padding: 15px; font-size: 0.92em; font-weight: 700; font-family: 'Inter', sans-serif; cursor: pointer; text-decoration: none; text-align: center; transition: all 0.2s; }
        .eval-btn:hover { background: #00ffaa; }
    </style>
</head>
<body>
    <div class="top-bar">
        <a href="/" class="back-btn">&#8592; Back</a>
        <span class="page-title">Evaluation Report</span>
        <a href="/leaderboard" class="board-link">&#128101; Full Board</a>
    </div>
    <div class="card">
        <div class="candidate-row">
            <div class="avatar">%s</div>
            <div style="flex:1">
                <div class="candidate-name">%s</div>
                <div class="candidate-handle">@%s</div>
            </div>
            <span class="check">&#10003;</span>
        </div>
        <div class="score-section">
            <div class="circular-score">
                <svg class="score-svg" width="128" height="128" viewBox="0 0 128 128">
                    <circle class="score-track" cx="64" cy="64" r="54"/>
                    <circle class="score-fill" cx="64" cy="64" r="54"/>
                </svg>
                <div class="score-text">
                    <div class="score-number">%d</div>
                    <div class="score-denom">/100</div>
                </div>
            </div>
            <div>
                <div class="total-label">Total Score</div>
                <div class="status-badge">%s</div>
                <div><span class="eligibility-pill">&#10003; %s</span></div>
            </div>
        </div>
    </div>
    <div class="card rank-card">
        <div class="rank-label">Live Leaderboard Rank</div>
        <div class="rank-value">#%d <span style="color:#555;font-size:0.6em;">of %d candidates</span></div>
    </div>
    <div class="card">
        <div class="breakdown-title">&#9642; Score Breakdown</div>
        <div class="bar-row">
            <span class="bar-icon">&#128031;</span>
            <span class="bar-label">GitHub Activity</span>
            <div class="bar-track"><div class="bar-fill bg-purple" style="width:%.0f%%"></div></div>
            <span class="bar-score">%d /20</span>
        </div>
        <div class="bar-row">
            <span class="bar-icon">&lt;/&gt;</span>
            <span class="bar-label">Algorithmic Skills</span>
            <div class="bar-track"><div class="bar-fill bg-blue" style="width:%.0f%%"></div></div>
            <span class="bar-score">%d /25</span>
        </div>
        <div class="bar-row">
            <span class="bar-icon">&#128101;</span>
            <span class="bar-label">Professional Network</span>
            <div class="bar-track"><div class="bar-fill bg-green" style="width:%.0f%%"></div></div>
            <span class="bar-score">%d /15</span>
        </div>
        <div class="bar-row">
            <span class="bar-icon">&#127942;</span>
            <span class="bar-label">Hackathon Experience</span>
            <div class="bar-track"><div class="bar-fill bg-orange" style="width:%.0f%%"></div></div>
            <span class="bar-score">%d /10</span>
        </div>
        <div class="bar-row">
            <span class="bar-icon">&#128196;</span>
            <span class="bar-label">AI Resume Screening</span>
            <div class="bar-track"><div class="bar-fill bg-pink" style="width:%.0f%%"></div></div>
            <span class="bar-score">%d /20</span>
        </div>
        <div class="bar-row">
            <span class="bar-icon">&#9889;</span>
            <span class="bar-label">AI Composite Bonus</span>
            <div class="bar-track"><div class="bar-fill bg-yellow" style="width:%.0f%%"></div></div>
            <span class="bar-score">%d /10</span>
        </div>
        <div class="total-row">
            <span style="font-size:0.88em;color:#666;text-transform:uppercase;letter-spacing:0.06em;">Total</span>
            <span class="total-big">%d/100</span>
        </div>
    </div>
    <div class="card">
        <div class="breakdown-title">&#128196; Resume Screening Note</div>
        <div class="insight-text">%s</div>
    </div>
    <div class="card insight-card">
        <div class="insight-title">&#10024; AI Insight</div>
        <div class="insight-text">%s</div>
    </div>
    <div class="card top5-card">
        <div class="breakdown-title">&#127942; Top 5 Right Now</div>
        %s
    </div>
    <a href="/" class="eval-btn">&#8592; Evaluate Another Candidate</a>
</body>
</html>
        """.formatted(
            circumference, dashOffset, statusColor,
            github.substring(0, 1).toUpperCase(), trimmedName, trimmedGithub,
            total, status, eligibility,
            rank, totalCandidates,
            ghPct, githubScore,
            lcPct, leetcodeScore,
            lnPct, linkedinScore,
            hkPct, hackathonScore,
            resPct, resumeScore,
            aiPct, aiBonus,
            total,
            resumeNote,
            insight,
            top5Html
        );
        return ResponseEntity.ok().header("Content-Type", "text/html; charset=UTF-8").body(html);
    }

    @GetMapping("/leaderboard")
    public ResponseEntity<String> leaderboard() {
        List<Candidate> sorted = new ArrayList<>(LEADERBOARD);
        sorted.sort((a, b) -> b.total() - a.total());

        StringBuilder rows = new StringBuilder();
        int rank = 1;
        for (Candidate c : sorted) {
            String rowColor = rank == 1 ? "#00ff88" : rank == 2 ? "#a78bfa" : rank == 3 ? "#fbbf24" : "#666";
            rows.append("""
                <div class="lb-row">
                    <div class="lb-rank" style="color:%s">#%d</div>
                    <div class="lb-avatar">%s</div>
                    <div class="lb-info">
                        <div class="lb-name">%s</div>
                        <div class="lb-handle">@%s</div>
                    </div>
                    <div class="lb-score">
                        <div class="lb-total">%d</div>
                        <div class="lb-denom">/100</div>
                    </div>
                </div>
                """.formatted(rowColor, rank, c.name.substring(0, 1).toUpperCase(), c.name, c.github, c.total()));
            rank++;
        }

        String html = """
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>PLAGG - Live Leaderboard</title>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=Space+Grotesk:wght@500;700&display=swap" rel="stylesheet">
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body { font-family: 'Inter', sans-serif; background: #0a0a0a; color: #fff; min-height: 100vh; padding: 24px; }
        .top-bar { display: flex; align-items: center; justify-content: space-between; max-width: 480px; margin: 0 auto 20px; }
        .back-btn { color: #666; text-decoration: none; font-size: 0.88em; }
        .back-btn:hover { color: #00ff88; }
        .page-title { font-size: 0.72em; letter-spacing: 0.15em; text-transform: uppercase; color: #00ff88; font-weight: 600; }
        h1 { font-family: 'Space Grotesk', sans-serif; font-size: 1.6em; text-align: center; max-width: 480px; margin: 0 auto 4px; }
        .sub { text-align: center; color: #555; font-size: 0.85em; max-width: 480px; margin: 0 auto 24px; }
        .lb-list { max-width: 480px; margin: 0 auto; background: #141414; border: 1px solid #1e1e1e; border-radius: 18px; padding: 8px; }
        .lb-row { display: flex; align-items: center; gap: 14px; padding: 14px 12px; border-bottom: 1px solid #1c1c1c; }
        .lb-row:last-child { border-bottom: none; }
        .lb-rank { font-family: 'Space Grotesk', sans-serif; font-weight: 700; font-size: 0.95em; width: 32px; }
        .lb-avatar { width: 38px; height: 38px; background: linear-gradient(135deg, #00ff88, #00aa55); border-radius: 10px; display: flex; align-items: center; justify-content: center; font-weight: 700; color: #000; flex-shrink: 0; }
        .lb-info { flex: 1; }
        .lb-name { font-weight: 600; font-size: 0.9em; }
        .lb-handle { color: #444; font-size: 0.75em; }
        .lb-score { text-align: right; }
        .lb-total { font-family: 'Space Grotesk', sans-serif; font-weight: 700; font-size: 1.1em; color: #00ff88; }
        .lb-denom { font-size: 0.65em; color: #444; }
        .eval-btn { display: block; width: 100%%; max-width: 480px; margin: 20px auto 0; background: #00ff88; color: #000; border: none; border-radius: 12px; padding: 15px; font-size: 0.92em; font-weight: 700; font-family: 'Inter', sans-serif; cursor: pointer; text-decoration: none; text-align: center; }
        .eval-btn:hover { background: #00ffaa; }
    </style>
</head>
<body>
    <div class="top-bar">
        <a href="/" class="back-btn">&#8592; Home</a>
        <span class="page-title">Live Leaderboard</span>
        <span></span>
    </div>
    <h1>&#127942; Candidate Rankings</h1>
    <p class="sub">%d candidates evaluated &middot; ranked by AI composite score</p>
    <div class="lb-list">
        %s
    </div>
    <a href="/" class="eval-btn">&#9889; Evaluate a New Candidate</a>
</body>
</html>
        """.formatted(sorted.size(), rows.toString());
        return ResponseEntity.ok().header("Content-Type", "text/html; charset=UTF-8").body(html);
    }
}
