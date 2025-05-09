package com.example.be12fin5verdosewmthisbe.user.repository;

import com.example.be12fin5verdosewmthisbe.user.model.PhoneVerification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PhoneVerificationRepository extends JpaRepository<PhoneVerification, Long> {
    Optional<PhoneVerification> findTopByPhoneNumberOrderByCreatedAtDesc(String phoneNumber);
}
