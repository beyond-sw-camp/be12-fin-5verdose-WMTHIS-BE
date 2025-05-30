package com.example.orderservice.order.controller;

import com.example.common.common.config.ErrorCode;
import com.example.orderservice.order.model.dto.*;
import com.example.common.common.config.BaseResponse;
import com.example.orderservice.order.model.Order;
import com.example.orderservice.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/order")
public class OrderController {
    private final OrderService orderService;

    @PostMapping("/create")
    public BaseResponse<OrderDto.OrderCreateResponse> createOrder(@RequestBody OrderDto.OrderCreateRequest request, @RequestHeader("X-Store-Id") String storeId) {

        OrderDto.OrderCreateResponse created = orderService.createOrder(request, Long.parseLong(storeId));
        return BaseResponse.success(created);
    }
    @GetMapping("/getList")
    public BaseResponse<List<OrderDto.AllOrderList>> getAllOrders(@RequestHeader("X-Store-Id") String storeId) {

        return BaseResponse.success(orderService.getOrdersByStoreId(Long.parseLong(storeId)));
    }

    @GetMapping("/{orderId}")
    public BaseResponse<Order> getOrder(@PathVariable Long orderId) {
        return BaseResponse.success(orderService.getOrderById(orderId));
    }

    @GetMapping("/todaySales")
    public BaseResponse<OrderTodayDto.OrderTodayResponse> getTodaySales(@RequestHeader("X-Store-Id") String storeId) {
        OrderTodayDto.OrderTodayResponse todayResponse = orderService.getTodaySales(Long.parseLong(storeId));

        return BaseResponse.success(todayResponse);
    }

    @GetMapping("/weekbestmenu")
    public BaseResponse<OrderTopMenuDto.TopWeekResponse> getWeekBestMenu(@RequestHeader("X-Store-Id") String storeId) {
        OrderTopMenuDto.TopWeekResponse todayResponse = orderService.getTopWeekSales(Long.parseLong(storeId));
        return BaseResponse.success(todayResponse);
    }

    @PostMapping("/monthSales")
    public BaseResponse<List<OrderMonthDto.TotalSaleResponse>> getMonthSales(@RequestHeader("X-Store-Id") String storeId, @Valid @RequestBody OrderMonthDto.TotalRequest totalRequest) {
        int year = totalRequest.getYear();
        int month = totalRequest.getMonth();
        List<OrderMonthDto.TotalSaleResponse> monthSaleList = orderService.getMonthSales(Long.parseLong(storeId), year, month);
        return BaseResponse.success(monthSaleList);
    }

    @PostMapping("/saleDetail")
    public BaseResponse<List<OrderSaleDetailDto.TotalResponse>> getSalesDetail(@RequestHeader("X-Store-Id") String storeId, @RequestBody OrderSaleDetailDto.OrderSaleDetailRequest dto) {
        LocalDate startDate = dto.getStartDate();
        LocalDate endDate = dto.getEndDate();
        List<OrderSaleDetailDto.TotalResponse> detailSaleList = orderService.getSalesDetail(Long.parseLong(storeId), startDate, endDate);
        return BaseResponse.success(detailSaleList);
    }
    @PostMapping("/validateOrder")
    public BaseResponse<String> validateOrder(@RequestHeader("X-Store-Id") String storeId, @RequestBody InventoryValidateOrderDto dto) {
        List<String> insufficientItems = orderService.validateOrder(Long.parseLong(storeId), dto);

        Set<String> uniqueItems = new LinkedHashSet<>(insufficientItems);

        if (!uniqueItems.isEmpty()) {
            String message = "해당 재고가 부족할 수도 있어요. 조리 전 확인해주세요. \n" + String.join("", uniqueItems);
            return new BaseResponse<>(ErrorCode.INSUFFICIENT_INVENTORY.getStatus(), message, null);
        }

        // 부족한 재고가 없으면 정상 처리
        return BaseResponse.success("모든 재고가 충분합니다.");
    }

}
        