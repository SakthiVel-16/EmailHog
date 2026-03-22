package com.javahog.mail_clone;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpamAnalysisResult {

    // Spam score 0 to 100
    private int spamScore;

    // Label — Excellent / Good / Warning / High Risk / Critical
    private String scoreLabel;

    // One sentence overall summary
    private String summary;

    // List of specific issues found
    private List<Issue> issues;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Issue {

        // HIGH / MEDIUM / LOW
        private String severity;

        // What the problem is
        private String issue;

        // Exactly how to fix it
        private String fix;
    }
}