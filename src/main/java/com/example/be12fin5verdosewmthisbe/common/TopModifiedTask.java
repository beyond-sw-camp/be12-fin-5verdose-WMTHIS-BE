package com.example.be12fin5verdosewmthisbe.common;


import com.example.be12fin5verdosewmthisbe.inventory.model.ModifyInventory;
import com.example.be12fin5verdosewmthisbe.inventory.model.TopModifiedInventory;
import com.example.be12fin5verdosewmthisbe.inventory.repository.ModifyInventoryRepository;
import com.example.be12fin5verdosewmthisbe.inventory.repository.TopModifiedInventoryRepository;
import com.example.be12fin5verdosewmthisbe.store.model.Store;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class TopModifiedTask {

    private final TopModifiedInventoryRepository topModifiedInventoryRepository;
    private final ModifyInventoryRepository modifyInventoryRepository;

    @Scheduled(cron = "0 0/10 * * * *")
    @Transactional
    public void analyzeTopModifiedInventory() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        Timestamp start = Timestamp.valueOf(yesterday.atStartOfDay());
        Timestamp end = Timestamp.valueOf(yesterday.plusDays(1).atStartOfDay().minusNanos(1));

        List<ModifyInventory> list = modifyInventoryRepository.findTopModifiedInventories(start, end);

        Map<Long, ModifyInventory> topByStore = new HashMap<>();
        for (ModifyInventory mi : list) {
            Long storeId = mi.getStoreInventory().getStore().getId();
            if (!topByStore.containsKey(storeId)) {
                topByStore.put(storeId, mi); // 가장 먼저 나오는(변경률 높은) 것만 저장
            }
        }

        List<TopModifiedInventory> result = new ArrayList<>();
        for (ModifyInventory mi : topByStore.values()) {
            Store store = mi.getStoreInventory().getStore();
            result.add(TopModifiedInventory.builder()
                    .recordDate(yesterday)
                    .store(store)
                    .storeInventory(mi.getStoreInventory())
                    .modifyQuantity(mi.getModifyQuantity())
                    .modifyRate(mi.getModifyRate())
                    .build());
        }

        topModifiedInventoryRepository.saveAll(result);
    }

}
