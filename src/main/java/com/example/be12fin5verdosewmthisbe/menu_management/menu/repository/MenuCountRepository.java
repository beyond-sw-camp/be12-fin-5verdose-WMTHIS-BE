package com.example.be12fin5verdosewmthisbe.menu_management.menu.repository;

import com.example.be12fin5verdosewmthisbe.inventory.model.UsedInventory;
import com.example.be12fin5verdosewmthisbe.menu_management.menu.model.Menu;
import com.example.be12fin5verdosewmthisbe.menu_management.menu.model.MenuCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

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

    Optional<MenuCount> findByMenuAndStoreId(Menu menu, Long storeId);
}
