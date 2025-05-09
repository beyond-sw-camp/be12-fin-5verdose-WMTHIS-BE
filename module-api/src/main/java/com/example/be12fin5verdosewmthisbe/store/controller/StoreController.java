package com.example.be12fin5verdosewmthisbe.store.controller;

import com.example.be12fin5verdosewmthisbe.common.BaseResponse;
import com.example.be12fin5verdosewmthisbe.market_management.market.model.dto.InventorySaleDto;
import com.example.be12fin5verdosewmthisbe.market_management.market.service.MarketService;
import com.example.be12fin5verdosewmthisbe.security.JwtTokenProvider;
import com.example.be12fin5verdosewmthisbe.store.model.Store;
import com.example.be12fin5verdosewmthisbe.store.model.dto.StoreDto;
import com.example.be12fin5verdosewmthisbe.store.service.StoreService;
import com.example.be12fin5verdosewmthisbe.user.model.User;
import com.example.be12fin5verdosewmthisbe.user.model.dto.UserDto;
import com.example.be12fin5verdosewmthisbe.user.service.UserService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/store")
@RequiredArgsConstructor
public class StoreController {
    // Your code here
    private final StoreService storeService;
    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final MarketService marketService;

    @PostMapping("/register")
    public BaseResponse<String> registerStore(HttpServletRequest request, HttpServletResponse response, @RequestBody StoreDto.RegistRequest dto) {
        log.info("Registering store {}", dto);
        String token = null;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("ATOKEN".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }
        Claims claims = jwtTokenProvider.getClaims(token); 
        // JWT 읽기

        String emailUrl = claims.get("email", String.class);
        String storeId = storeService.registerStore(dto, emailUrl);
        String jwtToken = jwtTokenProvider.createToken(emailUrl, storeId);
        log.info("jwt token {}", jwtToken);
        ResponseCookie cookie = ResponseCookie
                .from("ATOKEN", jwtToken)
                .path("/")
                .httpOnly(true)
                .secure(true)
                .maxAge(Duration.ofHours(1L))
                .build();
        response.setHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        log.info("set cookie");


        return BaseResponse.success("가게등록에 성공했습니다.");
    }


    @GetMapping("/getAddress")
    public BaseResponse<StoreDto.response> getAddress(HttpServletRequest request) {

        Long storeId = getStoreId(request);
        Store store = storeService.getStoreById(storeId);
        StoreDto.response response = StoreDto.response.builder()
                .address(store.getAddress())
                .name(store.getName())
                .longitude(store.getLongitude())
                .latitude(store.getLatitude())
                .phoneNumber(store.getPhoneNumber())
                .build();
        return BaseResponse.success(response);
    }
    private Long getStoreId(HttpServletRequest request) {
        String token = null;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("ATOKEN".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }
        Claims claims = jwtTokenProvider.getClaims(token);
        Long storeId = Long.valueOf(claims.get("storeId", String.class));
        return  storeId;
    }
    @GetMapping("/getNearbyStores")
    private List<StoreDto.response> getNearbyStores(HttpServletRequest request) {
        Long storeId = getStoreId(request);
        List<Long> storeIdList = storeService.getNearbyStoreIds(storeId);

        return storeIdList.stream()
                .map(id -> {
                    Store store = storeService.getStoreById(id);
                    return StoreDto.response.builder()
                            .name(store.getName())
                            .address(store.getAddress())
                            .phoneNumber(store.getPhoneNumber())
                            .latitude(store.getLatitude())
                            .longitude(store.getLongitude())
                            .boardList(marketService.findInventorySaleListByStoreId(id))
                            .build();
                })
                .toList();
    }

}
        