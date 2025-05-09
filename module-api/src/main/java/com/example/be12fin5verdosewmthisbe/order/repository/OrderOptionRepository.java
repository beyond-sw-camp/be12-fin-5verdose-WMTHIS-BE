package com.example.be12fin5verdosewmthisbe.order.repository;

import com.example.be12fin5verdosewmthisbe.order.model.OrderOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderOptionRepository extends JpaRepository<OrderOption, Long> {
    @Query("""
    SELECT oo
    FROM OrderOption oo
    JOIN FETCH oo.option op
    JOIN FETCH op.optionValueList ov
    JOIN FETCH ov.storeInventory si
    WHERE oo.orderMenu.id IN :orderMenuIds
""")
    List<OrderOption> findOrderOptionsByOrderMenuIds(@Param("orderMenuIds") List<Long> orderMenuIds);
}
