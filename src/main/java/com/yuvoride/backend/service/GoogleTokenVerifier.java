package com.yuvoride.backend.service;

public interface GoogleTokenVerifier {

    GoogleProfile verify(String idToken);

    record GoogleProfile(String subject, String email, String name, String picture) {
    }
}
