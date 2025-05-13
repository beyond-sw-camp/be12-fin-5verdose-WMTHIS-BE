package com.example.be12fin5verdosewmthisbe.store.repository;

import com.example.be12fin5verdosewmthisbe.store.model.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StoreRepository extends JpaRepository<Store, Long> {
    @Query(value = """
        SELECT s2.*
        FROM store s1
        JOIN store s2
          ON s2.id <> :storeId
        WHERE s1.id = :storeId
          AND ST_Distance_Sphere(s1.location, s2.location) <= :radiusInMeters
          AND EXISTS (
              SELECT 1
              FROM inventory_sale i
              WHERE i.store_id = s2.id
                AND i.status IN ('available','waiting')
          )
        """,
            nativeQuery = true
    )
    List<Store> findNearbyStoresByStoreId(
            @Param("storeId") Long storeId,
            @Param("radiusInMeters") double radiusInMeters
    );





}
        