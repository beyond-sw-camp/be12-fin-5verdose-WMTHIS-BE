package com.example.be12fin5verdosewmthisbe.inventory.service;

import com.example.be12fin5verdosewmthisbe.common.CustomException;
import com.example.be12fin5verdosewmthisbe.common.ErrorCode;
import com.example.be12fin5verdosewmthisbe.inventory.model.*;
import com.example.be12fin5verdosewmthisbe.inventory.model.dto.*;
import com.example.be12fin5verdosewmthisbe.inventory.repository.InventoryRepository;
import com.example.be12fin5verdosewmthisbe.inventory.repository.ModifyInventoryRepository;
import com.example.be12fin5verdosewmthisbe.inventory.repository.StoreInventoryRepository;
import com.example.be12fin5verdosewmthisbe.inventory.repository.UsedInventoryRepository;
import com.example.be12fin5verdosewmthisbe.market_management.market.model.InventoryPurchase;
import com.example.be12fin5verdosewmthisbe.market_management.market.model.InventorySale;
import com.example.be12fin5verdosewmthisbe.market_management.market.repository.InventoryPurchaseRepository;
import com.example.be12fin5verdosewmthisbe.market_management.market.repository.InventorySaleRepository;
import com.example.be12fin5verdosewmthisbe.menu_management.menu.model.Menu;
import com.example.be12fin5verdosewmthisbe.menu_management.menu.model.Recipe;
import com.example.be12fin5verdosewmthisbe.menu_management.menu.repository.MenuRepository;
import com.example.be12fin5verdosewmthisbe.menu_management.menu.repository.RecipeRepository;
import com.example.be12fin5verdosewmthisbe.menu_management.option.model.Option;
import com.example.be12fin5verdosewmthisbe.menu_management.option.model.OptionValue;
import com.example.be12fin5verdosewmthisbe.menu_management.option.repository.OptionRepository;
import com.example.be12fin5verdosewmthisbe.menu_management.option.repository.OptionValueRepository;
import com.example.be12fin5verdosewmthisbe.order.model.Order;
import com.example.be12fin5verdosewmthisbe.order.model.OrderMenu;
import com.example.be12fin5verdosewmthisbe.order.model.OrderOption;
import com.example.be12fin5verdosewmthisbe.order.repository.OrderMenuRepository;
import com.example.be12fin5verdosewmthisbe.order.repository.OrderOptionRepository;
import com.example.be12fin5verdosewmthisbe.order.repository.OrderRepository;
import com.example.be12fin5verdosewmthisbe.redis.dto.RedisMenuDto;
import com.example.be12fin5verdosewmthisbe.redis.dto.RedisOptionDto;
import com.example.be12fin5verdosewmthisbe.redis.dto.RedisOptionValueDto;
import com.example.be12fin5verdosewmthisbe.redis.dto.RedisRecipeDto;
import com.example.be12fin5verdosewmthisbe.store.model.Store;
import com.example.be12fin5verdosewmthisbe.store.repository.StoreRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;


import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static ch.qos.logback.classic.spi.ThrowableProxyVO.build;
import static kotlinx.datetime.LocalDateTimeKt.toLocalDateTime;

@Service
@RequiredArgsConstructor
public class InventoryService {
    private final InventoryRepository inventoryRepository;
    private final StoreInventoryRepository storeInventoryRepository;
    private final OrderMenuRepository orderMenuRepository;
    private final InventorySaleRepository inventorySaleRepository;
    private final OptionRepository optionRepository;
    private final OptionValueRepository optionValueRepository;
    private final InventoryPurchaseRepository inventoryPurchaseRepository;
    private final ModifyInventoryRepository modifyInventoryRepository;
    private final StoreRepository storeRepository;
    private final RecipeRepository recipeRepository;
    private final MenuRepository menuRepository;
    private final OrderRepository orderRepository;
    private final OrderOptionRepository orderOptionRepository;
    private final UsedInventoryRepository usedInventoryRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    public StoreInventory registerStoreInventory(InventoryDetailRequestDto dto, Long storeId) {
        Store store = storeRepository.findById(storeId).orElseThrow(()->
                new CustomException(ErrorCode.STORE_NOT_EXIST));

        // 이름 중복 검사
        if (storeInventoryRepository.existsByStore_IdAndName(storeId,dto.getName())) {
            throw new CustomException(ErrorCode.INVENTORY_DUPLICATE_NAME);
        }

        try {
            StoreInventory newStoreInventory = StoreInventory.builder()
                    .name(dto.getName())
                    .minQuantity(dto.getMinQuantity())
                    .unit(dto.getUnit())
                    .store(store)
                    .quantity(BigDecimal.ZERO)
                    .expiryDate(dto.getExpiryDate())
                    .build();

            return storeInventoryRepository.save(newStoreInventory);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.INVENTORY_REGISTER_FAIL);
        }
    }

    public void registerInventory(InventoryDto.InventoryRegisterDto dto) {

        // 1. StoreInventory 조회
        StoreInventory storeInventory = storeInventoryRepository.findById(dto.getStoreInventoryId())
                .orElseThrow(() -> new CustomException(ErrorCode.STORE_INVENTORY_NOT_FOUND));

        // 2. unitPrice 계산 (가격 ÷ 수량 → 올림 처리)
        if (dto.getQuantity() == null || dto.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            throw new CustomException(ErrorCode.INVALID_QUANTITY);
        }
        BigDecimal pricePerUnit = new BigDecimal(dto.getPrice())
                .divide(dto.getQuantity(), 0, RoundingMode.CEILING); // 소수점 올림

        // 3. Inventory 생성
        Inventory inventory = Inventory.builder()
                .purchaseDate(new Timestamp(System.currentTimeMillis()))
                .expiryDate(LocalDate.now().plusDays(storeInventory.getExpiryDate()))
                .unitPrice(pricePerUnit.intValue())
                .quantity(dto.getQuantity())
                .storeInventory(storeInventory)
                .build();

        inventoryRepository.save(inventory);

        storeInventory.setQuantity(storeInventory.getQuantity().add(dto.getQuantity()));

        storeInventoryRepository.save(storeInventory);
    }


    public Inventory totalInventory(TotalInventoryDto dto) {
        // StoreInventory 객체 찾기
        StoreInventory storeInventory = storeInventoryRepository.findById(dto.getStoreInventoryId())
                .orElseThrow(() -> new CustomException(ErrorCode.STORE_INVENTORY_NOT_FOUND));
        LocalDate nowDate = LocalDate.now(); // 찐 유통기한
        LocalDate expriyDate;
        Integer expiryDateInt = dto.getExpiryDateInt();
        if(dto.getExpiryDateInt() == -1){
            expriyDate = nowDate.plusDays(storeInventory.getExpiryDate());
        } // DB에 저장된 유통기한과 같을 때
        else {
            if(expiryDateInt == null){
                throw new CustomException(ErrorCode.INVENTORY_REGISTER_FAIL);
            }
            else{
                expriyDate = nowDate.plusDays(expiryDateInt);
            }
        } // DB에 저장된 유통기한과 다를 때

        // DTO를 Inventory 엔티티로 변환
        Inventory inventory = dto.toEntity(storeInventory, dto, expriyDate);

        // Inventory 엔티티 저장
        Inventory savedInventory = inventoryRepository.save(inventory);

        // StoreInventory 객체 업데이트 (입고 후 재고 수량 업데이트)
        storeInventory.setQuantity(storeInventory.getQuantity().add(savedInventory.getQuantity()));
        storeInventoryRepository.save(storeInventory);

        return savedInventory;  // 등록된 Inventory 객체 반환
    }


    public List<InventoryDto> getInventoriesByStoreInventoryId(Long storeInventoryId) {
        List<Inventory> inventories = inventoryRepository.findByStoreInventory_Id(storeInventoryId);

        return inventories.stream()
                .map(inventory -> {
                    return InventoryDto.builder()
                            .id(inventory.getId())
                            .purchaseDate(inventory.getPurchaseDate())
                            .expiryDate(inventory.getExpiryDate())
                            .quantity(inventory.getQuantity())
                            .build();
                })
                .collect(Collectors.toList());
    }
    public void updateInventory(InventoryDetailRequestDto dto) {
        try {
            StoreInventory inventory = storeInventoryRepository.findById(dto.getStoreInventoryId())
                    .orElseThrow(() -> new CustomException(ErrorCode.INVENTORY_NOT_FOUND));
            boolean exists = storeInventoryRepository.existsByNameAndIdNot(dto.getName(), dto.getStoreInventoryId());
            if (exists) {
                throw new CustomException(ErrorCode.INVENTORY_DUPLICATE_NAME);
            }
            inventory.setName(dto.getName());
            inventory.setMinQuantity(dto.getMinQuantity());
            inventory.setUnit(dto.getUnit());
            inventory.setExpiryDate(dto.getExpiryDate());

            storeInventoryRepository.save(inventory);

        } catch (Exception e) {
            throw new CustomException(ErrorCode.INVENTORY_UPDATE_FAIL);
        }
    }



    @Transactional
    public void deleteByIds(List<Long> inventoryIds) {
        try {
            storeInventoryRepository.deleteAllById(inventoryIds);
        } catch (EmptyResultDataAccessException e) {
            throw new CustomException(ErrorCode.STORE_INVENTORY_NOT_FOUND);
        } catch (DataIntegrityViolationException e) {
            throw new CustomException(ErrorCode.CANNOT_DELETE_INVENTORY);
        }
    }
    public Page<StoreInventoryDto.responseDto> getAllStoreInventories(Long storeId, Pageable pageable, String keyword) {
        return storeInventoryRepository.findByStoreAndNameContainingWithFetch(storeId,keyword, pageable)
                .map(this::toDto);
    }

    private StoreInventoryDto.responseDto toDto(StoreInventory storeInventory) {
        return StoreInventoryDto.responseDto.builder()
                .id(storeInventory.getId())
                .name(storeInventory.getName())
                .expiryDate(storeInventory.getExpiryDate())
                .minQuantity(storeInventory.getMinQuantity())
                .quantity(storeInventory.getQuantity())
                .unit(storeInventory.getUnit())
                .build();
    }

    @Transactional
    public void deleteById(List<Long> inventoryid) {
        // 먼저 주어진 inventoryIds를 기준으로 해당하는 Inventory 목록을 조회
        List<Inventory> inventories = inventoryRepository.findAllById(inventoryid);

        if (inventories.isEmpty()) {
            throw new CustomException(ErrorCode.INVENTORY_NOT_FOUND);  // 조회된 재고가 없으면 예외 처리
        }

        try {
            // 해당 재고들을 삭제
            inventoryRepository.deleteAll(inventories);  // 삭제
        } catch (DataIntegrityViolationException e) {
            throw new CustomException(ErrorCode.CANNOT_DELETE_INVENTORY);  // 삭제할 수 없는 경우 예외 처리
        }
    }

    @Transactional
    public List<InventoryListDto> getInventoryList(Long storeId) {
        // StoreInventory 리스트를 가져옵니다.
        /*List<StoreInventory> inventoryList = storeInventoryRepository.findWithInventoryListByStore(storeId);
        List<InventoryInfoDto.Response> inventoryResponseList = new ArrayList<>();

        // inventoryList를 순회하며 Response 객체로 변환합니다.
        for (StoreInventory inventory : inventoryList) {
            String name = inventory.getName();
            BigDecimal quantity = inventory.getQuantity();
            String unit = inventory.getUnit();

            // 남은 수량이 0보다 큰 입고 항목 중 가장 빠른 유통기한을 가진 항목 가져오기
            Inventory firstInventory = inventoryRepository.findByStoreInventory_Id(inventory.getId()).stream()
                    .filter(i -> i.getQuantity() != null && i.getQuantity().compareTo(BigDecimal.ZERO) > 0)
                    .sorted(Comparator.comparing(Inventory::getExpiryDate))
                    .findFirst()
                    .orElse(null);

            LocalDate expiryDate = null;
            if (firstInventory != null) {
                expiryDate = firstInventory.getExpiryDate();
            }

            // Response 객체 생성
            InventoryInfoDto.Response inventoryResponse = InventoryInfoDto.Response.builder()
                    .id(inventory.getId())
                    .name(name)
                    .quantity(quantity)
                    .unit(unit)
                    .expiryDate(expiryDate)
                    .minQuantity(inventory.getMinQuantity())
                    .build();

            // 리스트에 추가
            inventoryResponseList.add(inventoryResponse);
        }

        return inventoryResponseList;  // 변환된 리스트 반환*/
        return storeInventoryRepository.fetchInventoryInfoByStore(storeId);
    }

    @Transactional
    public List<TotalResponseDto.Response> getDetailedTotalInventoryList(Long storeId, Long storeInventoryId) {
        List<Inventory> inventoryList = inventoryRepository.findByStoreInventoryStoreIdANDStoreInAndInventoryId(storeId, storeInventoryId);
        List<TotalResponseDto.Response> responseTotalInventoryList = new ArrayList<>();
        for(Inventory inventory : inventoryList) {
            LocalDate purhcaseDate = inventory.getPurchaseDate().toLocalDateTime().toLocalDate();
            TotalResponseDto.Response response = TotalResponseDto.Response.of(
                    purhcaseDate, inventory.getExpiryDate(),
                    inventory.getQuantity());
            responseTotalInventoryList.add(response);
        }

        return responseTotalInventoryList;
    }

    @Transactional
    public List<InventoryChangeDto.Response> getInventoryChangeList(Long storeId, InventoryChangeDto.DateRequest dto) {
        LocalDate startDate = dto.getStartDate();
        LocalDate endDate = dto.getEndDate();
        Timestamp startTimestamp = Timestamp.valueOf(startDate.atStartOfDay());
        Timestamp endTimestamp = Timestamp.valueOf(endDate.plusDays(1).atStartOfDay());

        List<InventoryChangeDto.Response> inventoryChangeList = new ArrayList<>();
        List<UsedInventory> usedInventoryList = usedInventoryRepository.findUsedInventoryByStoreAndPeriod(storeId, startTimestamp, endTimestamp);

        for (UsedInventory usedInventory : usedInventoryList) {
            Timestamp date = usedInventory.getUsedDate();
            String stockName = usedInventory.getStoreInventory().getName();
            String changeReason;
            Boolean isMenu = usedInventory.getStatus();
            BigDecimal quantity = usedInventory.getTotalquantity();
            String unit = usedInventory.getStoreInventory().getUnit();
            if (isMenu == true) {
                changeReason = "메뉴";
                quantity = quantity.negate();
            } else {
                if (quantity.compareTo(BigDecimal.ZERO) > 0) {
                    changeReason = "장터 구매";
                } else {
                    changeReason = "장터 판매";
                }
            }
            InventoryChangeDto.Response inventoryChange = InventoryChangeDto.Response.of(date, stockName, changeReason, quantity, unit);
            inventoryChangeList.add(inventoryChange);

        }

        return inventoryChangeList;
    }

    @Transactional
    public List<InventoryChangeDto.Response> getUpdateList(Long storeId, InventoryChangeDto.DateRequest dto) {

        LocalDate startDate = dto.getStartDate();
        LocalDate endDate = dto.getEndDate();
        Timestamp startTimestamp = Timestamp.valueOf(startDate.atStartOfDay());
        Timestamp endTimestamp = Timestamp.valueOf(endDate.plusDays(1).atStartOfDay());

        List<InventoryChangeDto.Response> updateSoloList = new ArrayList<>();
        List<ModifyInventory> updateList = modifyInventoryRepository.findUpdateListByStoreAndPeriod(storeId, startTimestamp, endTimestamp);

        for (ModifyInventory modifyInventory : updateList) {
            Timestamp date = modifyInventory.getModifyDate(); // 수정 날짜
            String stockName = modifyInventory.getStoreInventory().getName();
            String changeReasonq = "수정";
            BigDecimal quantity = modifyInventory.getModifyQuantity();
            String unit = modifyInventory.getStoreInventory().getUnit();
            InventoryChangeDto.Response saleResponse = InventoryChangeDto.Response.of(date, stockName, changeReasonq, quantity, unit);
            updateSoloList.add(saleResponse);
        }

        return (updateSoloList);
    }

    /**
     * 여러 개의 storeInventoryId 당 소비량을 한 번에 처리합니다.
     * @param usedInventoryQty Map<StoreInventory.id, 소비할 총 수량>
     */
    @Transactional
    public void consumeInventories(Map<Long, BigDecimal> usedInventoryQty) {
        // 1) 관련된 StoreInventory 엔티티들 한 번에 조회
        List<StoreInventory> storeInventories =
                storeInventoryRepository.findAllById(usedInventoryQty.keySet());
        // 검증: 존재하지 않는 ID가 있으면 에러
        if (storeInventories.size() != usedInventoryQty.size()) {
            throw new CustomException(ErrorCode.INVENTORY_NOT_FOUND);
        }

        // 2) 관련된 모든 Inventory(유통기한별 재고) 한 번에 조회 (expiryDate 오름차순 보장)
        List<Inventory> allInventories =
                inventoryRepository.findByStoreInventoryIdInOrderByExpiryDateAsc(
                        new ArrayList<>(usedInventoryQty.keySet())
                );
        // 그룹화: storeInventoryId → List<Inventory>
        Map<Long, List<Inventory>> inventoryGroup = allInventories.stream()
                .collect(Collectors.groupingBy(inv -> inv.getStoreInventory().getId()));

        // 3) 처리 후 일괄 저장/삭제할 목록 준비
        List<StoreInventory> inventoriesToSave = new ArrayList<>();
        List<Inventory> inventoriesToSaveDetail = new ArrayList<>();
        List<Inventory> inventoriesToDelete = new ArrayList<>();
        List<ModifyInventory> modifyInventoriesToSave = new ArrayList<>();

        // 4) 각 StoreInventory별로 소비 로직 실행
        for (StoreInventory si : storeInventories) {
            Long siId = si.getId();
            BigDecimal toConsume = usedInventoryQty.get(siId);
            BigDecimal newTotal = si.getQuantity().subtract(toConsume);
            if (newTotal.compareTo(BigDecimal.ZERO) < 0) {
                modifyInventoriesToSave.add(
                        ModifyInventory.builder()
                                .modifyDate(new Timestamp(System.currentTimeMillis()))
                                .modifyQuantity(newTotal)
                                .modifyRate(BigDecimal.valueOf(100))  // 전량 시도
                                .storeInventory(si)
                                .build()
                );
                // storeinventory 아래있는 inventory 다 삭제
                continue;
            }
            // 남은 총 재고 반영
            si.setQuantity(newTotal);
            inventoriesToSave.add(si);

            // 유통기한별 재고 차감
            BigDecimal remaining = toConsume;
            List<Inventory> group = inventoryGroup.getOrDefault(siId, List.of());
            for (Inventory inv : group) {
                if (remaining.compareTo(BigDecimal.ZERO) <= 0) break;
                BigDecimal avail = inv.getQuantity();
                if (avail.compareTo(remaining) <= 0) {
                    // 이 항목은 전부 소비 → 삭제
                    remaining = remaining.subtract(avail);
                    inventoriesToDelete.add(inv);
                } else {
                    // 일부만 소비 → 수량만 수정
                    inv.setQuantity(avail.subtract(remaining));
                    inventoriesToSaveDetail.add(inv);
                    remaining = BigDecimal.ZERO;
                }
            }
        }

        // 5) 일괄 반영
        storeInventoryRepository.saveAll(inventoriesToSave);
        inventoryRepository.saveAll(inventoriesToSaveDetail);
        inventoryRepository.deleteAll(inventoriesToDelete);
        modifyInventoryRepository.saveAll(modifyInventoriesToSave);
    }

    @Transactional
    public void consumeInventory(Long storeInventoryId, BigDecimal requestedQuantity) {
        List<Inventory> inventories = inventoryRepository
                .findByStoreInventory_IdOrderByExpiryDateAsc(storeInventoryId);

        StoreInventory storeInventory = storeInventoryRepository.findById(storeInventoryId)
                .orElseThrow(() -> new CustomException(ErrorCode.INVENTORY_NOT_FOUND));

        BigDecimal changeQuantity = storeInventory.getQuantity().subtract(requestedQuantity);

        if(changeQuantity.compareTo(BigDecimal.ZERO) < 0) {
            throw new CustomException(ErrorCode.INSUFFICIENT_INVENTORY);
        }
        storeInventory.setQuantity(changeQuantity);
        storeInventoryRepository.save(storeInventory);

        BigDecimal remaining = requestedQuantity;

        for (Inventory inventory : inventories) {
            if (remaining.compareTo(BigDecimal.ZERO) <= 0) break;

            BigDecimal available = inventory.getQuantity();

            if (available.compareTo(remaining) <= 0) {
                // 전부 쓰고 삭제
                remaining = remaining.subtract(available);
                inventoryRepository.delete(inventory);
            } else {
                // 일부만 사용
                inventory.setQuantity(available.subtract(remaining));
                inventoryRepository.save(inventory);
                remaining = BigDecimal.ZERO;
            }
        }

        if (remaining.compareTo(BigDecimal.ZERO) > 0) {
            throw new CustomException(ErrorCode.INSUFFICIENT_INVENTORY);
        }
    }
    // 전체를 유통기한 빠른 순으로
    public List<Inventory> getSortedInventoriesByExpiry(Long storeInventoryId) {
        return inventoryRepository.findByStoreInventory_IdOrderByExpiryDateAsc(storeInventoryId);
    }

    // 가장 먼저 써야 하는 재고 1개
    public Inventory getFirstInventoryToUse(Long storeInventoryId) {
        return inventoryRepository.findTopByStoreInventory_IdOrderByExpiryDateAsc(storeInventoryId)
                .orElse(null);
    }



    @Transactional
    public InventoryCallDto.Response getInventoryCall(Long storeId) {

        List<StoreInventory> storeInventoryList = storeInventoryRepository.findAllWithInventories(storeId);
        int expiringCount=0; // 만료 임박
        int reorderRequiredCount=0; // 발주 필요
        int receivedTodayCount=0;// 금일 입고
        LocalDate today = LocalDate.now();

        for (StoreInventory storeInventory : storeInventoryList) {
            int expiryDate = Optional.ofNullable(storeInventory.getExpiryDate()).orElse(0);
            if(expiryDate ==0){
                throw new CustomException(ErrorCode.STORE_INVENTORY_EXPIRY_NOT_FOUND);
            }
            List<Inventory> inventoryList = storeInventory.getInventoryList();
            for(Inventory inventory : inventoryList){
                LocalDate purchaseDate = inventory.getPurchaseDate().toLocalDateTime().toLocalDate(); //입고 날짜
                LocalDate eachExpiryDate = inventory.getExpiryDate();
                int daysBetween = (int) ChronoUnit.DAYS.between(today, eachExpiryDate);
                int daysTodayBetween = (int) ChronoUnit.DAYS.between(today, purchaseDate);

                if (daysBetween >= 0 && daysBetween <= 2) {
                    expiringCount++;
                }
                if(daysTodayBetween ==0){
                    receivedTodayCount++;
                }
            }
            // 만료 임박
            BigDecimal minQuantity = storeInventory.getMinQuantity();
            BigDecimal quantity = storeInventory.getQuantity();

            if(minQuantity.compareTo(quantity) > 0){
                reorderRequiredCount ++;
            }
        }

        InventoryCallDto.Response response = InventoryCallDto.Response.of(expiringCount, reorderRequiredCount, receivedTodayCount);

        return(response);
    }

    @Transactional
    public InventoryUpdateDto.Response getTotalUpdateNumber(Long storeId) {
        LocalDate today = LocalDate.now();
        LocalDate firstDayOfMonth = today.withDayOfMonth(1);

        Timestamp start = Timestamp.valueOf(firstDayOfMonth.atStartOfDay());
        Timestamp end   = Timestamp.valueOf(LocalDateTime.now());

        // 1) 기간 내 수정 이력 개수
        int totalUpdateNumber = modifyInventoryRepository
                .findUpdateListByStoreAndPeriod(storeId, start, end)
                .size();

        // 2) StoreInventory + Inventory 한 번에 조회 (1쿼리)
        List<StoreInventory> sis = storeInventoryRepository.findAllWithInventories(storeId);

        // 3) 모든 id 수집
        List<Long> storeInventoryIds = sis.stream()
                .map(StoreInventory::getId)
                .toList();

        // 4) ModifyInventory 일괄 조회 (1쿼리)
        List<ModifyInventory> allMods = modifyInventoryRepository.findByStoreInventory_IdIn(storeInventoryIds);

        // 5) storeInventoryId → List<ModifyInventory> 그룹핑
        Map<Long, List<ModifyInventory>> modsByInv = allMods.stream()
                .collect(Collectors.groupingBy(mi -> mi.getStoreInventory().getId()));

        // 6) 집계 로직
        int expiringCount = 0;
        List<InventoryUpdateDto.ItemQuantityDto> highModifyItems = new ArrayList<>();

        for (StoreInventory si : sis) {
            BigDecimal totalStockedQuantity = BigDecimal.ZERO;
            BigDecimal totalModifiedQuantity = BigDecimal.ZERO;

            for (Inventory inv : si.getInventoryList()) {
                Timestamp purchaseTs = inv.getPurchaseDate();
                LocalDate  purchaseDate = purchaseTs.toLocalDateTime().toLocalDate();
                LocalDate  expiryDate   = inv.getExpiryDate();

                if (!purchaseDate.isBefore(start.toLocalDateTime().toLocalDate()) &&
                        !purchaseDate.isAfter(end.toLocalDateTime().toLocalDate())) {
                    totalStockedQuantity = totalStockedQuantity.add(
                            Optional.ofNullable(inv.getQuantity()).orElse(BigDecimal.ZERO)
                    );

                    // 이전에 로드해 둔 맵에서 꺼내기
                    List<ModifyInventory> mods = modsByInv.getOrDefault(si.getId(), List.of());
                    for (ModifyInventory mi : mods) {
                        Timestamp modTs = mi.getModifyDate();
                        if (!modTs.before(start) && !modTs.after(end)) {
                            totalModifiedQuantity = totalModifiedQuantity.add(
                                    Optional.ofNullable(mi.getModifyQuantity())
                                            .map(BigDecimal::abs)
                                            .orElse(BigDecimal.ZERO)
                            );
                        }
                    }
                }
            }

            if (totalStockedQuantity.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal ratio = totalModifiedQuantity
                        .divide(totalStockedQuantity, 4, RoundingMode.HALF_UP);
                if (ratio.compareTo(BigDecimal.valueOf(0.1)) >= 0) {
                    highModifyItems.add(InventoryUpdateDto.ItemQuantityDto.builder()
                            .itemName(si.getName())
                            .totalQuantity(totalModifiedQuantity)
                            .build());
                }
            }
        }

        return InventoryUpdateDto.Response.builder()
                .total(totalUpdateNumber)
                .itemQuantityDtoList(highModifyItems)
                .build();
    }


    @Transactional
    public InventoryNotUsed getMaximumMarketPurchase(Long storeId) {

        LocalDate today = LocalDate.now();
        LocalDate monthAgo = today.minusMonths(1);

        Timestamp startTimestamp = Timestamp.valueOf(monthAgo.atStartOfDay());
        Timestamp endTimestamp = Timestamp.valueOf(LocalDateTime.now());

        Map<String, BigDecimal> marketSale = new HashMap<>();
        Map<String, BigDecimal> menuSale = new HashMap<>();
        Map<String, String> menuUnit = new HashMap<>();

        // 1. 모든 재고 이름 초기화
        List<StoreInventory> storeInventoryList = storeInventoryRepository.findByStore_Id(storeId);
        for (StoreInventory storeInventory : storeInventoryList) {
            String name = storeInventory.getName();
            marketSale.put(name, BigDecimal.ZERO);
            menuSale.put(name, BigDecimal.ZERO); // 초기화는 미리
            menuUnit.put(name,"");
        }

        // 2. 장터 판매량 계산
        List<StoreInventory> storeMarketInventoryList = storeInventoryRepository
                .findAllStoreInventoryByStoreAndPeroid(storeId, startTimestamp, endTimestamp);

        for (StoreInventory storeInventory : storeMarketInventoryList) {
            String inventoryName = storeInventory.getName();
            for (InventorySale inventorySale : storeInventory.getInventorySaleList()) {
                BigDecimal current = marketSale.get(inventoryName);
                marketSale.put(inventoryName, current.add(inventorySale.getQuantity()));
            }
        }

        // 3. 메뉴 사용량은 JPQL로 계산
        List<InventoryMenuUsageDto> menuUsageList = storeInventoryRepository
                .findAllMenuSaleInventoryByStoreAndPeroid(storeId, startTimestamp, endTimestamp);

        for (InventoryMenuUsageDto dto : menuUsageList) {
            menuSale.put(dto.getInventoryName(), dto.getTotalUsedQuantity());
            menuUnit.put(dto.getInventoryName(), dto.getUnit());
        }

        // 4. 비율 계산
        String bestInventory = null;
        BigDecimal highestRatio = BigDecimal.ZERO;
        BigDecimal highest = BigDecimal.ZERO;
        String unit = "";

        for (String name : marketSale.keySet()) {
            BigDecimal market = marketSale.get(name);
            BigDecimal menu = menuSale.get(name);
            String tempUnit = menuUnit.get(name);

            if (menu.compareTo(BigDecimal.ZERO) == 0) continue;

            BigDecimal ratio = market.divide(menu, 4, RoundingMode.HALF_UP);
            if (ratio.compareTo(highestRatio) > 0) {
                highestRatio = ratio;
                bestInventory = name;
                highest = market;
                unit = tempUnit;
            }
        }

        InventoryNotUsed inventoryNotUsed = InventoryNotUsed.of(bestInventory, highest, unit);
        return inventoryNotUsed;
    }


    public InventoryRecipes.Response getInventoryRecipes(Long storeId, Long inventoryId) {
        StoreInventory storeInventory = storeInventoryRepository.findById(inventoryId).orElseThrow(()->
                new CustomException(ErrorCode.INVENTORY_NOT_FOUND));
        List<Recipe> recipes = recipeRepository.findAllByStoreInventoryWithMenu(storeInventory);
        List<String> list = new ArrayList<>();
        for(Recipe recipe : recipes){
            list.add(recipe.getMenu().getName());
        }
        return InventoryRecipes.Response.from(list);
    }

    @Transactional
    public List<String> validateOrder(Long storeId, InventoryValidateOrderDto dto) {
        List<String> insufficientItems = new ArrayList<>();

        for (InventoryValidateOrderDto.OrderMenuRequest menuReq : dto.getOrderMenus()) {
            Long menuId = menuReq.getMenuId();
            String menuKey = "menu:" + menuId;

            // ✅ 메뉴 Redis에서 조회
            RedisMenuDto redisMenuDto = (RedisMenuDto) redisTemplate.opsForValue().get(menuKey);
            Menu menu;
            if (redisMenuDto == null) {
                menu = menuRepository.findById(menuId)
                        .orElseThrow(() -> new CustomException(ErrorCode.MENU_NOT_FOUND));
                redisMenuDto = RedisMenuDto.fromMenu(menu);
                redisTemplate.opsForValue().set(menuKey, redisMenuDto, Duration.ofHours(6));
            } else {
                menu = redisMenuDto.toMenu(storeInventoryRepository);
            }

            // ✅ 레시피 Redis에서 조회
            String recipeKey = "recipe:menu:" + menuId;
            List<RedisRecipeDto> redisRecipeDtos = (List<RedisRecipeDto>) redisTemplate.opsForValue().get(recipeKey);
            List<Recipe> recipes;
            if (redisRecipeDtos == null) {
                recipes = recipeRepository.findAllByMenu(menu);
                redisRecipeDtos = recipes.stream().map(Recipe::toRedisRecipeDto).collect(Collectors.toList());
                redisTemplate.opsForValue().set(recipeKey, redisRecipeDtos, Duration.ofHours(6));
            } else {
                recipes = redisRecipeDtos.stream()
                        .map(dtoItem -> dtoItem.toRecipe(storeInventoryRepository))
                        .collect(Collectors.toList());
            }

            for (Recipe recipe : recipes) {
                List<StoreInventory> ingredients = storeInventoryRepository.findByStore_IdAndRecipeList(storeId, recipe);

                BigDecimal requiredQuantity = recipe.getQuantity().multiply(BigDecimal.valueOf(menuReq.getQuantity()));
                BigDecimal availableQuantity = ingredients.stream()
                        .map(StoreInventory::getQuantity)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                if (availableQuantity.compareTo(requiredQuantity) < 0) {
                    String ingredientName = recipe.getStoreInventory().getName();
                    insufficientItems.add("[" + ingredientName + "]");
                }
            }

            // ✅ 옵션 Redis에서 확인
            if (menuReq.getOptionIds() != null) {
                for (Long optionId : menuReq.getOptionIds()) {
                    String optionKey = "option:" + optionId;
                    RedisOptionDto redisOptionDto = (RedisOptionDto) redisTemplate.opsForValue().get(optionKey);

                    if (redisOptionDto == null) {
                        Option option = optionRepository.findById(optionId)
                                .orElseThrow(() -> new CustomException(ErrorCode.OPTION_NOT_FOUND));
                        redisOptionDto = RedisOptionDto.fromOption(option);
                        redisTemplate.opsForValue().set(optionKey, redisOptionDto, Duration.ofHours(6));
                    }

                    for (RedisOptionValueDto valueDto : redisOptionDto.getOptionValues()) {
                        StoreInventory optionInventory = storeInventoryRepository.findById(valueDto.getStoreInventoryId())
                                .orElseThrow(() -> new CustomException(ErrorCode.INVENTORY_NOT_FOUND));

                        BigDecimal requiredOptionQuantity = valueDto.getQuantity().multiply(BigDecimal.valueOf(menuReq.getQuantity()));
                        if (optionInventory.getQuantity().compareTo(requiredOptionQuantity) < 0) {
                            insufficientItems.add("\n[" + redisOptionDto.getName() + "] 옵션 구성 재료가 부족할 수도 있어요.\n");
                        }
                    }
                }
            }
        }

        return insufficientItems;
    }
  
    @Transactional
    public void updateInventory(InventoryDto.InventoryUpdateDto dto) {
        Inventory inventory = inventoryRepository.findById(dto.getInventoryId())
                .orElseThrow(() -> new CustomException(ErrorCode.INVENTORY_NOT_FOUND));

        LocalDate start = inventory.getPurchaseDate().toLocalDateTime().toLocalDate();
        LocalDate end = dto.getExpiryDate();

        if (end.isBefore(start)) {
            throw new CustomException(ErrorCode.INVALID_EXPIRY_DATE);
        }

        // 변경량 계산
        BigDecimal beforeQuantity = inventory.getQuantity();
        BigDecimal afterQuantity = dto.getQuantity();
        BigDecimal changeQuantity = afterQuantity.subtract(beforeQuantity); // (수정 후 - 수정 전)

        // 기존 재고 수정
        inventory.setExpiryDate(dto.getExpiryDate());
        inventory.setQuantity(afterQuantity);
        inventoryRepository.save(inventory);


        StoreInventory storeInventory = inventory.getStoreInventory();

        BigDecimal totalQuantity = storeInventory.getInventoryList().stream()
                .map(Inventory::getQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        storeInventory.setQuantity(totalQuantity);
        storeInventoryRepository.save(storeInventory);

        BigDecimal modifyRate;
        if (beforeQuantity.compareTo(BigDecimal.ZERO) == 0) {
            modifyRate = BigDecimal.valueOf(100); // 0 → X 로 변경: 100% 증가로 간주
        } else {
            modifyRate = changeQuantity
                    .divide(beforeQuantity, 4, RoundingMode.HALF_UP)  // 소수점 4자리
                    .multiply(BigDecimal.valueOf(100));                // % 환산
        }

        // 변경 이력 저장
        ModifyInventory modifyInventory = ModifyInventory.builder()
                .modifyDate(new Timestamp(System.currentTimeMillis()))
                .modifyQuantity(changeQuantity)
                .modifyRate(modifyRate)
                .storeInventory(storeInventory)
                .build();

        modifyInventoryRepository.save(modifyInventory);
    }

}