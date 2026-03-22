package com.javahog.mail_clone;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComposeRequest {

    // Basic email fields
    private String from;
    private String to;
    private String subject;

    // HTML body written in the composer
    private String htmlBody;

    // Plain text version
    private String plainText;

    // Attachments as Base64 strings
    private List<AttachmentData> attachments;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AttachmentData {
        private String filename;
        private String base64Content;
        private String mimeType;
    }
}