package com.selfbell.auth.service;

import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class FakeCertificationService implements CertificationService {

    private static final Set<String> certifiedPhoneNumbers = new HashSet<>();

    static {
        certifiedPhoneNumbers.add("01012345678"); // 테스트용
    }

    @Override
    public boolean isCertified(String phoneNumber) {
        return certifiedPhoneNumbers.contains(phoneNumber);
    }
}
