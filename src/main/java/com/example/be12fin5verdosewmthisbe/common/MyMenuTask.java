package com.example.be12fin5verdosewmthisbe.common;

import com.example.be12fin5verdosewmthisbe.menu_management.menu.model.Menu;
import com.example.be12fin5verdosewmthisbe.menu_management.menu.model.TopSellingMenu;
import com.example.be12fin5verdosewmthisbe.menu_management.menu.model.dto.TopMenuDto;
import com.example.be12fin5verdosewmthisbe.menu_management.menu.repository.MenuCountRepository;
import com.example.be12fin5verdosewmthisbe.menu_management.menu.repository.MenuRepository;
import com.example.be12fin5verdosewmthisbe.menu_management.menu.repository.TopSellingMenuRepository;
import com.example.be12fin5verdosewmthisbe.store.model.Store;
import com.example.be12fin5verdosewmthisbe.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class MyMenuTask {

    private final MenuCountRepository menuCountRepository;
    private final StoreRepository storeRepository;
    private final MenuRepository menuRepository;
    private final TopSellingMenuRepository topSellingMenuRepository;


    // 매일 새벽 1시에 각 가게에 전날 하루동안 가장 많이 팔린 메뉴 저장
    @Scheduled(cron = "0 0/10 * * * *")
    @Transactional
    public void runTask() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        Timestamp start = Timestamp.valueOf(yesterday.atStartOfDay());
        Timestamp end = Timestamp.valueOf(yesterday.plusDays(1).atStartOfDay().minusNanos(1));

        List<TopMenuDto> groupedResults = menuCountRepository.findTopMenusPerStore(start, end);

        // 중복 storeId 제거
        Map<Long, TopMenuDto> topPerStore = new HashMap<>();
        for (TopMenuDto dto : groupedResults) {
            topPerStore.putIfAbsent(dto.getStoreId(), dto);
        }

        // 미리 조회
        Set<Long> storeIds = topPerStore.values().stream()
                .map(TopMenuDto::getStoreId)
                .collect(Collectors.toSet());

        Set<Long> menuIds = topPerStore.values().stream()
                .map(TopMenuDto::getMenuId)
                .collect(Collectors.toSet());

        Map<Long, Store> storeMap = storeRepository.findAllById(storeIds).stream()
                .collect(Collectors.toMap(Store::getId, Function.identity()));

        Map<Long, Menu> menuMap = menuRepository.findAllById(menuIds).stream()
                .collect(Collectors.toMap(Menu::getId, Function.identity()));

        // TopSellingMenu 리스트 구성
        List<TopSellingMenu> toSaveList = topPerStore.values().stream()
                .map(dto -> TopSellingMenu.builder()
                        .store(storeMap.get(dto.getStoreId()))
                        .menu(menuMap.get(dto.getMenuId()))
                        .recordDate(yesterday)
                        .totalCount(dto.getTotalCount())
                        .build())
                .collect(Collectors.toList());

        topSellingMenuRepository.saveAll(toSaveList);
    }

}
