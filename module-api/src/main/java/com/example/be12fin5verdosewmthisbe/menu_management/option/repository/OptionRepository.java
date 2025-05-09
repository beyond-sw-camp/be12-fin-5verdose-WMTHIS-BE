package com.example.be12fin5verdosewmthisbe.menu_management.option.repository;

import com.example.be12fin5verdosewmthisbe.menu_management.option.model.Option;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OptionRepository extends JpaRepository<Option, Long> {
    Page<Option> findByStoreId(Long storeId, Pageable pageable);
    Page<Option> findByStoreIdAndNameContaining(Long storeId, String keyword, Pageable pageable);
    @Query("SELECT o FROM Option o " +
            "LEFT JOIN FETCH o.optionValueList ov " +
            "LEFT JOIN FETCH ov.storeInventory " +
            "WHERE o.id = :optionId")
    Optional<Option> findByIdWithOptionValues(@Param("optionId") Long optionId);

    Optional<Option> findByStoreIdAndName(Long storeId, String name);
}
