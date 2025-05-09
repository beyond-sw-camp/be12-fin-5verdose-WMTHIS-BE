package com.example.be12fin5verdosewmthisbe.market_management.market.repository;

import com.example.be12fin5verdosewmthisbe.market_management.market.model.InventorySale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

@Repository
public interface InventorySaleRepository extends JpaRepository<InventorySale, Long> {
    List<InventorySale> findByStore_IdAndStatusIn(Long StoreId, List<InventorySale.saleStatus> statuses);
    List<InventorySale> findByStore_Id(Long StoreId);
    List<InventorySale> findByStore_IdAndStatus(Long StoreId,InventorySale.saleStatus status);

    @Query("""
            SELECT DISTINCT s FROM InventorySale s
            JOIN FETCH s.store store
            LEFT JOIN FETCH s.imageList images
            WHERE s.status IN :statuses
            AND store.id IN :storeIds
            AND NOT EXISTS (
               SELECT 1 FROM InventoryPurchase p
               WHERE p.inventorySale = s AND p.store.id = :myStoreId
            )""")
    List<InventorySale> findVisibleSalesWithFetch(
            @Param("statuses") List<InventorySale.saleStatus> statuses,
            @Param("storeIds") List<Long> storeIds,
            @Param("myStoreId") Long myStoreId
    );


    @Query("""
        SELECT DISTINCT is FROM InventorySale is
        JOIN FETCH is.store s
        JOIN FETCH is.storeInventory si
        WHERE s.id = :storeId
        AND is.createdAt >= :start
        AND is.createdAt <= :end
    """)
    List<InventorySale> findMarketSaleForInventoryByStoreAndPeriod(
            @Param("storeId") Long storeId,
            @Param("start") Timestamp start,
            @Param("end") Timestamp end
    );


}
        