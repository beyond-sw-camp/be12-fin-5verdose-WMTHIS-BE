package com.example.batch.main.core.repository;

import com.example.be12fin5verdosewmthisbe.order.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface SalesAnalysisRepository extends JpaRepository<Order, Long> {

    @Query("SELECT SUM(o.totalPrice) FROM Order o " +
            "WHERE o.store.id = :storeId " +
            "AND FUNCTION('DATE', o.createdAt) = :targetDate")
    Integer findTotalSalesByStoreAndDate(
            @Param("storeId") Long storeId,
            @Param("targetDate") LocalDate targetDate
    );

}