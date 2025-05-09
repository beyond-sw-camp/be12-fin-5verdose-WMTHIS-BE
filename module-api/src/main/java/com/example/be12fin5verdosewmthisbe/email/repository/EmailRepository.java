package com.example.be12fin5verdosewmthisbe.email.repository;

import com.example.be12fin5verdosewmthisbe.email.model.Email;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

@Repository
public interface EmailRepository extends JpaRepository<Email,Long> {
    Optional<Email> findByEmailUrl(String emailUrl);
}
        