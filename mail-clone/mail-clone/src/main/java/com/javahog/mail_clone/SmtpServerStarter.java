package com.javahog.mail_clone;

import org.bson.types.ObjectId;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Component;
import org.subethamail.smtp.helper.SimpleMessageListener;
import org.subethamail.smtp.helper.SimpleMessageListenerAdapter;
import org.subethamail.smtp.server.SMTPServer;

import javax.mail.Header;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

@Component
public class SmtpServerStarter implements CommandLineRunner {

        private final InboxService inboxService;
        private final GridFsTemplate gridFsTemplate;

        private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

        public SmtpServerStarter(InboxService inboxService,
                        GridFsTemplate gridFsTemplate) {
                this.inboxService = inboxService;
                this.gridFsTemplate = gridFsTemplate;
        }

        @Override
        public void run(String... args) {

                SimpleMessageListener myListener = new SimpleMessageListener() {

                        @Override
                        public boolean accept(String from, String recipient) {
                                return true;
                        }

                        @Override
                        public void deliver(String from, String recipient,
                                        InputStream data) {
                                String emailId = UUID.randomUUID().toString();
                                List<Email.SmtpLogEntry> smtpLog = new ArrayList<>();
                                List<Email.AttachmentInfo> attachments = new ArrayList<>();

                                try {
                                        // Record SMTP session
                                        smtpLog.add(smtpEntry("SERVER",
                                                        "220 localhost EmailHog SMTP Ready", false));
                                        smtpLog.add(smtpEntry("CLIENT",
                                                        "EHLO localhost", false));
                                        smtpLog.add(smtpEntry("SERVER",
                                                        "250-localhost Hello", false));
                                        smtpLog.add(smtpEntry("SERVER",
                                                        "250 AUTH LOGIN PLAIN", false));
                                        smtpLog.add(smtpEntry("CLIENT",
                                                        "MAIL FROM:<" + from + ">", false));
                                        smtpLog.add(smtpEntry("SERVER",
                                                        "250 OK", false));
                                        smtpLog.add(smtpEntry("CLIENT",
                                                        "RCPT TO:<" + recipient + ">", false));
                                        smtpLog.add(smtpEntry("SERVER",
                                                        "250 OK", false));
                                        smtpLog.add(smtpEntry("CLIENT",
                                                        "DATA", false));
                                        smtpLog.add(smtpEntry("SERVER",
                                                        "354 End data with <CR><LF>.<CR><LF>",
                                                        false));

                                        // Parse email
                                        Session session = Session.getDefaultInstance(
                                                        new Properties());
                                        MimeMessage message = new MimeMessage(session, data);
                                        String subject = message.getSubject();

                                        // Build body and collect attachments
                                        StringBuilder htmlBody = new StringBuilder();
                                        StringBuilder plainText = new StringBuilder();
                                        // attachmentChips collects ALL attachment chips
                                        StringBuilder attachmentChips = new StringBuilder();

                                        parseMultipart(message, htmlBody, plainText,
                                                        attachments, attachmentChips, emailId);

                                        // Append Gmail-style attachment section at bottom
                                        if (attachmentChips.length() > 0) {
                                                htmlBody.append(
                                                                "<div style='margin-top:20px;"
                                                                                + "padding-top:16px;"
                                                                                + "border-top:1px solid #e8eaed'>"
                                                                                + "<div style='font-family:Arial;"
                                                                                + "font-size:12px;color:#5f6368;"
                                                                                + "margin-bottom:12px;font-weight:500'>"
                                                                                + attachments.size() + " attachment"
                                                                                + (attachments.size() > 1 ? "s" : "")
                                                                                + "</div>"
                                                                                + "<div style='display:flex;"
                                                                                + "flex-wrap:wrap;gap:10px;"
                                                                                + "align-items:flex-start'>"
                                                                                + attachmentChips.toString()
                                                                                + "</div></div>");
                                        }

                                        // Extract headers
                                        Map<String, String> headersMap = new LinkedHashMap<>();
                                        Enumeration<Header> allHeaders = message.getAllHeaders();
                                        while (allHeaders.hasMoreElements()) {
                                                Header header = allHeaders.nextElement();
                                                headersMap.put(header.getName(),
                                                                header.getValue());
                                        }

                                        // Calculate sizes — dual metric tracking
                                        long htmlBodySize = htmlBody.length()
                                                        + plainText.length();
                                        long attachmentSize = attachments.stream()
                                                        .mapToLong(Email.AttachmentInfo::getSizeBytes)
                                                        .sum();
                                        long totalSize = htmlBodySize + attachmentSize;

                                        // Finish SMTP log
                                        smtpLog.add(smtpEntry("CLIENT",
                                                        "(message headers and body)", false));
                                        smtpLog.add(smtpEntry("CLIENT", ".", false));
                                        smtpLog.add(smtpEntry("SERVER",
                                                        "250 OK: Message queued as "
                                                                        + emailId.substring(0, 8),
                                                        false));
                                        smtpLog.add(smtpEntry("CLIENT",
                                                        "QUIT", false));
                                        smtpLog.add(smtpEntry("SERVER",
                                                        "221 Bye", false));

                                        // Build email document
                                        Email email = Email.builder()
                                                        .id(emailId)
                                                        .from(from)
                                                        .to(recipient)
                                                        .subject(subject != null
                                                                        ? subject
                                                                        : "(No Subject)")
                                                        .body(htmlBody.toString())
                                                        .plainText(plainText.toString())
                                                        .receivedAt(Instant.now().toString())
                                                        .headers(headersMap)
                                                        .smtpLog(smtpLog)
                                                        .attachments(attachments)
                                                        .sizeBytes(totalSize)
                                                        .htmlBodySizeBytes(htmlBodySize)
                                                        .attachmentSizeBytes(attachmentSize)
                                                        .build();

                                        inboxService.addEmail(email);
                                        System.out.println(
                                                        "EmailHog received: " + subject);

                                } catch (Exception e) {
                                        smtpLog.add(smtpEntry("SERVER",
                                                        "500 Error: " + e.getMessage(), true));
                                        e.printStackTrace();
                                }
                        }
                };

                SMTPServer server = new SMTPServer(
                                new SimpleMessageListenerAdapter(myListener));
                server.setPort(2500);
                server.start();
                System.out.println(
                                "EmailHog SMTP Server started on port 2500...");
        }

        // ===== MIME PARSER =====
        private void parseMultipart(Part p,
                        StringBuilder htmlSb,
                        StringBuilder plainSb,
                        List<Email.AttachmentInfo> attachments,
                        StringBuilder attachmentChips,
                        String emailId) throws Exception {

                if (p.isMimeType("text/plain") && !isPart(p)) {
                        plainSb.append(p.getContent().toString());

                } else if (p.isMimeType("text/html") && !isPart(p)) {
                        htmlSb.append(p.getContent().toString());

                } else if (p.isMimeType("multipart/*")) {
                        javax.mail.Multipart mp = (javax.mail.Multipart) p.getContent();
                        for (int i = 0; i < mp.getCount(); i++) {
                                parseMultipart(mp.getBodyPart(i),
                                                htmlSb, plainSb, attachments,
                                                attachmentChips, emailId);
                        }

                } else {
                        // It is an attachment — save to GridFS
                        String filename = p.getFileName();
                        if (filename == null)
                                filename = "attachment";
                        filename = MimeUtility.decodeText(filename);

                        String mimeType = p.getContentType().split(";")[0].trim();

                        // Read bytes
                        InputStream is = p.getInputStream();
                        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                        byte[] chunk = new byte[4096];
                        int nRead;
                        while ((nRead = is.read(chunk, 0, chunk.length)) != -1) {
                                buffer.write(chunk, 0, nRead);
                        }
                        byte[] fileBytes = buffer.toByteArray();
                        long fileSize = fileBytes.length;

                        // Store in GridFS — raw bytes, NO Base64
                        ObjectId fileId = gridFsTemplate.store(
                                        new ByteArrayInputStream(fileBytes),
                                        filename,
                                        mimeType);

                        // Add metadata to attachments list
                        attachments.add(Email.AttachmentInfo.builder()
                                        .fileId(fileId.toString())
                                        .filename(filename)
                                        .contentType(mimeType)
                                        .sizeBytes(fileSize)
                                        .build());

                        boolean isImage = mimeType.toLowerCase()
                                        .startsWith("image/");

                        // Format file size nicely
                        String sizeLabel = fileSize >= 1024 * 1024
                                        ? String.format("%.1f MB",
                                                        fileSize / (1024.0 * 1024))
                                        : (fileSize / 1024) + " KB";

                        if (isImage) {
                                // Image thumbnail chip — small preview card
                                attachmentChips.append(
                                                "<div style='display:inline-flex;"
                                                                + "flex-direction:column;"
                                                                + "border:1px solid #e0e0e0;"
                                                                + "border-radius:8px;overflow:hidden;"
                                                                + "background:white;vertical-align:top;"
                                                                + "box-shadow:0 1px 3px rgba(0,0,0,0.08);"
                                                                + "width:160px'>"
                                                                // Thumbnail — click to open full size
                                                                + "<a href='/api/attachments/"
                                                                + fileId + "' target='_blank'>"
                                                                + "<img src='/api/attachments/" + fileId + "'"
                                                                + " style='width:160px;height:120px;"
                                                                + "object-fit:cover;display:block;"
                                                                + "background:#f5f5f5'/></a>"
                                                                // Info row
                                                                + "<div style='padding:6px 8px;"
                                                                + "border-top:1px solid #f0f0f0;"
                                                                + "display:flex;align-items:center;"
                                                                + "justify-content:space-between;gap:4px'>"
                                                                + "<div style='min-width:0;flex:1'>"
                                                                + "<div style='font-family:Arial;font-size:11px;"
                                                                + "font-weight:500;color:#202124;"
                                                                + "white-space:nowrap;overflow:hidden;"
                                                                + "text-overflow:ellipsis'>"
                                                                + filename + "</div>"
                                                                + "<div style='font-family:Arial;font-size:10px;"
                                                                + "color:#5f6368;margin-top:1px'>"
                                                                + sizeLabel + "</div>"
                                                                + "</div>"
                                                                + "<a href='/api/attachments/" + fileId + "'"
                                                                + " download='" + filename + "'"
                                                                + " title='Download'"
                                                                + " style='width:24px;height:24px;"
                                                                + "border-radius:50%;background:#f1f3f4;"
                                                                + "display:flex;align-items:center;"
                                                                + "justify-content:center;"
                                                                + "text-decoration:none;color:#5f6368;"
                                                                + "font-size:11px;flex-shrink:0;"
                                                                + "border:1px solid #e8e8e8'>⬇</a>"
                                                                + "</div></div>");

                        } else {
                                // Non-image chip — icon + name + size + download
                                attachmentChips.append(
                                                "<div style='display:inline-flex;"
                                                                + "align-items:center;gap:10px;"
                                                                + "padding:10px 14px;"
                                                                + "border:1px solid #e0e0e0;"
                                                                + "border-radius:8px;background:white;"
                                                                + "font-family:Arial;vertical-align:top;"
                                                                + "box-shadow:0 1px 2px rgba(0,0,0,0.08);"
                                                                + "min-width:200px;max-width:260px'>"
                                                                // File type icon
                                                                + "<div style='width:36px;height:36px;"
                                                                + "background:#fce8e6;border-radius:6px;"
                                                                + "display:flex;align-items:center;"
                                                                + "justify-content:center;font-size:18px;"
                                                                + "flex-shrink:0'>📄</div>"
                                                                // Name and meta
                                                                + "<div style='flex:1;min-width:0'>"
                                                                + "<div style='font-size:13px;font-weight:500;"
                                                                + "color:#202124;white-space:nowrap;"
                                                                + "overflow:hidden;text-overflow:ellipsis'>"
                                                                + filename + "</div>"
                                                                + "<div style='font-size:11px;color:#5f6368;"
                                                                + "margin-top:2px'>"
                                                                + mimeType + " · " + sizeLabel + "</div>"
                                                                + "</div>"
                                                                // Download button
                                                                + "<a href='/api/attachments/" + fileId + "'"
                                                                + " download='" + filename + "'"
                                                                + " title='Download'"
                                                                + " style='width:32px;height:32px;"
                                                                + "border-radius:50%;background:#f1f3f4;"
                                                                + "display:flex;align-items:center;"
                                                                + "justify-content:center;"
                                                                + "text-decoration:none;color:#5f6368;"
                                                                + "font-size:14px;flex-shrink:0;"
                                                                + "border:1px solid #e0e0e0'>⬇</a>"
                                                                + "</div>");
                        }
                }
        }

        // Check if part is an attachment
        private boolean isPart(Part p) {
                try {
                        String disposition = p.getDisposition();
                        return disposition != null &&
                                        disposition.equalsIgnoreCase(Part.ATTACHMENT);
                } catch (Exception e) {
                        return false;
                }
        }

        // Helper to create SMTP log entry
        private Email.SmtpLogEntry smtpEntry(String direction,
                        String message,
                        boolean error) {
                return Email.SmtpLogEntry.builder()
                                .direction(direction)
                                .message(message)
                                .timestamp(LocalDateTime.now().format(FORMATTER))
                                .error(error)
                                .build();
        }
}