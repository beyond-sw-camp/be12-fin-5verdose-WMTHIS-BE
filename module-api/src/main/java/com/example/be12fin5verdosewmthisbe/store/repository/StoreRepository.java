package com.example.be12fin5verdosewmthisbe.store.repository;

import com.example.be12fin5verdosewmthisbe.store.model.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StoreRepository extends JpaRepository<Store, Long> {
    // Your code here
    @Query("SELECT s.id FROM Store s WHERE s.id <> :storeId " +
            "AND s.latitude BETWEEN :minLat AND :maxLat " +
            "AND s.longitude BETWEEN :minLng AND :maxLng")
    List<Long> findNearbyStoreIds(@Param("storeId") Long storeId,
                                  @Param("minLat") Double minLat,
                                  @Param("maxLat") Double maxLat,
                                  @Param("minLng") Double minLng,
                                  @Param("maxLng") Double maxLng);

}
        