package com.example.userservice.email.service;

import com.example.common.common.config.CustomException;
import com.example.common.common.config.ErrorCode;
import com.example.userservice.email.model.Email;
import com.example.userservice.email.model.dto.EmailDto;
import com.example.userservice.email.repository.EmailRepository;
import com.example.userservice.user.model.User;
import com.example.userservice.user.repository.UserRepository;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender javaMailSender;
    private final EmailRepository emailRepository;
    private final UserRepository userRepository;

    @Transactional
    public void AuthRequest(EmailDto.EmailRequest dto) {
        String emailUrl = dto.getEmailUrl();
        String code = generateVerificationCode();

        Optional<Email> emailExisting = emailRepository.findByEmailUrl(emailUrl);

        if(emailExisting.isPresent()) {
            Email email = emailExisting.get();
            email.setCode(code);
            email.setCreatedAt(LocalDateTime.now());
            email.setExpiresAt(LocalDateTime.now().plusMinutes(5));
            email.setVerified(false);
            emailRepository.save(email);
        }else{
            Email email = EmailDto.EmailAuthRequest.toEntity(emailUrl, code);
            emailRepository.save(email);
        }

        sendHtmlEmail(emailUrl, code);

    }

    @Transactional
    public void sendCodeifpwfind(EmailDto.EmailRequest dto) {
        String emailUrl = dto.getEmailUrl();
        String code = generateVerificationCode();

        Optional<User> userExisting = userRepository.findByEmail(emailUrl);

        if (!userExisting.isPresent()) {
            return;
        }

        Optional<Email> existingEmail = emailRepository.findByEmailUrl(emailUrl);
        if (existingEmail.isPresent()) {
            Email email = existingEmail.get();
            email.setCode(code);
            email.setCreatedAt(LocalDateTime.now());
            email.setExpiresAt(LocalDateTime.now().plusMinutes(5));
            email.setVerified(false);
            emailRepository.save(email);
            sendHtmlEmail(emailUrl, code);
        }
    }

    public void sendHtmlEmail(String to, String code) {
        try {

            String htmlContent = readHtmlTemplate("templates/EmailTemplate.html");
            htmlContent = htmlContent.replace("{{code}}", code);

            // 3. 이메일 발송
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setFrom("dhtmdwo73@gmail.com");
            helper.setText(htmlContent, true);

            javaMailSender.send(message);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String readHtmlTemplate(String path) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new ClassPathResource(path).getInputStream(), StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining("\n"));
        } catch (Exception e) {
            throw new RuntimeException("HTML 템플릿을 불러오는 중 오류 발생", e);
        }
    }

    // 6자리 인증 코드 생성 메서드
    private String generateVerificationCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000); // 100000 ~ 999999
        return String.valueOf(code);
    }

    // 인증 코드 검증
    @Transactional
    public void verifyCode(EmailDto.EmailAuthRequest dto) {
        String emailUrl = dto.getEmailUrl();
        String code = dto.getCode();

        Optional<Email> emailExisting = emailRepository.findByEmailUrl(emailUrl);
        if(emailExisting.isPresent()) {
            Email email = emailExisting.get();
            if(email.isExpired()) {
                throw new CustomException(ErrorCode.EMAIL_ALREADY_EXPIRED);
            }
            else if(!email.getCode().equals(code)) {
                throw new CustomException(ErrorCode.EMAILCODE_NOT_MATCH);
            }
            else{
                email.setVerified(true);
                emailRepository.save(email);
            }
        }
        else{
            throw new CustomException(ErrorCode.EMAILCODE_NOT_MATCH);
        }
    }
}
        