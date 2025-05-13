package com.example.be12fin5verdosewmthisbe.order.controller;

import com.example.be12fin5verdosewmthisbe.common.BaseResponse;
import com.example.be12fin5verdosewmthisbe.order.model.Order;
import com.example.be12fin5verdosewmthisbe.order.model.dto.*;
import com.example.be12fin5verdosewmthisbe.order.service.OrderService;
import com.example.be12fin5verdosewmthisbe.security.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/order")
public class OrderController {
    private final OrderService orderService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/create")
    public BaseResponse<OrderDto.OrderCreateResponse> createOrder(@RequestBody OrderDto.OrderCreateRequest request, HttpServletRequest req) {
        /*String token = null;
        if (req.getCookies() != null) {
            for (Cookie cookie : req.getCookies()) {
                if ("ATOKEN".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }
        Claims claims = jwtTokenProvider.getClaims(token);
        // JWT 읽기
        String storeIdStr = claims.get("storeId", String.class);
        Long storeId = Long.parseLong(storeIdStr);*/

        OrderDto.OrderCreateResponse created = orderService.createOrder(request, request.getStoreId());
        return BaseResponse.success(created);
    }
    @GetMapping("/getList")
    public BaseResponse<List<OrderDto.AllOrderList>> getAllOrders(HttpServletRequest req) {
        String token = null;
        if (req.getCookies() != null) {
            for (Cookie cookie : req.getCookies()) {
                if ("ATOKEN".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }
        Claims claims = jwtTokenProvider.getClaims(token);
        // JWT 읽기
        String storeIdStr = claims.get("storeId", String.class);
        Long storeId = Long.parseLong(storeIdStr);
        return BaseResponse.success(orderService.getOrdersByStoreId(storeId));
    }

    @GetMapping("/{orderId}")
    public BaseResponse<Order> getOrder(@PathVariable Long orderId) {
        return BaseResponse.success(orderService.getOrderById(orderId));
    }

    @GetMapping("/todaySales")
    public BaseResponse<OrderTodayDto.OrderTodayResponse> getTodaySales(HttpServletRequest request) {
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
        String storeId = claims.get("storeId", String.class);
        OrderTodayDto.OrderTodayResponse todayResponse = orderService.getTodaySales(storeId);

        return BaseResponse.success(todayResponse);
    }

    @GetMapping("/weekbestmenu")
    public BaseResponse<OrderTopMenuDto.TopWeekResponse> getWeekBestMenu(HttpServletRequest request) {

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
        String storeIdStr = claims.get("storeId", String.class);
        Long storeId = Long.parseLong(storeIdStr);
        OrderTopMenuDto.TopWeekResponse todayResponse = orderService.getTopWeekSales(storeId);
        return BaseResponse.success(todayResponse);
    }

    @PostMapping("/monthSales")
    public BaseResponse<List<OrderMonthDto.TotalSaleResponse>> getMonthSales(HttpServletRequest request,@Valid @RequestBody OrderMonthDto.TotalRequest totalRequest) {

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
        String storeIdStr = claims.get("storeId", String.class);
        Long storeId = Long.parseLong(storeIdStr);
        int year = totalRequest.getYear();
        int month = totalRequest.getMonth();
        List<OrderMonthDto.TotalSaleResponse> monthSaleList = orderService.getMonthSales(storeId, year, month);
        return BaseResponse.success(monthSaleList);
    }
    //매출 분석 리스트

    @PostMapping("/saleDetail")
    public BaseResponse<List<OrderSaleDetailDto.TotalResponse>> getSalesDetail(HttpServletRequest request, @RequestBody OrderSaleDetailDto.OrderSaleDetailRequest dto) {

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
        String storeIdStr = claims.get("storeId", String.class);
        Long storeId = Long.parseLong(storeIdStr);
        LocalDate startDate = dto.getStartDate();
        LocalDate endDate = dto.getEndDate();
        List<OrderSaleDetailDto.TotalResponse> detailSaleList = orderService.getSalesDetail(storeId, startDate, endDate);
        return BaseResponse.success(detailSaleList);
    }
    // 매출 분석 상세

}
        