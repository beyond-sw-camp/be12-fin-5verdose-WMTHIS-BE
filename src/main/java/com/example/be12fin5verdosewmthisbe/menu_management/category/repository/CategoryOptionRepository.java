package com.example.be12fin5verdosewmthisbe.menu_management.category.repository;

import com.example.be12fin5verdosewmthisbe.menu_management.category.model.CategoryOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryOptionRepository extends JpaRepository<CategoryOption, Long> {

    List<CategoryOption> findAllByCategoryId(Long id);
}
