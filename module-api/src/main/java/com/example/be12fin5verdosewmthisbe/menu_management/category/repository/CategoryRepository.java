package com.example.be12fin5verdosewmthisbe.menu_management.category.repository;

import com.example.be12fin5verdosewmthisbe.menu_management.category.model.Category;
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
public interface CategoryRepository extends JpaRepository<Category, Long> {
    // Your code here
    Optional<Category> findByStoreIdAndName(Long storeId, String name);
    @Query("SELECT c FROM Category c LEFT JOIN FETCH c.categoryOptions co LEFT JOIN FETCH co.option WHERE c.id = :id")
    Optional<Category> findByIdWithOptions(@Param("id") Long id);
    @EntityGraph(attributePaths = {"categoryOptions", "categoryOptions.option"})
    Page<Category> findByStoreId(Long storeId, Pageable pageable);
    List<Category> findByStoreId(Long storeId);

    Page<Category> findByStoreIdAndNameContaining(Long storeId, String name, Pageable pageable);
}
        