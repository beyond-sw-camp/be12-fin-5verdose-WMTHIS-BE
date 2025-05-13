package com.example.be12fin5verdosewmthisbe.menu_management.menu.repository;

import com.example.be12fin5verdosewmthisbe.inventory.model.StoreInventory;
import com.example.be12fin5verdosewmthisbe.menu_management.menu.model.Menu;
import com.example.be12fin5verdosewmthisbe.menu_management.menu.model.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RecipeRepository extends JpaRepository<Recipe, Long> {
    List<Recipe> findAllByMenu(Menu menu);

    List<Recipe> findAllByStoreInventory(StoreInventory storeInventory);


    @Query("""
      SELECT r
      FROM Recipe r
      JOIN FETCH r.menu m
      WHERE r.storeInventory = :storeInventory
    """)
    List<Recipe> findAllByStoreInventoryWithMenu(@Param("storeInventory") StoreInventory storeInventory);
}
