package com.example.be12fin5verdosewmthisbe.inventory.repository;

import com.example.be12fin5verdosewmthisbe.inventory.model.ModifyInventory;
import com.example.be12fin5verdosewmthisbe.market_management.market.model.InventorySale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.sql.Timestamp;
import java.util.List;

public interface ModifyInventoryRepository extends JpaRepository<ModifyInventory, Integer> {


    @Query("""
        SELECT DISTINCT mi FROM ModifyInventory mi
        JOIN FETCH mi.storeInventory si
        JOIN FETCH si.store s
        WHERE s.id = :storeId
        AND mi.modifyDate >= :start
        AND mi.modifyDate <= :end
    """)
    List<ModifyInventory> findUpdateListByStoreAndPeriod(
            @Param("storeId") Long storeId,
            @Param("start") Timestamp start,
            @Param("end") Timestamp end
    );

    @Query("""
    SELECT mi
    FROM ModifyInventory mi
    WHERE mi.modifyDate BETWEEN :start AND :end
    AND mi.storeInventory.store.id IS NOT NULL
    ORDER BY mi.storeInventory.store.id ASC, ABS(mi.modifyRate) DESC
    """)
    List<ModifyInventory> findTopModifiedInventories(@Param("start") Timestamp start, @Param("end") Timestamp end);


    List<ModifyInventory> findByStoreInventory_IdIn(List<Long> storeInventoryIds);
}
