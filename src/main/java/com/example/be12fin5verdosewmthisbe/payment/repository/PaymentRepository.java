package com.example.be12fin5verdosewmthisbe.payment.repository;


import com.example.be12fin5verdosewmthisbe.payment.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestMapping;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    // Your code here
}
        