package com.example.be12fin5verdosewmthisbe.order.repository;

import com.example.be12fin5verdosewmthisbe.order.model.Order;
import com.example.be12fin5verdosewmthisbe.order.model.OrderMenu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

@Repository
public interface OrderMenuRepository extends JpaRepository<OrderMenu, Long> {

        @Query("""
        SELECT m.name, SUM(om.quantity) as totalSold
        FROM OrderMenu om
        JOIN om.order o
        JOIN o.store s
        JOIN om.menu m
        JOIN m.category c
        JOIN c.store store
        WHERE store.id = :storeId
        AND o.createdAt BETWEEN :start AND :end
        GROUP BY m.name
        ORDER BY totalSold DESC
        """)
        List<Object[]> findBestSellingMenusByStoreAndPeriod(
                @Param("storeId") Long storeId,
                @Param("start") Timestamp start,
                @Param("end") Timestamp end
        );

}
