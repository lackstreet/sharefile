package com.company.sharefile.service;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.UUID;

@ApplicationScoped
public class CsrfService {
    public String generate() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public boolean verify(String header, String cookie) {
        return header != null && header.equals(cookie);
    }
}
