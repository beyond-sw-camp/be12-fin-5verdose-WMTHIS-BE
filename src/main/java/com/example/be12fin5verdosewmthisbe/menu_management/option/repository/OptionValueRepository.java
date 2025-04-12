package com.example.be12fin5verdosewmthisbe.menu_management.option.repository;

import com.example.be12fin5verdosewmthisbe.menu_management.option.model.OptionValue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OptionValueRepository extends JpaRepository<OptionValue, Long> {
    List<OptionValue> findByOptionId(Long optionId);

    void deleteByOptionIdAndInventoryIdIn(Long optionId, List<Long> inventoryIdsToDelete);
}
