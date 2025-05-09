package com.example.be12fin5verdosewmthisbe.menu_management.menu.repository;

import com.example.be12fin5verdosewmthisbe.inventory.model.UsedInventory;
import com.example.be12fin5verdosewmthisbe.menu_management.menu.model.MenuCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;

public interface MenuCountRepository extends JpaRepository<MenuCount, Long> {
    @Query(value = """
        SELECT m.name AS menuName, SUM(mc.count) AS totalCount
        FROM menu_count mc
        JOIN menu m ON mc.menu_id = m.id
        WHERE mc.store_id = :storeId
        AND mc.used_date >= :start
        AND mc.used_date < :end
        GROUP BY m.name
        ORDER BY totalCount DESC
        LIMIT 3
        """, nativeQuery = true)
    List<Object[]> findTopMenusByStoreAndPeriod(
            @Param("storeId") Long storeId,
            @Param("start") Timestamp start,
            @Param("end") Timestamp end
    );

    @Query("SELECT mc FROM MenuCount mc WHERE mc.store.id = :storeId AND DATE(mc.usedDate) = :date")
    List<MenuCount> findByStoreAndDate(@Param("storeId") Long storeId, @Param("date") LocalDate date);

}
