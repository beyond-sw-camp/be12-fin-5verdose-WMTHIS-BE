package com.example.be12fin5verdosewmthisbe.order.repository;

import com.example.be12fin5verdosewmthisbe.order.model.Order;
import com.example.be12fin5verdosewmthisbe.payment.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByStoreId(Long storeId);

    @Query("SELECT DISTINCT o FROM Order o " +
            "JOIN FETCH o.store s " +
            "WHERE s.id = :storeId " +
            "AND FUNCTION('DATE', o.createdAt) = :today")
    List<Order> findTodayOrderByStoreIdx(
            @Param("storeId") String storeId,
            @Param("today") LocalDate today
    );

    @Query("SELECT COALESCE(SUM(o.totalPrice), 0) FROM Order o WHERE o.createdAt BETWEEN :start AND :end")
    Integer sumTotalPriceBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    @Query("""
        SELECT o FROM Order o
        WHERE o.store.id = :storeId
        AND o.createdAt >= :start
        AND o.createdAt <= :end
    """)
    List<Order> findByCreatedAtBetween(
            @Param("storeId") Long storeId, @Param("start") Timestamp start
            ,@Param("end") Timestamp end
    );



}
        