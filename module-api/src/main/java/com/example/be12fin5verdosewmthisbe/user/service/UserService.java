package com.example.be12fin5verdosewmthisbe.user.service;

import com.example.be12fin5verdosewmthisbe.common.CustomException;
import com.example.be12fin5verdosewmthisbe.common.ErrorCode;
import com.example.be12fin5verdosewmthisbe.security.JwtTokenProvider;
import com.example.be12fin5verdosewmthisbe.store.model.Store;
import com.example.be12fin5verdosewmthisbe.user.model.User;
import com.example.be12fin5verdosewmthisbe.user.model.dto.UserInfoDto;
import com.example.be12fin5verdosewmthisbe.user.model.dto.UserRegisterDto;
import com.example.be12fin5verdosewmthisbe.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    // Your code here
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + email));
    }

    @Transactional
    public UserRegisterDto.SignupResponse signUp(UserRegisterDto.SignupRequest dto) {

        if(userRepository.existsByEmail(dto.getEmail())) {
            throw new CustomException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        if(userRepository.existsByBusinessNumber(dto.getBusinessNumber())) {
            throw new CustomException(ErrorCode.BUSINESSNUMBER_ALREADY_EXISTS);
        }

        if(userRepository.existsByPhoneNumber(dto.getPhoneNumber())) {
            throw new CustomException(ErrorCode.PHONENUMBER_ALREADY_EXISTS);
        }

        if(userRepository.existsBySsn(dto.getSsn())) {
            throw new CustomException(ErrorCode.SSN_ALREADY_EXISTS);
        }

        String encodedPassword = passwordEncoder.encode(dto.getPassword());
        User user = userRepository.save(dto.toEntity(encodedPassword));
        return UserRegisterDto.SignupResponse.from(user);
    }


    public User login(String email, String rawPassword) {
        String encoded = passwordEncoder.encode(rawPassword);
        User user = userRepository.findByEmail(email).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND) );
        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }
        return user;
    }

    public UserInfoDto.SearchResponse searchUserInfo(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND) );
        return UserInfoDto.SearchResponse.from(user);
    }

    public String updateUserInfo(UserInfoDto.UpdateRequest dto) {
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 현재 비밀번호 확인
        if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }

        // 동일한 비밀번호로 변경하는 경우 방지
        if (passwordEncoder.matches(dto.getNewPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.SAME_AS_CURRENT_PASSWORD);
        }

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);
        return "성공적으로 정보가 수정되었습니다.";
    }

    public String deleteUser(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND) );
        userRepository.delete(user);


        return "성공적으로 탈퇴되었습니다.";
    }

    public String updatePassword(UserInfoDto.PasswordRequest dto) {
        User user = userRepository.findByEmail(dto.getEmail()).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        userRepository.save(user);
        return "새로운 비밀번호가 생성되었습니다.";
    }
    public boolean isStoreRegistered(String emailUrl) {
        User user = userRepository.findByEmail(emailUrl).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        Store store = user.getStore();
        if(store == null) {
            return false;
        }
        return true;
    } // 가게 등록되었는지 확인

    public String getStoreId(String emailUrl) {
        User user = userRepository.findByEmail(emailUrl).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        Store store = Optional.ofNullable(user.getStore()).orElseThrow(()-> new CustomException(ErrorCode.STORE_NOT_EXIST));

        return String.valueOf(store.getId());
    } 
    // 가게 번호 얻기

}
        