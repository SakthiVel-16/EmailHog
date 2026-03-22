package com.javahog.mail_clone;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class InboxService {

    private final EmailRepository emailRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public InboxService(EmailRepository emailRepository,
            SimpMessagingTemplate messagingTemplate) {
        this.emailRepository = emailRepository;
        this.messagingTemplate = messagingTemplate;
    }

    // 1. Save email to MongoDB AND broadcast via WebSocket
    public void addEmail(Email email) {
        emailRepository.save(email);
        messagingTemplate.convertAndSend("/topic/inbox", email);
    }

    // 2. Get all emails or search
    public List<Email> getEmails(String query) {
        if (query == null || query.isBlank()) {
            return emailRepository.findAllByOrderByReceivedAtDesc();
        }
        return emailRepository.searchEmails(query);
    }

    // 3. Delete one email by ID
    public void deleteEmail(String id) {
        emailRepository.deleteById(id);
    }

    // 4. Delete all emails
    public void deleteAll() {
        emailRepository.deleteAll();
    }
}