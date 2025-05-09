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
import org.springframework.stereotype.Service;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;


import java.util.*;
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
                            .id(inventory.getInventoryId())
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
        return storeInventoryRepository.findByStore_IdAndNameContaining(storeId,keyword, pageable)
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
    public List<InventoryInfoDto.Response> getInventoryList(Long storeId) {
        // StoreInventory 리스트를 가져옵니다.
        List<StoreInventory> inventoryList = storeInventoryRepository.findInventoryListByStore(storeId);
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

        return inventoryResponseList;  // 변환된 리스트 반환
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
            String stockName = modifyInventory.getInventory().getStoreInventory().getName();
            String changeReasonq = "수정";
            BigDecimal quantity = modifyInventory.getModifyQuantity();
            String unit = modifyInventory.getInventory().getStoreInventory().getUnit();
            InventoryChangeDto.Response saleResponse = InventoryChangeDto.Response.of(date, stockName, changeReasonq, quantity, unit);
            updateSoloList.add(saleResponse);
        }

        return (updateSoloList);
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

        List<StoreInventory> storeInventoryList = storeInventoryRepository.findAllStoreInventoryByStore(storeId);
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

        Timestamp startTimestamp = Timestamp.valueOf(firstDayOfMonth.atStartOfDay());
        Timestamp endTimestamp = Timestamp.valueOf(LocalDateTime.now());

        int totalUpdateNumber = 0;

        // 1. 수정된 재고 조회
        List<ModifyInventory> modifyInventoryList = modifyInventoryRepository.findUpdateListByStoreAndPeriod(storeId, startTimestamp, endTimestamp);

        totalUpdateNumber = modifyInventoryList.size(); // 단순 사이즈로 수정

        List<InventoryUpdateDto.ItemQuantityDto> highModifyItems = new ArrayList<>();

        // 2. 매장(storeId)의 StoreInventory 가져오기
        List<StoreInventory> storeInventories = storeInventoryRepository.findAllByStoreId(storeId);

        for (StoreInventory storeInventory : storeInventories) {
            BigDecimal totalStockedQuantity = BigDecimal.ZERO;
            BigDecimal totalModifiedQuantity = BigDecimal.ZERO;

            // 3. 해당 StoreInventory의 Inventory 리스트 가져오  기
            List<Inventory> inventories = storeInventory.getInventoryList();

            for (Inventory inventory : inventories) {
                if (inventory.getPurchaseDate().after(startTimestamp) && inventory.getPurchaseDate().before(endTimestamp)) {
                    totalStockedQuantity = totalStockedQuantity.add(
                            inventory.getQuantity() != null ? inventory.getQuantity() : BigDecimal.ZERO
                    );

                    for (ModifyInventory modifyInventory : inventory.getModifyInventoryList()) {
                        if (modifyInventory.getModifyDate().after(startTimestamp) && modifyInventory.getModifyDate().before(endTimestamp)) {
                            // ❗ 수정된 양은 절대값으로 누적해야 한다!
                            totalModifiedQuantity = totalModifiedQuantity.add(
                                    modifyInventory.getModifyQuantity() != null ? modifyInventory.getModifyQuantity().abs() : BigDecimal.ZERO
                            );
                        }
                    }
                }
            }

            if (totalStockedQuantity.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal ratio = totalModifiedQuantity.divide(totalStockedQuantity, 4, RoundingMode.HALF_UP);
                if (ratio.compareTo(BigDecimal.valueOf(0.1)) >= 0) {
                    highModifyItems.add(InventoryUpdateDto.ItemQuantityDto.builder()
                            .itemName(storeInventory.getName())
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
        List<Recipe> recipes = recipeRepository.findAllByStoreInventory(storeInventory);
        List<String> list = new ArrayList<>();
        for(Recipe recipe : recipes){
            Menu menu = menuRepository.findById(recipe.getMenu().getId()).orElseThrow(()->
                    new CustomException(ErrorCode.MENU_NOT_FOUND));;
            list.add(menu.getName());
        }
        return InventoryRecipes.Response.from(list);
    }

    @Transactional
    public List<String> validateOrder(Long storeId, InventoryValidateOrderDto dto) {
        List<String> insufficientItems = new ArrayList<>();

        for (InventoryValidateOrderDto.OrderMenuRequest menuReq : dto.getOrderMenus()) {
            Menu menu = menuRepository.findById(menuReq.getMenuId())
                    .orElseThrow(() -> new CustomException(ErrorCode.MENU_NOT_FOUND));

            // 레시피 기반 재고 확인
            List<Recipe> recipes = recipeRepository.findAllByMenu(menu);
            for (Recipe recipe : recipes) {
                List<StoreInventory> ingredients = storeInventoryRepository.findByStore_IdAndRecipeList(storeId, recipe);

                BigDecimal requiredQuantity = recipe.getQuantity().multiply(BigDecimal.valueOf(menuReq.getQuantity()));
                BigDecimal availableQuantity = ingredients.stream()
                        .map(StoreInventory::getQuantity)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                if (availableQuantity.compareTo(requiredQuantity) < 0) {
                    // 부족한 재고 항목을 리스트에 추가
                    String ingredientName = recipe.getStoreInventory().getName(); // StoreInventory에서 재료 이름 가져오기
                    insufficientItems.add("[" + ingredientName +"]");
                }
            }

            // 옵션 기반 재고 확인
            if (menuReq.getOptionIds() != null) {
                for (Long optionId : menuReq.getOptionIds()) {
                    Option option = optionRepository.findById(optionId)
                            .orElseThrow(() -> new CustomException(ErrorCode.OPTION_NOT_FOUND));

                    List<OptionValue> optionValues = optionValueRepository.findAllByOption(option);
                    for (OptionValue optionValue : optionValues) {
                        StoreInventory optionInventory = optionValue.getStoreInventory();
                        BigDecimal requiredOptionQuantity = optionValue.getQuantity().multiply(BigDecimal.valueOf(menuReq.getQuantity()));

                        if (optionInventory.getQuantity().compareTo(requiredOptionQuantity) < 0) {
                            // 옵션 재고 부족 항목 추가
                            insufficientItems.add("\n["+ option.getName() +"]" + " 옵션 구성 재료가 부족할 수도 있어요.\n");
                        }
                    }
                }
            }
        }

        // 부족한 항목들을 리스트로 반환
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

        // 변경 이력 저장
        ModifyInventory modifyInventory = ModifyInventory.builder()
                .modifyDate(new Timestamp(System.currentTimeMillis()))
                .modifyQuantity(changeQuantity)
                .inventory(inventory)
                .build();

        modifyInventoryRepository.save(modifyInventory);
    }

}