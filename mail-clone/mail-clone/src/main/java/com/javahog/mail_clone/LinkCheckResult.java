package com.javahog.mail_clone;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LinkCheckResult {

    // The full URL that was checked
    private String url;

    // HTTP status code returned — 200, 301, 404, 0 (unreachable)
    private int statusCode;

    // Human readable label — OK, REDIRECT, BROKEN, UNREACHABLE
    private String statusLabel;
}