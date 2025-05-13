package com.example.be12fin5verdosewmthisbe.inventory.repository;

import com.example.be12fin5verdosewmthisbe.inventory.model.Inventory;
import com.example.be12fin5verdosewmthisbe.inventory.model.StoreInventory;
import com.example.be12fin5verdosewmthisbe.inventory.model.dto.InventoryInfoDto;
import com.example.be12fin5verdosewmthisbe.inventory.model.dto.InventoryListDto;
import com.example.be12fin5verdosewmthisbe.inventory.model.dto.InventoryMenuUsageDto;
import com.example.be12fin5verdosewmthisbe.menu_management.menu.model.Recipe;
import com.example.be12fin5verdosewmthisbe.market_management.market.model.InventorySale;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.sql.Timestamp;
import java.util.List;

public interface StoreInventoryRepository extends JpaRepository<StoreInventory, Long> {
    boolean existsByStore_IdAndName(Long store_id, String name);

    List<StoreInventory> findByStore_Id(Long storeId);


    @Query(
            value = """
            select si
              from StoreInventory si
              join fetch si.store s
              join fetch s.user u
             where s.id = :storeId
               and si.name like %:keyword%
             order by si.id desc
        """,
            countQuery = """
            select count(si)
              from StoreInventory si
              where si.store.id = :storeId
                and si.name like %:keyword%
        """
    )
    Page<StoreInventory> findByStoreAndNameContainingWithFetch(
            @Param("storeId") Long storeId,
            @Param("keyword") String keyword,
            Pageable pageable
    );




    @Query("""
  SELECT si
  FROM StoreInventory si
  LEFT JOIN FETCH si.inventoryList inv
  WHERE si.store.id = :storeId
""")
    List<StoreInventory> findAllWithInventories(@Param("storeId") Long storeId);


    @Query("""
        SELECT DISTINCT si FROM StoreInventory si
        JOIN FETCH si.inventorySaleList is
        JOIN FETCH si.store s
        WHERE s.id = :storeId
        AND is.createdAt >= :start
        AND is.createdAt <= :end
    """)
    List<StoreInventory> findAllStoreInventoryByStoreAndPeroid(
            @Param("storeId") Long storeId,
            @Param("start") Timestamp start,
            @Param("end") Timestamp end
    );

    @Query("""
    SELECT new com.example.be12fin5verdosewmthisbe.inventory.model.dto.InventoryMenuUsageDto(
        si.name,
        SUM(CAST(r.quantity * om.quantity AS BIGDECIMAL)),
        si.unit,
        si.id
    )
    FROM StoreInventory si
    JOIN si.recipeList r
    JOIN r.menu m
    JOIN m.orderMenuList om
    JOIN om.order o
    WHERE si.store.id = :storeId
    AND o.createdAt BETWEEN :start AND :end
    GROUP BY si.name, si.unit, si.id
    ORDER BY SUM(r.quantity * om.quantity) DESC
""")
    List<InventoryMenuUsageDto> findAllMenuSaleInventoryByStoreAndPeroid(
            @Param("storeId") Long storeId,
            @Param("start") Timestamp start,
            @Param("end") Timestamp end
    );

    boolean existsByNameAndIdNot(@NotBlank(message = "재고명은 필수입니다.") String name, Long id);


    @Query("""
        select new com.example.be12fin5verdosewmthisbe.inventory.model.dto.InventoryListDto(
            si.id,
            si.name,
            si.quantity,
            si.unit,
            min(i.expiryDate),
            si.minQuantity
        )
        from StoreInventory si
        left join si.inventoryList i
        where si.store.id = :storeId
          and i.quantity > 0
        group by si.id, si.name, si.quantity, si.unit, si.minQuantity
    """)
    List<InventoryListDto> fetchInventoryInfoByStore(@Param("storeId") Long storeId);



    @Query("SELECT si FROM StoreInventory si WHERE si.store.id = :storeId")
    List<StoreInventory> findByStoreId(@Param("storeId") Long storeId);

    List<StoreInventory> findByStore_IdAndRecipeList(Long storeId, Recipe recipe);
}

