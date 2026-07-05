package com.plagg.plagg;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.ByteArrayOutputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
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

    static class ResumeAnalysis {
        int score;
        List<String> strengths;
        List<String> improvements;

        ResumeAnalysis(int score, List<String> strengths, List<String> improvements) {
            this.score = score;
            this.strengths = strengths;
            this.improvements = improvements;
        }
    }

    static final String[] RESUME_KEYWORDS = {
        "java", "python", "javascript", "typescript", "react", "node", "sql", "aws",
        "docker", "kubernetes", "machine learning", "spring", "git", "api", "cloud",
        "c++", "html", "css", "agile", "leadership", "project", "internship",
        "hackathon", "open source", "data structures", "algorithms"
    };

    static final String[] ACTION_VERBS = {
        "built", "led", "developed", "designed", "improved", "created", "implemented",
        "managed", "optimized", "launched", "architected", "deployed", "automated",
        "increased", "reduced", "collaborated", "mentored"
    };

    static final String MASCOT_SVG = """
        <svg viewBox="0 0 100 100" xmlns="http://www.w3.org/2000/svg">
            <ellipse cx="50" cy="58" rx="34" ry="30" fill="#1a1a1a"/>
            <path d="M 20 40 L 12 14 L 38 32 Z" fill="#1a1a1a"/>
            <path d="M 80 40 L 88 14 L 62 32 Z" fill="#1a1a1a"/>
            <path d="M 22 38 L 18 22 L 34 33 Z" fill="#00ff88" opacity="0.55"/>
            <path d="M 78 38 L 82 22 L 66 33 Z" fill="#00ff88" opacity="0.55"/>
            <ellipse cx="36" cy="55" rx="7" ry="9" fill="#00ff88"/>
            <ellipse cx="64" cy="55" rx="7" ry="9" fill="#00ff88"/>
            <ellipse cx="36" cy="58" rx="2.6" ry="4.2" fill="#031"/>
            <ellipse cx="64" cy="58" rx="2.6" ry="4.2" fill="#031"/>
            <path d="M 46 68 Q 50 72 54 68" stroke="#00ff88" stroke-width="2" fill="none" stroke-linecap="round"/>
            <path d="M 50 64 L 46 68 M 50 64 L 54 68" stroke="#00ff88" stroke-width="1.6" fill="none" stroke-linecap="round"/>
            <line x1="8" y1="60" x2="26" y2="58" stroke="#333" stroke-width="1.5" stroke-linecap="round"/>
            <line x1="8" y1="68" x2="26" y2="66" stroke="#333" stroke-width="1.5" stroke-linecap="round"/>
            <line x1="92" y1="60" x2="74" y2="58" stroke="#333" stroke-width="1.5" stroke-linecap="round"/>
            <line x1="92" y1="68" x2="74" y2="66" stroke="#333" stroke-width="1.5" stroke-linecap="round"/>
        </svg>
        """;

    static final String MASCOT_CSS = """
        .mascot-row { display: flex; align-items: flex-end; gap: 10px; max-width: 420px; margin: 0 auto 14px; }
        .mascot-icon { width: 46px; height: 46px; flex-shrink: 0; background: #141414; border: 1px solid #222; border-radius: 50%; padding: 6px; cursor: pointer; transition: transform 0.15s, border-color 0.15s; }
        .mascot-icon:hover { transform: scale(1.08); border-color: #00ff88; }
        .mascot-icon.speaking { animation: mascotBounce 0.5s ease-in-out infinite; border-color: #00ff88; }
        @keyframes mascotBounce { 0%, 100% { transform: translateY(0) scale(1.05); } 50% { transform: translateY(-6px) scale(1.12); } }
        .mascot-icon svg { width: 100%; height: 100%; display: block; pointer-events: none; }
        .mascot-bubble { background: #141414; border: 1px solid #222; border-radius: 14px 14px 14px 2px; padding: 10px 14px; font-size: 0.82em; color: #ccc; line-height: 1.4; position: relative; }
        .mascot-bubble strong { color: #00ff88; }
        .mascot-hint { font-size: 0.62em; color: #444; margin-top: 3px; }
        """;

    static final String MASCOT_JS = """
        <script>
        function plaggSpeak(el) {
            try {
                var msg = el.getAttribute('data-message');
                if (!msg || !('speechSynthesis' in window)) return;
                window.speechSynthesis.cancel();
                var utter = new SpeechSynthesisUtterance(msg);
                utter.rate = 1.0;
                utter.pitch = 1.15;
                el.classList.add('speaking');
                utter.onend = function() { el.classList.remove('speaking'); };
                utter.onerror = function() { el.classList.remove('speaking'); };
                window.speechSynthesis.speak(utter);
            } catch (e) {}
        }
        </script>
        """;

    static String escapeForAttr(String s) {
        return s.replace("&", "&amp;").replace("\"", "&quot;");
    }

    static String mascotBlock(String message) {
        String safeAttr = escapeForAttr(message);
        return """
            <div class="mascot-row">
                <div class="mascot-icon" onclick="plaggSpeak(this)" data-message="%s" title="Click to hear Plagg!">%s</div>
                <div>
                    <div class="mascot-bubble"><strong>Plagg:</strong> %s</div>
                    <div class="mascot-hint">&#128266; tap Plagg to hear this</div>
                </div>
            </div>
            """.formatted(safeAttr, MASCOT_SVG, message);
    }

    static String mascotMessageForScore(int total) {
        if (total >= 70) return "Whoa, you're crushing it! Fast-tracking you to the top.";
        if (total >= 50) return "Solid profile! You're in good shape for an interview.";
        if (total >= 30) return "Getting there! A bit more polish and you'll qualify easily.";
        return "Early days, but everyone starts somewhere. Keep building!";
    }

    // Fallback: keyword-based heuristic, used if Groq API key missing or call fails
    static ResumeAnalysis analyzeResumeFallback(String resume) {
        List<String> strengths = new ArrayList<>();
        List<String> improvements = new ArrayList<>();

        if (resume == null || resume.isBlank()) {
            improvements.add("No resume was submitted - add one next time so PLAGG can properly screen your background and boost your Resume Screening + AI Composite scores.");
            return new ResumeAnalysis(4, strengths, improvements);
        }

        String text = resume.toLowerCase();
        int score = 5;

        if (resume.trim().length() < 50) {
            improvements.add("Resume text is very short - paste a fuller version for a more accurate screening.");
        }

        int matches = 0;
        for (String k : RESUME_KEYWORDS) {
            if (text.contains(k)) matches++;
        }
        score += Math.min(matches, 10);
        if (matches >= 5) {
            strengths.add("Strong technical keyword coverage (" + matches + " relevant skills detected).");
        } else if (matches > 0) {
            improvements.add("Only " + matches + " technical keyword(s) found - list more specific skills, languages, or tools.");
        } else {
            improvements.add("No recognizable technical keywords found - add specific skills, languages, or tools you've used.");
        }

        boolean hasQuantified = text.matches("(?s).*\\d+%.*") || text.matches("(?s).*\\b\\d{2,}\\+.*");
        if (hasQuantified) {
            score += 3;
            strengths.add("Includes quantified achievements (numbers/percentages) - shows measurable impact.");
        } else {
            improvements.add("No quantified achievements found - add numbers, e.g. \"improved performance by 30%\" or \"led a team of 5\".");
        }

        boolean hasActionVerb = false;
        for (String v : ACTION_VERBS) {
            if (text.contains(v)) { hasActionVerb = true; break; }
        }
        if (hasActionVerb) {
            strengths.add("Uses strong action verbs to describe experience.");
        } else {
            improvements.add("Start bullet points with action verbs like \"built\", \"led\", \"designed\", or \"improved\".");
        }

        if (resume.trim().length() > 150) {
            score += 2;
            strengths.add("Good level of detail in the resume text.");
        }

        score = Math.min(score, 20);
        if (strengths.isEmpty()) strengths.add("Resume submitted and reviewed.");
        if (improvements.isEmpty()) improvements.add("No major issues found - resume looks solid.");
        return new ResumeAnalysis(score, strengths, improvements);
    }

    // Primary: real AI analysis via Groq. Falls back to keyword heuristic on any failure.
    static ResumeAnalysis analyzeResumeWithAI(String resume) {
        if (resume == null || resume.isBlank()) {
            List<String> improvements = new ArrayList<>();
            improvements.add("No resume was submitted - add one next time so PLAGG's AI can properly screen your background.");
            return new ResumeAnalysis(4, new ArrayList<>(), improvements);
        }

        String apiKey = System.getenv("GROQ_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            return analyzeResumeFallback(resume);
        }

        try {
            ObjectMapper mapper = new ObjectMapper();

            String systemPrompt = "You are an expert technical recruiter AI screening candidate resumes for a hackathon recruiting tool. "
                + "Respond with ONLY valid JSON, no markdown fences, no explanation, in exactly this format: "
                + "{\"score\": <integer 0-20>, \"strengths\": [\"...\", \"...\"], \"improvements\": [\"...\", \"...\"]}. "
                + "The score reflects overall resume quality out of 20 points. "
                + "List 2-4 concise, specific strengths and 2-4 concise, specific improvements (real mistakes or missing elements) based only on the actual resume content provided.";

            List<Map<String, String>> messages = new ArrayList<>();
            Map<String, String> sysMsg = new LinkedHashMap<>();
            sysMsg.put("role", "system");
            sysMsg.put("content", systemPrompt);
            Map<String, String> userMsg = new LinkedHashMap<>();
            userMsg.put("role", "user");
            userMsg.put("content", "Resume:\n" + resume);
            messages.add(sysMsg);
            messages.add(userMsg);

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("model", "llama-3.3-70b-versatile");
            body.put("messages", messages);
            body.put("temperature", 0.3);
            body.put("max_tokens", 500);

            String jsonBody = mapper.writeValueAsString(body);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);
            HttpEntity<String> request = new HttpEntity<>(jsonBody, headers);

            RestTemplate rt = new RestTemplate();
            String response = rt.postForObject("https://api.groq.com/openai/v1/chat/completions", request, String.class);

            JsonNode root = mapper.readTree(response);
            String content = root.path("choices").get(0).path("message").path("content").asText();

            content = content.trim();
            if (content.startsWith("```")) {
                content = content.replaceAll("^```json", "").replaceAll("^```", "").replaceAll("```$", "").trim();
            }

            JsonNode parsed = mapper.readTree(content);
            int score = parsed.path("score").asInt(10);
            score = Math.max(0, Math.min(score, 20));

            List<String> strengths = new ArrayList<>();
            for (JsonNode n : parsed.path("strengths")) strengths.add(n.asText());
            List<String> improvements = new ArrayList<>();
            for (JsonNode n : parsed.path("improvements")) improvements.add(n.asText());

            if (strengths.isEmpty()) strengths.add("Resume reviewed by AI - looks reasonable overall.");
            if (improvements.isEmpty()) improvements.add("No major issues found by AI screening.");

            return new ResumeAnalysis(score, strengths, improvements);
        } catch (Exception e) {
            return analyzeResumeFallback(resume);
        }
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

    static String getLocalIp() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                if (iface.isLoopback() || !iface.isUp()) continue;
                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (addr.isSiteLocalAddress() && addr.getHostAddress().indexOf(':') == -1) {
                        return addr.getHostAddress();
                    }
                }
            }
        } catch (Exception e) {
            // fall through
        }
        return "localhost";
    }

    static String generateQrBase64(String text) {
        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(text, BarcodeFormat.QR_CODE, 260, 260);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", baos);
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (WriterException | java.io.IOException e) {
            return "";
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
        %s
        .err-card { background: #141414; border: 1px solid #3a1414; border-radius: 18px; padding: 28px; max-width: 420px; }
        h1 { font-family: 'Space Grotesk', sans-serif; font-size: 1.4em; color: #ef4444; margin-bottom: 14px; }
        ul { margin: 0 0 20px 20px; color: #ddd; font-size: 0.92em; line-height: 1.8; }
        .back { display: inline-block; background: #00ff88; color: #000; padding: 12px 26px; border-radius: 10px; font-weight: 700; text-decoration: none; }
    </style>
</head>
<body>
    %s
    <div class="err-card">
        <h1>&#9888; Please fix the following</h1>
        <ul>%s</ul>
        <a class="back" href="/">&#8592; Back to form</a>
    </div>
    %s
</body>
</html>
        """.formatted(MASCOT_CSS, mascotBlock("Oops! Let's fix these together before we continue."), items.toString(), MASCOT_JS);
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
        %s
        .logo-wrap { position: relative; display: inline-block; }
        .logo-icon { width: 56px; height: 56px; background: radial-gradient(circle at 30%% 30%%, #00ff88, #00aa55); border-radius: 16px; display: flex; align-items: center; justify-content: center; font-size: 28px; margin-bottom: 12px; box-shadow: 0 0 30px rgba(0,255,136,0.3); }
        .logo-dot { width: 10px; height: 10px; background: #00ff88; border-radius: 50%%; position: absolute; top: -3px; right: -3px; box-shadow: 0 0 8px #00ff88; }
        h1 { font-family: 'Space Grotesk', sans-serif; font-size: 3.2em; font-weight: 700; letter-spacing: 0.08em; text-transform: uppercase; }
        .subtitle { font-size: 0.8em; color: #00ff88; letter-spacing: 0.2em; text-transform: uppercase; font-weight: 500; margin-bottom: 8px; }
        .tagline { color: #555; font-size: 0.9em; margin-bottom: 24px; }
        .nav-link { color: #00ff88; text-decoration: none; font-size: 0.85em; margin-bottom: 12px; display: inline-block; border: 1px solid rgba(0,255,136,0.25); padding: 8px 18px; border-radius: 20px; }
        .nav-link:hover { background: rgba(0,255,136,0.08); }
        .nav-row { display: flex; gap: 10px; margin-bottom: 24px; }
        .form-card { background: #141414; border: 1px solid #222; border-radius: 20px; padding: 32px; width: 100%%; max-width: 440px; }
        .field-group { margin-bottom: 16px; }
        .field-label { display: block; font-size: 0.75em; color: #777; letter-spacing: 0.08em; text-transform: uppercase; margin-bottom: 8px; font-weight: 500; }
        .field-wrapper { display: flex; align-items: center; background: #1a1a1a; border: 1px solid #2a2a2a; border-radius: 10px; transition: border-color 0.2s; }
        .field-wrapper:focus-within { border-color: #00ff88; box-shadow: 0 0 0 3px rgba(0,255,136,0.08); }
        .field-icon { padding: 0 12px; font-size: 15px; opacity: 0.5; align-self: flex-start; margin-top: 13px; }
        input, select, textarea { background: transparent; border: none; color: #fff; font-family: 'Inter', sans-serif; font-size: 0.92em; padding: 13px 13px 13px 0; width: 100%%; outline: none; resize: vertical; }
        textarea { min-height: 80px; }
        select { padding: 13px; cursor: pointer; }
        input::placeholder, textarea::placeholder { color: #3a3a3a; }
        select option { background: #1a1a1a; }
        .hint { color: #555; font-size: 0.7em; margin-top: 6px; }
        .submit-btn { width: 100%%; background: #00ff88; color: #000; border: none; border-radius: 10px; padding: 15px; font-size: 0.95em; font-weight: 700; font-family: 'Inter', sans-serif; cursor: pointer; margin-top: 8px; letter-spacing: 0.04em; transition: all 0.2s; }
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
    %s
    <div class="nav-row">
        <a href="/leaderboard" class="nav-link">&#128101; Leaderboard</a>
        <a href="/qr" class="nav-link">&#128241; Scan QR</a>
    </div>
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
                <label class="field-label">Resume Text (optional, for AI screening)</label>
                <div class="field-wrapper">
                    <span class="field-icon">&#128196;</span>
                    <textarea name="resume" placeholder="Paste resume text here for AI screening (optional)..."></textarea>
                </div>
                <div class="hint">Optional, but PLAGG's AI will flag it and score lower if skipped.</div>
            </div>
            <button type="submit" class="submit-btn">&#9889; Evaluate Candidate</button>
        </form>
    </div>
    %s
</body>
</html>
        """.formatted(MASCOT_CSS, mascotBlock("Hi, I'm Plagg! Fill this out and I'll evaluate your profile."), MASCOT_JS);
        return ResponseEntity.ok().header("Content-Type", "text/html; charset=UTF-8").body(html);
    }

    @GetMapping("/qr")
    public ResponseEntity<String> qrCode() {
        String ip = getLocalIp();
        String targetUrl = "http://" + ip + ":8080/";
        String qrBase64 = generateQrBase64(targetUrl);
        String qrImgSrc = "data:image/png;base64," + qrBase64;

        String html = """
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>PLAGG - Scan to Enter</title>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=Space+Grotesk:wght@500;700&display=swap" rel="stylesheet">
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body { font-family: 'Inter', sans-serif; background: #0a0a0a; color: #fff; min-height: 100vh; display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 24px; text-align: center; }
        %s
        h1 { font-family: 'Space Grotesk', sans-serif; font-size: 1.8em; margin-bottom: 6px; letter-spacing: 0.04em; }
        .sub { color: #666; font-size: 0.9em; margin-bottom: 20px; }
        .qr-card { background: #141414; border: 1px solid #222; border-radius: 20px; padding: 28px; }
        .qr-card img { border-radius: 10px; background: #fff; padding: 10px; }
        .url-hint { margin-top: 18px; color: #00ff88; font-size: 0.85em; word-break: break-all; }
        .board-link { margin-top: 24px; color: #555; font-size: 0.85em; text-decoration: none; border: 1px solid #222; padding: 8px 18px; border-radius: 20px; }
        .board-link:hover { color: #00ff88; }
    </style>
</head>
<body>
    <h1>&#9889; Scan to Evaluate</h1>
    <p class="sub">Point your phone camera at the QR code below</p>
    %s
    <div class="qr-card">
        <img src="%s" width="260" height="260" alt="QR Code"/>
        <div class="url-hint">%s</div>
    </div>
    <a href="/leaderboard" class="board-link">&#128101; View Live Leaderboard &#8594;</a>
    %s
</body>
</html>
        """.formatted(MASCOT_CSS, mascotBlock("Scan me and bring your friends along!"), qrImgSrc, targetUrl, MASCOT_JS);
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
        String trimmedResume = resume == null ? "" : resume.trim();

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
                HttpHeaders ghHeaders = new HttpHeaders();
                String ghToken = System.getenv("GITHUB_TOKEN");
                if (ghToken != null && !ghToken.isBlank()) {
                    ghHeaders.setBearerAuth(ghToken);
                }
                HttpEntity<Void> ghRequest = new HttpEntity<>(ghHeaders);
                ResponseEntity<String> ghResponse = rt.exchange(
                    "https://api.github.com/users/" + trimmedGithub,
                    HttpMethod.GET,
                    ghRequest,
                    String.class
                );
                String ghBody = ghResponse.getBody();
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

        ResumeAnalysis resumeAnalysis = analyzeResumeWithAI(trimmedResume);
        int resumeScore = resumeAnalysis.score;

        int aiBonus = (githubScore >= 12 && leetcodeScore >= 15) ? 10 : 5;
        int total = githubScore + leetcodeScore + linkedinScore + hackathonScore + resumeScore + aiBonus;

        String insight = generateInsight(githubScore, leetcodeScore, linkedinScore, hackathonScore, resumeScore, total);

        StringBuilder resumeFeedbackHtml = new StringBuilder();
        if (!resumeAnalysis.strengths.isEmpty()) {
            resumeFeedbackHtml.append("<div class=\"rf-block\"><div class=\"rf-title rf-good\">&#10003; Strengths</div><ul class=\"rf-list\">");
            for (String s : resumeAnalysis.strengths) {
                resumeFeedbackHtml.append("<li>").append(s).append("</li>");
            }
            resumeFeedbackHtml.append("</ul></div>");
        }
        resumeFeedbackHtml.append("<div class=\"rf-block\"><div class=\"rf-title rf-warn\">&#9888; Areas to Improve</div><ul class=\"rf-list\">");
        for (String s : resumeAnalysis.improvements) {
            resumeFeedbackHtml.append("<li>").append(s).append("</li>");
        }
        resumeFeedbackHtml.append("</ul></div>");

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
        %s
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
        .rf-block { margin-bottom: 14px; }
        .rf-block:last-child { margin-bottom: 0; }
        .rf-title { font-size: 0.75em; letter-spacing: 0.06em; text-transform: uppercase; font-weight: 700; margin-bottom: 8px; }
        .rf-good { color: #00ff88; }
        .rf-warn { color: #fbbf24; }
        .rf-list { margin: 0 0 0 18px; color: #ccc; font-size: 0.85em; line-height: 1.7; }
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
    %s
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
        <div class="breakdown-title">&#128196; AI Resume Screening Feedback</div>
        %s
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
    %s
</body>
</html>
        """.formatted(
            MASCOT_CSS,
            circumference, dashOffset, statusColor,
            mascotBlock(mascotMessageForScore(total)),
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
            resumeFeedbackHtml.toString(),
            insight,
            top5Html,
            MASCOT_JS
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
    <meta http-equiv="refresh" content="5">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>PLAGG - Live Leaderboard</title>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=Space+Grotesk:wght@500;700&display=swap" rel="stylesheet">
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body { font-family: 'Inter', sans-serif; background: #0a0a0a; color: #fff; min-height: 100vh; padding: 24px; }
        %s
        .mascot-row { max-width: 480px; }
        .top-bar { display: flex; align-items: center; justify-content: space-between; max-width: 480px; margin: 0 auto 20px; }
        .back-btn { color: #666; text-decoration: none; font-size: 0.88em; }
        .back-btn:hover { color: #00ff88; }
        .page-title { font-size: 0.72em; letter-spacing: 0.15em; text-transform: uppercase; color: #00ff88; font-weight: 600; }
        h1 { font-family: 'Space Grotesk', sans-serif; font-size: 1.6em; text-align: center; max-width: 480px; margin: 0 auto 4px; }
        .sub { text-align: center; color: #555; font-size: 0.85em; max-width: 480px; margin: 0 auto 20px; }
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
    %s
    <div class="lb-list">
        %s
    </div>
    <a href="/" class="eval-btn">&#9889; Evaluate a New Candidate</a>
    %s
</body>
</html>
        """.formatted(MASCOT_CSS, sorted.size(), mascotBlock("Who's leading today? Let's find out!"), rows.toString(), MASCOT_JS);
        return ResponseEntity.ok().header("Content-Type", "text/html; charset=UTF-8").body(html);
    }
}
