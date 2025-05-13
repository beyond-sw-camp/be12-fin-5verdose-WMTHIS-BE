package com.example.be12fin5verdosewmthisbe.common;

import com.example.be12fin5verdosewmthisbe.order.model.SalesComparison;
import com.example.be12fin5verdosewmthisbe.order.repository.OrderRepository;
import com.example.be12fin5verdosewmthisbe.order.repository.SalesComparisonRepository;
import com.example.be12fin5verdosewmthisbe.store.model.Store;
import com.example.be12fin5verdosewmthisbe.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SalesAnalysisTask {

    private final OrderRepository orderRepository;
    private final StoreRepository storeRepository;
    private final SalesComparisonRepository salesComparisonRepository;

    @Scheduled(cron = "0 0/10 * * * *")
    @Transactional
    public void analyzeSalesComparison() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        LocalDate weekAgo = LocalDate.now().minusDays(8);

        Timestamp yStart = Timestamp.valueOf(yesterday.atStartOfDay());
        Timestamp yEnd = Timestamp.valueOf(yesterday.plusDays(1).atStartOfDay().minusNanos(1));

        Timestamp wStart = Timestamp.valueOf(weekAgo.atStartOfDay());
        Timestamp wEnd = Timestamp.valueOf(weekAgo.plusDays(1).atStartOfDay().minusNanos(1));

        Map<Long, Long> yesterdaySales = toMap(orderRepository.findSalesByStoreBetween(yStart, yEnd));
        Map<Long, Long> weekAgoSales = toMap(orderRepository.findSalesByStoreBetween(wStart, wEnd));

        Set<Long> storeIds = new HashSet<>();
        storeIds.addAll(yesterdaySales.keySet());
        storeIds.addAll(weekAgoSales.keySet());

        Map<Long, Store> storeMap = storeRepository.findAllById(storeIds).stream()
                .collect(Collectors.toMap(Store::getId, Function.identity()));

        List<SalesComparison> comparisons = new ArrayList<>();

        for (Long storeId : storeIds) {
            Store store = storeMap.get(storeId);
            if (store == null) continue;

            int ySales = yesterdaySales.getOrDefault(storeId, 0L).intValue();
            int wSales = weekAgoSales.getOrDefault(storeId, 0L).intValue();
            int diff = ySales - wSales;
            double percent = (wSales == 0) ? 100.0 : (diff / (double) wSales) * 100.0;

            comparisons.add(SalesComparison.builder()
                    .store(store)
                    .targetDate(yesterday)
                    .yesterdaySales(ySales)
                    .weekAgoSales(wSales)
                    .difference(diff)
                    .percentageChange(percent)
                    .build());
        }

        salesComparisonRepository.saveAll(comparisons);
    }

    private Map<Long, Long> toMap(List<Object[]> rawData) {
        Map<Long, Long> map = new HashMap<>();
        for (Object[] row : rawData) {
            map.put((Long) row[0], ((Number) row[1]).longValue());
        }
        return map;
    }
}
