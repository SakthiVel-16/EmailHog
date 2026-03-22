package com.javahog.mail_clone;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.index.Indexed;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "emails")
public class Email {

    @Id
    private String id;

    @Field("from")
    private String from;

    @Field("to")
    private String to;

    @Field("subject")
    private String subject;

    // Clean HTML body — NO Base64 inside
    @Field("htmlBody")
    private String body;

    // Plain text version
    @Field("plainText")
    private String plainText;

    @Indexed
    @Field("receivedAt")
    private String receivedAt;

    // All headers embedded directly — no separate table needed
    @Builder.Default
    @Field("headers")
    private Map<String, String> headers = new LinkedHashMap<>();

    // SMTP session log embedded — no separate collection needed
    @Builder.Default
    @Field("smtpLog")
    private List<SmtpLogEntry> smtpLog = new ArrayList<>();

    // Attachment metadata — actual files stored in GridFS
    @Builder.Default
    @Field("attachments")
    private List<AttachmentInfo> attachments = new ArrayList<>();

    // Total email size in bytes — for size badge feature
    @Field("sizeBytes")
    private long sizeBytes;

    // HTML body size only — used for Gmail 102KB clip warning
    @Field("htmlBodySizeBytes")
    private long htmlBodySizeBytes;

    // Total attachment size only
    @Field("attachmentSizeBytes")
    private long attachmentSizeBytes;

    // ===== EMBEDDED CLASSES =====

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SmtpLogEntry {
        private String direction; // CLIENT or SERVER
        private String message;
        private String timestamp;
        private boolean error;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AttachmentInfo {
        private String fileId; // GridFS ObjectId
        private String filename;
        private String contentType;
        private long sizeBytes;
    }
}