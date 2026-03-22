package com.javahog.mail_clone;

import org.springframework.stereotype.Service;
import javax.mail.*;
import javax.mail.internet.*;
import java.util.Base64;
import java.util.Properties;

@Service
public class ComposeService {

    public void sendToSandbox(ComposeRequest request) throws Exception {

        // Setup connection to our own SMTP server on localhost:2500
        Properties props = new Properties();
        props.put("mail.smtp.host", "localhost");
        props.put("mail.smtp.port", "2500");
        props.put("mail.smtp.auth", "false");
        props.put("mail.smtp.starttls.enable", "false");

        Session session = Session.getInstance(props);

        // Build the MimeMessage
        MimeMessage message = new MimeMessage(session);

        // Set basic fields
        message.setFrom(new InternetAddress(
                request.getFrom() != null && !request.getFrom().isBlank()
                        ? request.getFrom()
                        : "composer@emailhog.local"));
        message.setRecipient(
                Message.RecipientType.TO,
                new InternetAddress(
                        request.getTo() != null && !request.getTo().isBlank()
                                ? request.getTo()
                                : "inbox@emailhog.local"));
        message.setSubject(
                request.getSubject() != null && !request.getSubject().isBlank()
                        ? request.getSubject()
                        : "(No Subject)");

        // Check if we have attachments
        boolean hasAttachments = request.getAttachments() != null
                && !request.getAttachments().isEmpty();

        if (!hasAttachments) {
            // Simple email — just HTML body
            MimeMultipart multipart = new MimeMultipart("alternative");

            // Plain text part
            MimeBodyPart textPart = new MimeBodyPart();
            String plainText = request.getPlainText() != null
                    ? request.getPlainText()
                    : stripHtml(request.getHtmlBody());
            textPart.setText(plainText, "UTF-8");
            multipart.addBodyPart(textPart);

            // HTML part
            MimeBodyPart htmlPart = new MimeBodyPart();
            htmlPart.setContent(
                    request.getHtmlBody() != null ? request.getHtmlBody() : "",
                    "text/html; charset=UTF-8");
            multipart.addBodyPart(htmlPart);

            message.setContent(multipart);

        } else {
            // Email with attachments — mixed multipart
            MimeMultipart mixed = new MimeMultipart("mixed");

            // Body part (alternative — text + html)
            MimeBodyPart bodyWrapper = new MimeBodyPart();
            MimeMultipart alternative = new MimeMultipart("alternative");

            MimeBodyPart textPart = new MimeBodyPart();
            String plainText = request.getPlainText() != null
                    ? request.getPlainText()
                    : stripHtml(request.getHtmlBody());
            textPart.setText(plainText, "UTF-8");
            alternative.addBodyPart(textPart);

            MimeBodyPart htmlPart = new MimeBodyPart();
            htmlPart.setContent(
                    request.getHtmlBody() != null ? request.getHtmlBody() : "",
                    "text/html; charset=UTF-8");
            alternative.addBodyPart(htmlPart);

            bodyWrapper.setContent(alternative);
            mixed.addBodyPart(bodyWrapper);

            // Add each attachment
            for (ComposeRequest.AttachmentData attachment : request.getAttachments()) {
                MimeBodyPart attachPart = new MimeBodyPart();
                byte[] fileBytes = Base64.getDecoder()
                        .decode(attachment.getBase64Content());
                attachPart.setFileName(attachment.getFilename());
                attachPart.setContent(fileBytes, attachment.getMimeType());
                mixed.addBodyPart(attachPart);
            }

            message.setContent(mixed);
        }

        // Send to our own SMTP server on localhost:2500
        Transport.send(message);
    }

    // Strip HTML tags to generate plain text fallback
    private String stripHtml(String html) {
        if (html == null)
            return "";
        return html.replaceAll("<[^>]*>", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }
}