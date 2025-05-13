package com.example.orderservice.menu_management.option.repository;

import com.example.orderservice.menu_management.option.model.Option;
import com.example.orderservice.menu_management.option.model.OptionValue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OptionValueRepository extends JpaRepository<OptionValue, Long> {
    List<OptionValue> findAllByOption(Option option);
}
