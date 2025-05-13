package com.example.be12fin5verdosewmthisbe.order.model;

import com.example.be12fin5verdosewmthisbe.store.model.Store;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "sales_comparison")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalesComparison {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Store store;

    private LocalDate targetDate; // 어제 날짜

    private Integer yesterdaySales;

    private Integer weekAgoSales;

    private Integer difference;

    private Double percentageChange; // ((어제 - 1주전) / 1주전) * 100
}
