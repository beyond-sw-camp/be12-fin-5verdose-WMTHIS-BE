package com.example.marketservice.market.service;

import com.example.common.common.config.CustomException;
import com.example.common.common.config.ErrorCode;
import com.example.common.common.config.KafkaTopic;
import com.example.common.common.config.UnitConvertService;
import com.example.common.kafka.dto.ConsumeEvent;
import com.example.common.kafka.dto.InventoryRegisterEvent;
import com.example.common.kafka.dto.StoreInventoryCreateEvent;
import com.example.marketservice.market.model.*;
import com.example.marketservice.market.model.dto.InventoryPurchaseDto;
import com.example.marketservice.market.model.dto.InventorySaleDto;
import com.example.marketservice.market.model.dto.TransactionDto;
import com.example.marketservice.market.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MarketService {
    private final InventoryPurchaseRepository inventoryPurchaseRepository;
    private final InventorySaleRepository inventorySaleRepository;
    private final StoreRepository storeRepository;
    private final StoreInventoryRepository storeInventoryRepository;
    private final InventoryRepository inventoryRepository;
    private final UnitConvertService unitConvertService;

    private final KafkaTemplate<String, Object> kafkaTemplate;


    private static final double RADIUS_KM = 3.0;

    public void saleRegister(InventorySaleDto.InventorySaleRequestDto dto, Long storeId, Inventory inventory) {

        StoreInventory storeInventory = storeInventoryRepository.findById(dto.getStoreInventoryId())
                .orElseThrow(() -> new CustomException(ErrorCode.STORE_INVENTORY_NOT_FOUND));

        Store store = storeInventory.getStore();

        if (dto.getQuantity().compareTo(storeInventory.getQuantity()) > 0) {
            throw new CustomException(ErrorCode.INVALID_SALE_QUANTITY);
        }


        InventorySale inventorySale = InventorySale.builder()
                .inventoryName(storeInventory.getName())
                .storeInventory(storeInventory)
                .expiryDate(inventory.getExpiryDate())
                .sellerStoreName(store.getName())
                .quantity(dto.getQuantity())
                .unit(storeInventory.getUnit())
                .price(dto.getPrice())
                .status(InventorySale.saleStatus.valueOf("available"))
                .content(dto.getContent())
                .imageList(new ArrayList<>())
                .store(store)
                .createdAt(Timestamp.from(Instant.now()))
                .build();

        // 이미지 엔티티 생성 및 연결
        if (dto.getImageUrls() != null) {
            for (String url : dto.getImageUrls()) {
                Images image = Images.builder()
                        .url(url)
                        .inventorySale(inventorySale)
                        .build();
                inventorySale.getImageList().add(image);
            }
        }

        inventorySaleRepository.save(inventorySale);
    }
    public void purchaseRegister(InventoryPurchaseDto.InventoryPurchaseRequestDto dto,Long storeId) {
        InventorySale sale = inventorySaleRepository.findById(dto.getInventorySaleId())
                .orElseThrow(() -> new CustomException(ErrorCode.SALE_NOT_FOUND));
        StoreInventory storeInventory = null;
        if(dto.getStoreInventoryId() != null) {
            storeInventory = storeInventoryRepository.findById(dto.getStoreInventoryId())
                    .orElseThrow(() -> new CustomException(ErrorCode.STORE_INVENTORY_NOT_FOUND));
        }

        if(sale.getStatus() == InventorySale.saleStatus.valueOf("available")) {
            sale.setStatus(InventorySale.saleStatus.valueOf("waiting"));
            inventorySaleRepository.save(sale);
        }

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_EXIST));

        InventoryPurchase purchase = InventoryPurchase.builder()
                .inventoryName(dto.getInventoryName())
                .store(store)
                .quantity(dto.getQuantity())
                .price(dto.getPrice())
                .unit(sale.getUnit())
                .status(InventoryPurchase.purchaseStatus.PENDING_APPROVAL)
                .method(InventoryPurchase.purchaseMethod.valueOf(dto.getMethod()))
                .createdAt(Timestamp.from(Instant.now()))
                .inventorySale(sale)
                .storeInventory(storeInventory)
                .build();

        inventoryPurchaseRepository.save(purchase);
    }

    public InventorySale findInventorySaleById(Long id) {
        return inventorySaleRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.SALE_NOT_FOUND));
    }

    public Map<Long, List<InventorySaleDto.InventorySaleListDto>> getInventorySalesByStoreIds(List<Long> storeIds) {
        List<InventorySale> sales = inventorySaleRepository.findByStoreIdsAndStatus(storeIds, InventorySale.saleStatus.available);
        return sales.stream()
                .collect(Collectors.groupingBy(
                        sale -> sale.getStore().getId(),
                        Collectors.mapping(sale -> InventorySaleDto.InventorySaleListDto.builder()
                                .inventorySaleId(sale.getId())
                                .expirationDate(sale.getExpiryDate())
                                .createdDate(sale.getCreatedAt().toLocalDateTime().toLocalDate())
                                .inventoryName(sale.getInventoryName())
                                .sellerStoreName(sale.getSellerStoreName())
                                .price(sale.getPrice())
                                .quantity(sale.getQuantity().toString())
                                .build(), Collectors.toList())
                ));
    }

    public List<InventorySale> findInventorySaleBySellerStoreId(Long sellerStoreId) {
        return inventorySaleRepository.findByStore_Id(sellerStoreId);
    }
    public List<InventoryPurchase> findInventoryPurchaseByBuyerStoreId(Store store) {
        return inventoryPurchaseRepository.findInventoryPurchaseByStore(store);
    }
    @Transactional
    public List<TransactionDto> getAllTransactions(Long storeId,String keyword) {
        List<InventorySale> inventorySaleList = findInventorySaleBySellerStoreId(storeId);

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_EXIST));

        List<InventoryPurchase> inventoryPurchaseList = findInventoryPurchaseByBuyerStoreId(store);

        List<TransactionDto> saleTransactionDtoList = new ArrayList<>(inventorySaleList.stream()
                .map(sale -> {
                    return TransactionDto.builder()
                            .inventorySaleId(sale.getId())
                            .name(sale.getInventoryName())
                            .price(sale.getPrice())
                            .type(true)
                            .quantity(sale.getQuantity())
                            .status(String.valueOf(sale.getStatus()))
                            .otherStoreName(sale.getBuyerStoreName())
                            .createdAt(sale.getCreatedAt().toLocalDateTime().toLocalDate())
                            .build();
                }).toList());
        List<TransactionDto> purchaseTransactionDtoList = inventoryPurchaseList.stream()
                .map(sale -> {
                    return TransactionDto.builder()
                            .inventoryPurchaseId(sale.getId())
                            .name(sale.getInventorySale().getInventoryName()) // n+1예상
                            .price(sale.getPrice())
                            .type(false)
                            .quantity(sale.getQuantity())
                            .status(String.valueOf(sale.getStatus()))
                            .createdAt(sale.getCreatedAt().toLocalDateTime().toLocalDate())
                            .otherStoreName(sale.getInventorySale().getSellerStoreName())
                            .build();
                }).toList();

        List<TransactionDto> allTransactions = new ArrayList<>();
        allTransactions.addAll(saleTransactionDtoList);
        allTransactions.addAll(purchaseTransactionDtoList);

        return allTransactions.stream()
                .filter(dto -> keyword == null || dto.getName().toLowerCase().contains(keyword.toLowerCase()))
                .toList();
    }

    public void confirmEnd(Long purchaseId) {
        InventoryPurchase inventoryPurchase = inventoryPurchaseRepository.findById(purchaseId)
                .orElseThrow(() -> new CustomException(ErrorCode.PURCHASE_NOT_FOUND));
        InventorySale inventorySale = inventoryPurchase.getInventorySale();
        inventoryPurchase.setStatus(InventoryPurchase.purchaseStatus.end);
        inventorySale.setStatus(InventorySale.saleStatus.sold);

        inventorySaleRepository.save(inventorySale);
        inventoryPurchaseRepository.save(inventoryPurchase);
        StoreInventory storeInventory = inventoryPurchase.getStoreInventory();
        addInventory(inventoryPurchase,storeInventory,inventorySale);
    }

    @Transactional
    public void approvePurchase(Long saleId, Long purchaseId) {
        InventorySale sale = inventorySaleRepository.findById(saleId)
                .orElseThrow(() -> new CustomException(ErrorCode.SALE_NOT_FOUND));

        List<InventoryPurchase> purchases = sale.getPurchaseList();
        StoreInventory storeInventory = sale.getStoreInventory();

        boolean found = false;
        for (InventoryPurchase purchase : purchases) {
            if (purchase.getId().equals(purchaseId)) {
                sale.setPrice(purchase.getPrice());
                sale.setQuantity(purchase.getQuantity());
                sale.setBuyerStoreName(purchase.getStore().getName());
                if(purchase.getMethod().equals(InventoryPurchase.purchaseMethod.cash)) {
                    sale.setStatus(InventorySale.saleStatus.delivery);
                    purchase.setStatus(InventoryPurchase.purchaseStatus.confirmDelivery);
                } else {
                    sale.setStatus(InventorySale.saleStatus.isPaymentPending);
                    purchase.setStatus(InventoryPurchase.purchaseStatus.isPaymentInProgress);
                }
                // 재고 차감 로직 추가해야함 카프카로
                ConsumeEvent consumeEvent = new ConsumeEvent(
                        sale.getStoreInventory().getId(),
                        sale.getQuantity()
                );
                kafkaTemplate.send(KafkaTopic.INVENTORY_CONSUME_TOPIC,consumeEvent);

                sale.setInventoryPurchaseId(purchaseId);
                inventorySaleRepository.save(sale);
                found = true;
            } else {
                purchase.setStatus(InventoryPurchase.purchaseStatus.cancelled);
            }
        }

        if (!found) {
            throw new CustomException(ErrorCode.PURCHASE_NOT_FOUND);
        }

        inventoryPurchaseRepository.saveAll(purchases);
    }

    public void rejectPurchase(Long purchaseId) {
        InventoryPurchase inventoryPurchase = inventoryPurchaseRepository.findById(purchaseId)
                .orElseThrow(() -> new CustomException(ErrorCode.PURCHASE_NOT_FOUND));

        inventoryPurchase.setStatus(InventoryPurchase.purchaseStatus.cancelled);
        inventoryPurchaseRepository.save(inventoryPurchase);
    }

    public List<InventoryPurchaseDto.InventoryPurchaseResponseDto> getPurchasesBySaleId(Long saleId) {
        InventorySale sale = inventorySaleRepository.findById(saleId)
                .orElseThrow(() -> new CustomException(ErrorCode.SALE_NOT_FOUND));

        return sale.getPurchaseList().stream()
                .filter(purchase -> purchase.getStatus() == InventoryPurchase.purchaseStatus.PENDING_APPROVAL) // 상태가 WAITING인 것만 필터링
                .map(purchase -> {
                    String buyerName = storeRepository.findById(purchase.getStore().getId())
                            .map(Store::getName)
                            .orElse("알 수 없음");
                    return new InventoryPurchaseDto.InventoryPurchaseResponseDto(purchase, buyerName);
                })
                .toList();
    }

    public List<InventorySaleDto.InventorySaleListDto> getNearbyAvailableSalesDto(List<Long> nearbyStoreIds, Long myStoreId) {
        List<InventorySale.saleStatus> targetStatuses = List.of(
                InventorySale.saleStatus.available,
                InventorySale.saleStatus.waiting
        );

        List<InventorySale> sales = inventorySaleRepository.findVisibleSalesWithFetch(
                targetStatuses, nearbyStoreIds, myStoreId
        );

        return convertToDtoList(sales);
    }


    public List<InventorySaleDto.InventorySaleListDto> convertToDtoList(List<InventorySale> sales) {

        return sales.stream()
                .map(sale -> new InventorySaleDto.InventorySaleListDto(

                        sale.getId(),
                        sale.getInventoryName(),
                        sale.getQuantity().toPlainString() + sale.getStoreInventory().getUnit(),  // BigDecimal → String
                        sale.getExpiryDate(),
                        sale.getPrice(),
                        sale.getCreatedAt().toLocalDateTime().toLocalDate(), // Timestamp → LocalDate
                        sale.getSellerStoreName()
                ))
                .collect(Collectors.toList());
    }
    public InventoryPurchase findPurchaseById(Long purchaseId) {
        return inventoryPurchaseRepository.findById(purchaseId)
                .orElseThrow(() -> new CustomException(ErrorCode.PURCHASE_NOT_FOUND));
    }
    public void statusChange(Long purchaseId) {
        InventoryPurchase inventoryPurchase = inventoryPurchaseRepository.findById(purchaseId)
                .orElseThrow(()-> new CustomException(ErrorCode.PURCHASE_NOT_FOUND));
        inventoryPurchase.setStatus(InventoryPurchase.purchaseStatus.confirmDelivery);
        InventorySale inventorySale = inventoryPurchase.getInventorySale();
        inventorySale.setStatus(InventorySale.saleStatus.delivery);
        inventoryPurchaseRepository.save(inventoryPurchase);
        inventorySaleRepository.save(inventorySale);

        StoreInventory storeInventory = inventoryPurchase.getStoreInventory();

        addInventory(inventoryPurchase,storeInventory,inventorySale);
    }
    public void addInventory(InventoryPurchase inventoryPurchase,StoreInventory storeInventory,InventorySale inventorySale) {
        InventoryRegisterEvent registerEvent = null;

        // 단위변환이 성공하면
        if(storeInventory != null && unitConvertService.canConvert(inventoryPurchase.getUnit(),storeInventory.getUnit())) {
            BigDecimal addition = unitConvertService.convert(inventoryPurchase.getQuantity(),inventoryPurchase.getUnit(),storeInventory.getUnit());

            registerEvent = new InventoryRegisterEvent(
                    storeInventory.getId(),
                    addition,
                    inventoryPurchase.getPrice()
                    );
            publishRegisterEvent(registerEvent);
        } else { // 단위변환이 실패하면 그냥 새로 넣기
            StoreInventory storeInventory2 = inventorySale.getStoreInventory();
            StoreInventoryCreateEvent createEvt = new StoreInventoryCreateEvent(
                    inventoryPurchase.getStore().getId(),
                    storeInventory2.getUnit(),
                    storeInventory2.getName(),
                    storeInventory2.getMinQuantity(),
                    BigDecimal.ZERO,
                    storeInventory2.getExpiryDate(),
                    inventoryPurchase.getQuantity(),
                    inventoryPurchase.getPrice()
            );
            kafkaTemplate.send(KafkaTopic.MARKET_STORE_INVENTORY_CREATE_TOPIC,
                    inventoryPurchase.getStore().getId().toString(), createEvt
            );
        }
    }


    public void publishRegisterEvent(InventoryRegisterEvent registerEvent) {
        InventoryRegisterEvent event = new InventoryRegisterEvent(
                registerEvent.getStoreInventoryId(),
                registerEvent.getQuantity(),
                registerEvent.getPrice()
        );
        kafkaTemplate.send(KafkaTopic.MARKET_INVENTORY_CREATE_TOPIC, registerEvent.getStoreInventoryId().toString(), event);
        log.info("Published InventoryRegisterEvent: {}", event);
    }


    public List<Store> getNearbyStoreIds(Long storeId) {
        double radiusInMeters = RADIUS_KM * 1000;
        return storeRepository.findNearbyStoresByStoreId(storeId, radiusInMeters);
    }

    public Inventory getFirstInventoryToUse(Long storeInventoryId) {
        return inventoryRepository.findTopByStoreInventory_IdOrderByExpiryDateAsc(storeInventoryId)
                .orElse(null);
    }
}