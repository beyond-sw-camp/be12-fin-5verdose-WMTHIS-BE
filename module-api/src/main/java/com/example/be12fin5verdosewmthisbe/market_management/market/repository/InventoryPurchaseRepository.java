package com.example.be12fin5verdosewmthisbe.market_management.market.repository;

import com.example.be12fin5verdosewmthisbe.market_management.market.model.InventoryPurchase;
import com.example.be12fin5verdosewmthisbe.market_management.market.model.InventorySale;
import com.example.be12fin5verdosewmthisbe.store.model.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

@Repository
public interface InventoryPurchaseRepository extends JpaRepository<InventoryPurchase, Long> {
    public List<InventoryPurchase> findInventoryPurchaseByStore(Store store);

    @Query("""
    SELECT ip.inventoryName, COUNT(ip)
    FROM InventoryPurchase ip
    WHERE ip.store = :store
    AND ip.status = :status
    AND ip.createdAt >= :start
    AND ip.createdAt <= :end
    GROUP BY ip.inventoryName
""")
    List<Object[]> findMostFrequentInventoryPurchaseByStoreAndPeriod(
            @Param("store") Store store,
            @Param("status") InventoryPurchase.purchaseStatus status,
            @Param("start") Timestamp start,
            @Param("end") Timestamp end
    );

}
        