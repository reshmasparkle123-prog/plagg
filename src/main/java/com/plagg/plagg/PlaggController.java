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
        .tagline { color: #555; font-size: 0.9em; margin-bottom: 36px; }
        .form-card { background: #141414; border: 1px solid #222; border-radius: 20px; padding: 32px; width: 100%; max-width: 420px; }
        .field-group { margin-bottom: 16px; }
        .field-label { display: block; font-size: 0.75em; color: #777; letter-spacing: 0.08em; text-transform: uppercase; margin-bottom: 8px; font-weight: 500; }
        .field-wrapper { display: flex; align-items: center; background: #1a1a1a; border: 1px solid #2a2a2a; border-radius: 10px; transition: border-color 0.2s; }
        .field-wrapper:focus-within { border-color: #00ff88; box-shadow: 0 0 0 3px rgba(0,255,136,0.08); }
        .field-icon { padding: 0 12px; font-size: 15px; opacity: 0.5; }
        input, select { background: transparent; border: none; color: #fff; font-family: 'Inter', sans-serif; font-size: 0.92em; padding: 13px 13px 13px 0; width: 100%; outline: none; }
        select { padding: 13px; cursor: pointer; }
        input::placeholder { color: #3a3a3a; }
        select option { background: #1a1a1a; }
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
    <div class="form-card">
        <form action="/score" method="get">
            <div class="field-group">
                <label class="field-label">GitHub Username</label>
                <div class="field-wrapper">
                    <span class="field-icon">&#128031;</span>
                    <input name="github" placeholder="e.g. torvalds" required/>
                </div>
            </div>
            <div class="field-group">
                <label class="field-label">LeetCode Problems Solved</label>
                <div class="field-wrapper">
                    <span class="field-icon">&lt;/&gt;</span>
                    <input name="leetcode" type="number" placeholder="e.g. 342" required/>
                </div>
            </div>
            <div class="field-group">
                <label class="field-label">Candidate Name</label>
                <div class="field-wrapper">
                    <span class="field-icon">&#128100;</span>
                    <input name="name" placeholder="e.g. Reshma K" required/>
                </div>
            </div>
            <div class="field-group">
                <label class="field-label">Hackathons Participated</label>
                <div class="field-wrapper">
                    <span class="field-icon">&#127942;</span>
                    <input name="hackathons" type="number" placeholder="e.g. 7" required/>
                </div>
            </div>
            <input type="hidden" name="linkedin" value="yes"/>
            <button type="submit" class="submit-btn">&#9889; Evaluate Candidate</button>
        </form>
    </div>
</body>
</html>
        """;
        return ResponseEntity.ok().header("Content-Type", "text/html; charset=UTF-8").body(html);
    }

    @GetMapping("/score")
    public ResponseEntity<String> getScore(
        @RequestParam String name,
        @RequestParam String github,
        @RequestParam(defaultValue = "0") int leetcode,
        @RequestParam(defaultValue = "yes") String linkedin,
        @RequestParam(defaultValue = "0") int hackathons) {

        int githubScore = 10;
        try {
            RestTemplate rt = new RestTemplate();
            String ghBody = rt.getForObject("https://api.github.com/users/" + github, String.class);
            if (ghBody != null) {
                if(ghBody.contains("\"public_repos\":2")) githubScore += 5;
                if(ghBody.contains("\"public_repos\":3")) githubScore += 8;
                if(ghBody.contains("\"public_repos\":4")) githubScore += 10;
                if(!ghBody.contains("\"followers\":0")) githubScore += 5;
            }
        } catch(Exception e) { githubScore = 15; }
        githubScore = Math.min(githubScore, 25);

        int leetcodeScore = Math.min(leetcode / 10, 30);
        int linkedinScore = linkedin.equals("yes") ? 20 : 6;
        int hackathonScore = Math.min(hackathons * 3, 15);
        int aiBonus = (githubScore > 15 && leetcodeScore > 15) ? 10 : 5;
        int total = githubScore + leetcodeScore + linkedinScore + hackathonScore + aiBonus;

        String status, statusColor, eligibility;
        if(total >= 50) { status = "QUALIFIED"; statusColor = "#7c3aed"; eligibility = "Interview Ready"; }
        else if(total >= 30) { status = "BORDERLINE"; statusColor = "#f59e0b"; eligibility = "Needs Review"; }
        else { status = "NOT QUALIFIED"; statusColor = "#ef4444"; eligibility = "Below Threshold"; }

        double circumference = 2 * Math.PI * 54;
        double dashOffset = circumference - (total / 100.0) * circumference;
        double ghPct = (githubScore / 25.0) * 100;
        double lcPct = (leetcodeScore / 30.0) * 100;
        double lnPct = (linkedinScore / 20.0) * 100;
        double hkPct = (hackathonScore / 15.0) * 100;
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
        .bg-yellow { background: #fbbf24; }
        .total-row { display: flex; justify-content: space-between; padding-top: 14px; border-top: 1px solid #1e1e1e; margin-top: 2px; }
        .total-big { font-family: 'Space Grotesk', sans-serif; font-size: 1.05em; font-weight: 700; color: #a78bfa; }
        .eval-btn { display: block; width: 100%%; max-width: 420px; margin: 0 auto; background: #00ff88; color: #000; border: none; border-radius: 12px; padding: 15px; font-size: 0.92em; font-weight: 700; font-family: 'Inter', sans-serif; cursor: pointer; text-decoration: none; text-align: center; transition: all 0.2s; }
        .eval-btn:hover { background: #00ffaa; }
    </style>
</head>
<body>
    <div class="top-bar">
        <a href="/" class="back-btn">&#8592; Back</a>
        <span class="page-title">Evaluation Report</span>
        <span style="color:#555;font-size:0.82em;">&#128101; Board</span>
    </div>
    <div class="card">
        <div class="candidate-row">
            <div class="avatar">%s</div>
            <div style="flex:1">
                <div class="candidate-name">@%s</div>
                <div class="candidate-handle">%s</div>
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
    <div class="card">
        <div class="breakdown-title">&#9642; Score Breakdown</div>
        <div class="bar-row">
            <span class="bar-icon">&#128031;</span>
            <span class="bar-label">GitHub Activity</span>
            <div class="bar-track"><div class="bar-fill bg-purple" style="width:%.0f%%"></div></div>
            <span class="bar-score">%d /25</span>
        </div>
        <div class="bar-row">
            <span class="bar-icon">&lt;/&gt;</span>
            <span class="bar-label">Algorithmic Skills</span>
            <div class="bar-track"><div class="bar-fill bg-blue" style="width:%.0f%%"></div></div>
            <span class="bar-score">%d /30</span>
        </div>
        <div class="bar-row">
            <span class="bar-icon">&#128101;</span>
            <span class="bar-label">Professional Network</span>
            <div class="bar-track"><div class="bar-fill bg-green" style="width:%.0f%%"></div></div>
            <span class="bar-score">%d /20</span>
        </div>
        <div class="bar-row">
            <span class="bar-icon">&#127942;</span>
            <span class="bar-label">Hackathon Experience</span>
            <div class="bar-track"><div class="bar-fill bg-orange" style="width:%.0f%%"></div></div>
            <span class="bar-score">%d /15</span>
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
    <a href="/" class="eval-btn">&#8592; Evaluate Another Candidate</a>
</body>
</html>
        """.formatted(
            circumference, dashOffset, statusColor,
            github.substring(0,1).toUpperCase(), github, name,
            total, status, eligibility,
            ghPct, githubScore,
            lcPct, leetcodeScore,
            lnPct, linkedinScore,
            hkPct, hackathonScore,
            aiPct, aiBonus,
            total
        );
        return ResponseEntity.ok().header("Content-Type", "text/html; charset=UTF-8").body(html);
    }
}
