package com.javahog.mail_clone;

import org.springframework.stereotype.Service;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class LinkCheckerService {

    // HTTP client — 5 second timeout, does NOT follow redirects
    // We want to detect redirects ourselves
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .followRedirects(HttpClient.Redirect.NEVER)
            .build();

    // Step 1 — Extract all URLs from email HTML body
    public List<String> extractLinks(String htmlBody) {
        List<String> links = new ArrayList<>();
        if (htmlBody == null || htmlBody.isBlank())
            return links;

        // Match href="https://..." and href='https://...'
        Pattern pattern = Pattern.compile(
                "href=[\"'](https?://[^\"'\\s>]+)[\"']",
                Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(htmlBody);
        while (matcher.find()) {
            String url = matcher.group(1);
            if (!links.contains(url)) {
                links.add(url); // avoid duplicates
            }
        }
        return links;
    }

    // Step 2 — Check one URL and return its status
    public LinkCheckResult checkLink(String url) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .method("HEAD", HttpRequest.BodyPublishers.noBody())
                    .timeout(Duration.ofSeconds(5))
                    .header("User-Agent", "EmailHog-LinkChecker/2.0")
                    .build();

            HttpResponse<Void> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.discarding());

            int status = response.statusCode();
            String label;

            if (status >= 200 && status < 300) {
                label = "OK";
            } else if (status >= 300 && status < 400) {
                label = "REDIRECT";
            } else if (status >= 400 && status < 500) {
                label = "BROKEN";
            } else {
                label = "ERROR";
            }

            return new LinkCheckResult(url, status, label);

        } catch (Exception e) {
            return new LinkCheckResult(url, 0, "UNREACHABLE");
        }
    }

    // Step 3 — Check ALL links in parallel for speed
    public List<LinkCheckResult> checkAllLinks(String htmlBody) {
        List<String> urls = extractLinks(htmlBody);

        if (urls.isEmpty())
            return new ArrayList<>();

        // parallelStream checks all URLs simultaneously
        // 10 links checked at same time = same speed as checking 1 link
        return urls.parallelStream()
                .map(this::checkLink)
                .toList();
    }
}