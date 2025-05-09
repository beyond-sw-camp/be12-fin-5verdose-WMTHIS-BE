package com.example.be12fin5verdosewmthisbe.inventory.controller;

import com.example.be12fin5verdosewmthisbe.common.BaseResponse;
import com.example.be12fin5verdosewmthisbe.common.CustomException;
import com.example.be12fin5verdosewmthisbe.common.ErrorCode;
import com.example.be12fin5verdosewmthisbe.inventory.model.StoreInventory;

import com.example.be12fin5verdosewmthisbe.inventory.model.dto.*;
import com.example.be12fin5verdosewmthisbe.inventory.service.InventoryService;
import com.example.be12fin5verdosewmthisbe.security.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Tag(name = "Inventory", description = "재고 관련 API")
@RequiredArgsConstructor
@RestController
@Validated
@RequestMapping("/api/inventory")
@Tag(name = "재고관리", description = "재고 관리 API") // 이 라인을 추가하여 CORS 허용
public class InventoryController {
    private final InventoryService inventoryService;
    private final JwtTokenProvider jwtTokenProvider;

    //dto로 정보 받아서 StoreInventory 저장
    @PostMapping("/registerStoreInventory")
    public BaseResponse<String> registerStoreInventory(HttpServletRequest request, @Valid @RequestBody InventoryDetailRequestDto dto) {

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

        inventoryService.registerStoreInventory(dto, storeId);
        return BaseResponse.success("ok");
    }

    //dto로 정보 받아서 Inventory 저장
    @PostMapping("/registerInventory")
    public BaseResponse<String> registerInventory(HttpServletRequest request, @Valid @RequestBody InventoryDto.InventoryRegisterDto dto) {

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

        inventoryService.registerInventory(dto);
        return BaseResponse.success("ok");
    }

    @PostMapping("/totalInventory")
    public BaseResponse<String> totalInventory(HttpServletRequest request, @RequestBody TotalInventoryDto dto) {
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

        inventoryService.totalInventory(dto);
        return BaseResponse.success("ok");
    }

    @GetMapping("/DetailInventory/{storeInventoryId}")
    public BaseResponse<List<InventoryDto>> getDetailInventoryList(@PathVariable Long storeInventoryId) {
        List<InventoryDto> list = inventoryService.getInventoriesByStoreInventoryId(storeInventoryId);
        return BaseResponse.success(list);
    }

    @GetMapping("/totalInventory/{storeInventoryId}")
    public BaseResponse<List<TotalResponseDto.Response>> getDetailedTotalInventoryList(HttpServletRequest request, @PathVariable Long storeInventoryId) {
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

        // 입고 내역 리스트 조회
        List<TotalResponseDto.Response> list = inventoryService.getDetailedTotalInventoryList(storeId, storeInventoryId);
        return BaseResponse.success(list);
    }

    @PutMapping("/storeInventory")
    public BaseResponse<String> updateInventory(
            @RequestBody InventoryDetailRequestDto dto) {
        inventoryService.updateInventory(dto);
        return BaseResponse.success("수정되었습니다.");
    }

    @DeleteMapping("/storeInventory")
    public BaseResponse<String> deleteInventories(@RequestBody List<Long> inventoryIds) {
        inventoryService.deleteByIds(inventoryIds);
        return BaseResponse.success("선택한 재고가 성공적으로 삭제되었습니다.");
    }


    @GetMapping("/storeInventory/getList")
    public BaseResponse<Page<StoreInventoryDto.responseDto>> getAllStoreInventories(
            HttpServletRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword  // ✅ 추가
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<StoreInventoryDto.responseDto> result = inventoryService.getAllStoreInventories(getStoreId(request), pageable, keyword);
        return BaseResponse.success(result);
    }


    @GetMapping("/inventoryList")
    public BaseResponse<List<InventoryInfoDto.Response>> getInventoryList(HttpServletRequest request) {

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
        List<InventoryInfoDto.Response> inventoryList = inventoryService.getInventoryList(storeId);
        return BaseResponse.success(inventoryList);
    }
    // 재고 종류 리스트로 뽑기

    @PostMapping("/menuMarket")
    public BaseResponse<List<InventoryChangeDto.Response>> getInventoryChangeList(HttpServletRequest request, @RequestBody InventoryChangeDto.DateRequest dto) {

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
        List<InventoryChangeDto.Response> SaleList = inventoryService.getInventoryChangeList(storeId, dto);
        return BaseResponse.success(SaleList);
    }
    // 메뉴, 장터로 재고 얼마나 사용됐나

    @PostMapping("/updateSolo")
    public BaseResponse<List<InventoryChangeDto.Response>> getUpdateList(HttpServletRequest request, @RequestBody InventoryChangeDto.DateRequest dto) {

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
        List<InventoryChangeDto.Response> SaleList = inventoryService.getUpdateList(storeId, dto);
        return BaseResponse.success(SaleList);
    }
    // 수정으로 재고가 얼마나 변동했나 조회

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

    @GetMapping("/inventoryCall")
    public BaseResponse<InventoryCallDto.Response> getInventoryCall(HttpServletRequest request) {

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
        InventoryCallDto.Response inventoryCall = inventoryService.getInventoryCall(storeId);
        return BaseResponse.success(inventoryCall);
    }

    @GetMapping("/inventoryAmount")
    public BaseResponse<InventoryUpdateDto.Response> getTotalUpdateNumber(HttpServletRequest request) {

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
        InventoryUpdateDto.Response result = inventoryService.getTotalUpdateNumber(storeId);
        return BaseResponse.success(result);
    }
    // 이번주 재료 보정 얼마나 발생했는지

    @GetMapping("/marketAmount")
    public BaseResponse<InventoryNotUsed> getMaximumMarketPurchase(HttpServletRequest request) {

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
        InventoryNotUsed inventoryNotUsed = inventoryService.getMaximumMarketPurchase(storeId);
        return BaseResponse.success(inventoryNotUsed);
    }
    // 이번주 재료 보정 얼마나 발생했는지

    @PostMapping("/getRecipes")
    public BaseResponse<InventoryRecipes.Response> getRecipes(HttpServletRequest request, @RequestBody InventoryRecipes.Request req) {
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
        InventoryRecipes.Response result = inventoryService.getInventoryRecipes(storeId, req.getInventoryId());
        return BaseResponse.success(result);
    }

    @PostMapping("/validateOrder")
    public BaseResponse<String> validateOrder(HttpServletRequest request, @RequestBody InventoryValidateOrderDto dto) {
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

        List<String> insufficientItems = inventoryService.validateOrder(storeId, dto);

        Set<String> uniqueItems = new LinkedHashSet<>(insufficientItems);

        if (!uniqueItems.isEmpty()) {
            String message = "해당 재고가 부족할 수도 있어요. 조리 전 확인해주세요. \n" + String.join("", uniqueItems);
            return new BaseResponse<>(ErrorCode.INSUFFICIENT_INVENTORY.getStatus(), message, null);
        }

        // 부족한 재고가 없으면 정상 처리
        return BaseResponse.success("모든 재고가 충분합니다.");
    }

    @PutMapping("/inventory")
    public BaseResponse<String> updateInventory(@RequestBody @Valid InventoryDto.InventoryUpdateDto dto) {
        inventoryService.updateInventory(dto);
        return BaseResponse.success("재고 보정 완료");
    }
    @DeleteMapping("/inventory")
    public BaseResponse<String> deleteInventory(@RequestBody List<Long> inventoryid) {
        inventoryService.deleteById(inventoryid);
        return BaseResponse.success("선택한 재고가 성공적으로 삭제되었습니다.");
    }


}