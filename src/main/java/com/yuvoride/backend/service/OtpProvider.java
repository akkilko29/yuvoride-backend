package com.yuvoride.backend.service;

public interface OtpProvider {
    void send(String destination);
}
