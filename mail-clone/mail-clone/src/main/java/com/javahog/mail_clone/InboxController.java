package com.javahog.mail_clone;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@RestController
@RequestMapping("/api/emails")
public class InboxController {

    private final InboxService inboxService;
    private final EmailRepository emailRepository;
    private final LinkCheckerService linkCheckerService;
    private final SpamAnalyserService spamAnalyserService;
    private final ComposeService composeService;
    private final GridFsTemplate gridFsTemplate;

    public InboxController(InboxService inboxService,
            EmailRepository emailRepository,
            LinkCheckerService linkCheckerService,
            SpamAnalyserService spamAnalyserService,
            ComposeService composeService,
            GridFsTemplate gridFsTemplate) {
        this.inboxService = inboxService;
        this.emailRepository = emailRepository;
        this.linkCheckerService = linkCheckerService;
        this.spamAnalyserService = spamAnalyserService;
        this.composeService = composeService;
        this.gridFsTemplate = gridFsTemplate;
    }

    // 1. Get all emails (supports search)
    @GetMapping
    public List<Email> getEmails(
            @RequestParam(required = false) String query) {
        return inboxService.getEmails(query);
    }

    // 2. Get single email by ID
    @GetMapping("/{id}")
    public ResponseEntity<Email> getEmail(@PathVariable String id) {
        return emailRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 3. Get headers for one email
    @GetMapping("/{id}/headers")
    public ResponseEntity<Map<String, String>> getHeaders(
            @PathVariable String id) {
        return emailRepository.findById(id)
                .map(email -> ResponseEntity.ok(email.getHeaders()))
                .orElse(ResponseEntity.notFound().build());
    }

    // 4. Get SMTP session log for one email
    @GetMapping("/{id}/smtp-log")
    public ResponseEntity<List<Email.SmtpLogEntry>> getSmtpLog(
            @PathVariable String id) {
        return emailRepository.findById(id)
                .map(email -> ResponseEntity.ok(email.getSmtpLog()))
                .orElse(ResponseEntity.notFound().build());
    }

    // 5. Check all links in one email
    @GetMapping("/{id}/check-links")
    public ResponseEntity<List<LinkCheckResult>> checkLinks(
            @PathVariable String id) {
        return emailRepository.findById(id)
                .map(email -> ResponseEntity.ok(
                        linkCheckerService.checkAllLinks(email.getBody())))
                .orElse(ResponseEntity.notFound().build());
    }

    // 6. AI spam analysis
    @PostMapping("/{id}/analyse")
    public ResponseEntity<SpamAnalysisResult> analyseEmail(
            @PathVariable String id) {
        return emailRepository.findById(id)
                .map(email -> ResponseEntity.ok(
                        spamAnalyserService.analyseEmail(email)))
                .orElse(ResponseEntity.notFound().build());
    }

    // 7. Compose and send email to sandbox
    @PostMapping("/compose")
    public ResponseEntity<Map<String, String>> composeEmail(
            @RequestBody ComposeRequest request) {
        try {
            composeService.sendToSandbox(request);
            return ResponseEntity.ok(
                    Map.of("status", "success",
                            "message", "Email sent to sandbox!"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("status", "error",
                            "message", e.getMessage()));
        }
    }

    // 9. Delete one email
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmail(@PathVariable String id) {
        try {
            // Delete attachments from GridFS first
            emailRepository.findById(id).ifPresent(email -> {
                if (email.getAttachments() != null) {
                    email.getAttachments().forEach(att -> {
                        try {
                            gridFsTemplate.delete(
                                    query(where("_id")
                                            .is(new ObjectId(att.getFileId()))));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                }
            });
            // Delete email document — headers and smtp log
            // deleted automatically (embedded)
            inboxService.deleteEmail(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // 10. Delete all emails
    @DeleteMapping
    public ResponseEntity<Void> deleteAll() {
        try {
            // Delete all GridFS files first
            gridFsTemplate.delete(query(where("_id").exists(true)));
            // Delete all email documents
            inboxService.deleteAll();
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}