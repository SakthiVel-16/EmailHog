package com.javahog.mail_clone;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EmailRepository extends MongoRepository<Email, String> {

    // Get all emails — newest first
    List<Email> findAllByOrderByReceivedAtDesc();

    // Search across from, subject, body
    @Query("{ '$or': [ " +
            "{ 'from': { '$regex': ?0, '$options': 'i' } }, " +
            "{ 'subject': { '$regex': ?0, '$options': 'i' } }, " +
            "{ 'htmlBody': { '$regex': ?0, '$options': 'i' } } " +
            "] }")
    List<Email> searchEmails(String keyword);
}