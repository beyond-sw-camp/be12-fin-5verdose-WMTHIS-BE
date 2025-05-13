package com.example.be12fin5verdosewmthisbe.inventory.repository;

import com.example.be12fin5verdosewmthisbe.inventory.model.Inventory;

import com.example.be12fin5verdosewmthisbe.inventory.model.StoreInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    // 유통기한 기준 오름차순 정렬된 리스트 반환
    List<Inventory> findByStoreInventory_IdOrderByExpiryDateAsc(Long storeInventoryId);

    // 유통기한이 가장 짧은 것 하나만 반환
    Optional<Inventory> findTopByStoreInventory_IdOrderByExpiryDateAsc(Long storeInventoryId);
    List<Inventory> findByStoreInventory_Store_Id(Long storeId);

    @Query("""
        SELECT DISTINCT i FROM Inventory i
        JOIN FETCH i.storeInventory si
        JOIN FETCH si.store s
        WHERE s.id = :storeId       
        AND i.id = :inventoryId
    """)
    List<Inventory> findByStoreInventoryStoreIdANDStoreInAndInventoryId(@Param("storeId") Long storeId, @Param("inventoryId") Long inventoryId);

    List<Inventory> findAllByStoreInventory(StoreInventory storeInventory);

    List<Inventory> findByStoreInventory_Id(Long storeInventoryId);

    @Query(value = """
    SELECT si.name, SUM(i.quantity) AS totalQuantity
    FROM inventory i
    JOIN store_inventory si ON i.store_inventory_id = si.store_inventory_id
    WHERE i.purchasedate BETWEEN :startDate AND :endDate
    GROUP BY si.name
    """, nativeQuery = true)
    List<Object[]> findTotalQuantityByItemBetweenDates(@Param("startDate") Timestamp startDate,
                                                       @Param("endDate") Timestamp endDate);


    @Query("""
    SELECT i
    FROM Inventory i
    JOIN FETCH i.storeInventory si
    WHERE si.id IN :storeInventoryIds
    ORDER BY i.expiryDate ASC
""")
    List<Inventory> findByStoreInventoryIdInOrderByExpiryDateAsc(
            @Param("storeInventoryIds") List<Long> storeInventoryIds
    );
    // 기간동안 입고된 inventory 종류별 수

}
