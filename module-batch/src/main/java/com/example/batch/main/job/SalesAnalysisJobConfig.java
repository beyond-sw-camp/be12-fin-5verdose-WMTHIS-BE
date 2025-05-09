package com.example.batch.main.job;


import com.example.be12fin5verdosewmthisbe.dashboard.model.DashboardInfo;
import com.example.be12fin5verdosewmthisbe.dashboard.repository.DashboardInfoRepository;
import com.example.be12fin5verdosewmthisbe.inventory.model.StoreInventory;
import com.example.be12fin5verdosewmthisbe.inventory.model.dto.InventoryMenuUsageDto;
import com.example.be12fin5verdosewmthisbe.inventory.repository.StoreInventoryRepository;
import com.example.be12fin5verdosewmthisbe.market_management.market.model.InventoryPurchase;
import com.example.be12fin5verdosewmthisbe.market_management.market.model.InventorySale;
import com.example.be12fin5verdosewmthisbe.market_management.market.repository.InventoryPurchaseRepository;
import com.example.be12fin5verdosewmthisbe.menu_management.menu.repository.MenuCountRepository;
import com.example.be12fin5verdosewmthisbe.store.model.Store;
import com.example.be12fin5verdosewmthisbe.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.*;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@Configuration
@RequiredArgsConstructor
@EnableScheduling
@SpringBootApplication
@EntityScan(basePackages = {
        "com.example.be12fin5verdosewmthisbe.store.model",
        "com.example.be12fin5verdosewmthisbe.dashboard.model",
        "com.example.be12fin5verdosewmthisbe.menu_management.menu.model"
})
@EnableJpaRepositories(basePackages = {
        "com.example.be12fin5verdosewmthisbe.dashboard.repository",
        "com.example.be12fin5verdosewmthisbe.menu_management.menu.repository",  // 필요 시 추가
        "com.example.be12fin5verdosewmthisbe.store.repository",
        "com.example.be12fin5verdosewmthisbe.inventory.repository",
        "com.example.be12fin5verdosewmthisbe.market_management.market.repository",
        "com.example.batch.main.core.repository"                // 필요 시 추가
})
public class SalesAnalysisJobConfig {

    private final DataSource dataSource;
    private final DashboardInfoRepository dashboardInfoRepository;
    private final MenuCountRepository menuCountRepository;
    private final StoreRepository storeRepository;
    private final StoreInventoryRepository storeInventoryRepository;
    private final InventoryPurchaseRepository inventoryPurchaseRepository;

    @Bean
    public Step bestMenuTaskletStep(JobRepository jobRepository,
                                    PlatformTransactionManager transactionManager) {
        return new StepBuilder("bestMenuTaskletStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {

                    // 1️⃣ 집계 대상 기간: 어제 하루
                    LocalDate yesterday = LocalDate.now().minusDays(1);
                    Timestamp startTimestamp = Timestamp.valueOf(yesterday.atStartOfDay());
                    Timestamp endTimestamp = Timestamp.valueOf(yesterday.plusDays(1).atStartOfDay());

                    // ✅ 가게 리스트 조회 (모든 가게 처리)
                    List<Store> stores = storeRepository.findAll();

                    for (Store store : stores) {
                        Long storeId = store.getId();

                        // 2️⃣ 어제 최고 메뉴 1개만 조회
                        List<Object[]> topMenus = menuCountRepository.findTopMenusByStoreAndPeriod(
                                storeId, startTimestamp, endTimestamp);

                        String topMenuName = topMenus.size() > 0 ? (String) topMenus.get(0)[0] : null;

                        // 3️⃣ DashboardInfo 메시지 구성
                        String message;
                        if (topMenuName != null) {
                            message = "어제 제일 잘 나간 메뉴는 " + topMenuName + "입니다.";
                        } else {
                            message = "어제는 판매 기록이 없습니다.";
                        }

                        // 4️⃣ 기존 DashboardInfo 조회 후 업데이트 또는 새로 저장
                        Optional<DashboardInfo> existingInfoOpt = dashboardInfoRepository
                                .findByStoreIdAndStatus(storeId, DashboardInfo.InfoStatus.MENU);

                        DashboardInfo info;
                        if (existingInfoOpt.isPresent()) {
                            // 기존 데이터가 있으면 업데이트
                            info = existingInfoOpt.get();
                            info.setContent(message);
                            System.out.println("[" + store.getName() + "] 업데이트됨: " + message);
                        } else {
                            // 없으면 새로 생성
                            info = new DashboardInfo();
                            info.setStatus(DashboardInfo.InfoStatus.MENU);
                            info.setContent(message);
                            info.setStore(store);
                            System.out.println("[" + store.getName() + "] 새로 생성됨: " + message);
                        }

                        dashboardInfoRepository.save(info);
                    }

                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    @Bean
    public Step marketPurchaseAnalysisStep(JobRepository jobRepository,
                                           PlatformTransactionManager transactionManager) {
        return new StepBuilder("marketPurchaseAnalysisStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {

                    // ✅ 전체 가게 조회
                    List<Store> stores = storeRepository.findAll();

                    for (Store store : stores) {
                        Long storeId = store.getId();

                        LocalDate today = LocalDate.now();
                        LocalDate weekAgo = today.minusWeeks(1);

                        Timestamp startTimestamp = Timestamp.valueOf(weekAgo.atStartOfDay());
                        Timestamp endTimestamp = Timestamp.valueOf(LocalDateTime.now());

                        Map<String, BigDecimal> marketSale = new HashMap<>();
                        Map<String, BigDecimal> menuSale = new HashMap<>();
                        Map<String, String> menuUnit = new HashMap<>();

                        // 1️⃣ 모든 재고 이름 초기화
                        List<StoreInventory> storeInventoryList = storeInventoryRepository.findByStore_Id(storeId);
                        for (StoreInventory storeInventory : storeInventoryList) {
                            String name = storeInventory.getName();
                            marketSale.put(name, BigDecimal.ZERO);
                            menuSale.put(name, BigDecimal.ZERO);
                            menuUnit.put(name, "");
                        }

                        // 2️⃣ 장터 판매량 계산
                        List<StoreInventory> storeMarketInventoryList = storeInventoryRepository
                                .findAllStoreInventoryByStoreAndPeroid(storeId, startTimestamp, endTimestamp);

                        for (StoreInventory storeInventory : storeMarketInventoryList) {
                            String inventoryName = storeInventory.getName();
                            for (InventorySale inventorySale : storeInventory.getInventorySaleList()) {
                                BigDecimal current = marketSale.get(inventoryName);
                                marketSale.put(inventoryName, current.add(inventorySale.getQuantity()));
                            }
                        }
                        // 3️⃣ 메뉴 사용량 계산
                        List<InventoryMenuUsageDto> menuUsageList = storeInventoryRepository
                                .findAllMenuSaleInventoryByStoreAndPeroid(storeId, startTimestamp, endTimestamp);

                        for (InventoryMenuUsageDto dto : menuUsageList) {
                            menuSale.put(dto.getInventoryName(), dto.getTotalUsedQuantity());
                            menuUnit.put(dto.getInventoryName(), dto.getUnit());
                        }

                        // 4️⃣ 비율 계산
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

                        // ✅ 결과 메시지 구성 & 저장
                        String message;
                        if (bestInventory != null) {
                            message = "지난 일주일간 " + bestInventory + " (" + highest + unit + ") 을(를) 장터에 가장 많이 팔았습니다. 재료가 많이 남으면 발주량을 줄이는 건 어떠신가요?";
                        } else {
                            message = "지난 일주일간 장터에 판매한 재료가 없습니다.";
                        }

                        // 기존 DashboardInfo 조회 후 업데이트 또는 새로 저장
                        Optional<DashboardInfo> existingInfoOpt = dashboardInfoRepository
                                .findByStoreAndStatus(store, DashboardInfo.InfoStatus.MARKET);

                        DashboardInfo info;
                        if (existingInfoOpt.isPresent()) {
                            info = existingInfoOpt.get();
                            info.setContent(message);
                            System.out.println("[" + store.getName() + "] 업데이트됨: " + message);
                        } else {
                            info = new DashboardInfo();
                            info.setStore(store);
                            info.setStatus(DashboardInfo.InfoStatus.MARKET);
                            info.setContent(message);
                            System.out.println("[" + store.getName() + "] 새로 생성됨: " + message);
                        }

                        dashboardInfoRepository.save(info);
                    }

                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    @Bean
    public Step topPurchasedInventoryStep(JobRepository jobRepository,
                                          PlatformTransactionManager transactionManager) {
        return new StepBuilder("topPurchasedInventoryStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {

                    List<Store> stores = storeRepository.findAll();

                    for (Store store : stores) {

                        LocalDate today = LocalDate.now();
                        LocalDate weekAgo = today.minusWeeks(1);

                        Timestamp startTimestamp = Timestamp.valueOf(weekAgo.atStartOfDay());
                        Timestamp endTimestamp = Timestamp.valueOf(LocalDateTime.now());

                        // ✅ Store별로 그룹화된 (inventoryName, 구매 횟수) 가져오기
                        List<Object[]> resultList = inventoryPurchaseRepository.findMostFrequentInventoryPurchaseByStoreAndPeriod(
                                store,
                                InventoryPurchase.purchaseStatus.end,
                                startTimestamp,
                                endTimestamp
                        );

                        // ✅ 가장 자주 산 물품 찾기
                        String topInventory = null;
                        Long maxCount = 0L;

                        for (Object[] row : resultList) {
                            String inventoryName = (String) row[0];
                            Long count = (Long) row[1];

                            if (count > maxCount) {
                                maxCount = count;
                                topInventory = inventoryName;
                            }
                        }

                        // ✅ 메시지 만들기
                        String message;
                        if (topInventory != null) {
                            message = "지난 일주일간" + topInventory + " (" + maxCount + "회) 을(를) 가장 많이 구매했습니다. 추가 발주를 하시는건 어떠신가요?";
                        } else {
                            message = "지난 일주일간 장터에서 구매한 재료가 없습니다.";
                        }

                        // ✅ DashboardInfo 업데이트 or 생성
                        Optional<DashboardInfo> existingInfoOpt = dashboardInfoRepository
                                .findByStoreAndStatus(store, DashboardInfo.InfoStatus.INGREDIENT);

                        DashboardInfo info;
                        if (existingInfoOpt.isPresent()) {
                            info = existingInfoOpt.get();
                            info.setContent(message);
                            System.out.println("[" + store.getName() + "] 업데이트됨: " + message);
                        } else {
                            info = new DashboardInfo();
                            info.setStore(store);
                            info.setStatus(DashboardInfo.InfoStatus.INGREDIENT);
                            info.setContent(message);
                            System.out.println("[" + store.getName() + "] 새로 생성됨: " + message);
                        }

                        dashboardInfoRepository.save(info);
                    }

                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }



    @Bean
    public Job bestMenuTaskletJob(JobRepository jobRepository, Step bestMenuTaskletStep) {
        return new JobBuilder("bestMenuTaskletJob", jobRepository)
                .start(bestMenuTaskletStep)
                .build();
    }

    @Bean
    public Job marketPurchaseAnalysisJob(JobRepository jobRepository, Step marketPurchaseAnalysisStep) {
        return new JobBuilder("marketPurchaseAnalysisJob", jobRepository)
                .start(marketPurchaseAnalysisStep)
                .build();
    }

    @Bean
    public Job topPurchasedInventoryJob(JobRepository jobRepository, Step topPurchasedInventoryStep) {
        return new JobBuilder("topPurchasedInventoryJob", jobRepository)
                .start(topPurchasedInventoryStep)
                .build();
    }





}
