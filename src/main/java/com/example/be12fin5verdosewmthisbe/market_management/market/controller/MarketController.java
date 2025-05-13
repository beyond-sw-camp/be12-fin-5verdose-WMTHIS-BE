package com.example.be12fin5verdosewmthisbe.market_management.market.controller;

import com.example.be12fin5verdosewmthisbe.common.BaseResponse;
import com.example.be12fin5verdosewmthisbe.common.CustomException;
import com.example.be12fin5verdosewmthisbe.common.ErrorCode;
import com.example.be12fin5verdosewmthisbe.inventory.model.Inventory;
import com.example.be12fin5verdosewmthisbe.inventory.service.InventoryService;
import com.example.be12fin5verdosewmthisbe.market_management.market.model.InventorySale;
import com.example.be12fin5verdosewmthisbe.market_management.market.model.dto.InventoryPurchaseDto;
import com.example.be12fin5verdosewmthisbe.market_management.market.model.dto.InventorySaleDto;
import com.example.be12fin5verdosewmthisbe.market_management.market.model.dto.TransactionDto;
import com.example.be12fin5verdosewmthisbe.market_management.market.service.MarketService;
import com.example.be12fin5verdosewmthisbe.security.JwtTokenProvider;
import com.example.be12fin5verdosewmthisbe.store.model.Store;
import com.example.be12fin5verdosewmthisbe.store.service.StoreService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/market")
@RequiredArgsConstructor
public class MarketController {
    private final MarketService marketService;
    private final JwtTokenProvider jwtTokenProvider;
    private final StoreService storeService;
    private final InventoryService inventoryService;

    @PostMapping("/registerSale")
    public BaseResponse<String> registerInventorySale(@RequestBody @Valid InventorySaleDto.InventorySaleRequestDto dto, HttpServletRequest request) {
        Inventory inventory = inventoryService.getFirstInventoryToUse(dto.getStoreInventoryId());
        if(inventory == null) {
            throw new CustomException(ErrorCode.INSUFFICIENT_INVENTORY);
        }
        marketService.saleRegister(dto,getStoreId(request),inventory);

        return BaseResponse.success("ok");
    }

    @PostMapping("/registerPurchase")
    public BaseResponse<String> registerInventoryPurchase(@RequestBody @Valid InventoryPurchaseDto.InventoryPurchaseRequestDto dto, HttpServletRequest request) {
        marketService.purchaseRegister(dto, getStoreId(request));
        return BaseResponse.success("ok");
    }

    @GetMapping("/get/{saleId}/purchaseList")
    public List<InventoryPurchaseDto.InventoryPurchaseResponseDto> getPurchasesBySaleId(@PathVariable Long saleId) {
        return marketService.getPurchasesBySaleId(saleId);
    }

    @GetMapping("/get/{saleId}/detail")
    public BaseResponse<InventorySaleDto.InventorySaleDetailDto> getSalesDetail(@PathVariable Long saleId) {
        InventorySale entity = marketService.findInventorySaleById(saleId);
        InventorySaleDto.InventorySaleDetailDto dto = InventorySaleDto.InventorySaleDetailDto.fromEntity(entity);
        return BaseResponse.success(dto);
    }


    @GetMapping("/approve")
    public BaseResponse<String> approve(
            @RequestParam Long saleId,
            @RequestParam Long purchaseId
    ) {
        marketService.approvePurchase(saleId, purchaseId);
        return BaseResponse.success("ok");
    }

    @GetMapping("/reject")
    public BaseResponse<String> reject(
            @RequestParam Long purchaseId
    ) {
        marketService.rejectPurchase(purchaseId);
        return BaseResponse.success("ok");
    }

    @GetMapping("/transaction")
    public BaseResponse<List<TransactionDto>> transaction(
            HttpServletRequest request,
            @RequestParam(required = false) String keyword) {
        List<TransactionDto> transactionDtoList = marketService.getAllTransactions(getStoreId(request),keyword);
        return BaseResponse.success(transactionDtoList.stream()
                .sorted(Comparator.comparing(TransactionDto::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed()) // 최신순으로 정렬
                .toList());
    }

    @PutMapping("/confirm")
    public BaseResponse<String> confirm(
            @RequestParam Long purchaseId
    ) {
        marketService.confirmEnd(purchaseId);
        return BaseResponse.success("ok");
    }
    @PostMapping("/images/upload")
    public BaseResponse<List<String>> uploadImages(@RequestParam("files") List<MultipartFile> files) {
        List<String> filePaths = new ArrayList<>();
        String uploadDir = "uploads/";

        for (MultipartFile file : files) {
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path path = Paths.get(uploadDir + fileName);
            try {
                Files.createDirectories(path.getParent());
                Files.write(path, file.getBytes());
                filePaths.add("/" + uploadDir + fileName); // 프론트에 보낼 경로
            } catch (IOException e) {
                e.printStackTrace();
                return BaseResponse.error(ErrorCode.ERROR_CODE);
            }
        }
        return BaseResponse.success(filePaths);
    }


    // 요청보낸 가게의 주변 3km이내의 가게만 보여줌
    @GetMapping("/getList")
    public BaseResponse<List<InventorySaleDto.InventorySaleListDto>> getList(HttpServletRequest request) {
        Long storeId = getStoreId(request);
        List<Store> storeList = storeService.getNearbyStoreIds(storeId);
        List<Long> storeIds = storeList.stream().map(Store::getId).toList();
        return BaseResponse.success(marketService.getNearbyAvailableSalesDto(storeIds,storeId));
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

}