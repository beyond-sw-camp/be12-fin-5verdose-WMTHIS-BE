package com.example.be12fin5verdosewmthisbe.dashboard.model;

import com.example.be12fin5verdosewmthisbe.store.model.Store;
import jakarta.persistence.*;
import lombok.*;
import java.util.List;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class DashboardInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 기본 키

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private Store store;

    private InfoStatus status;

    private String content;

    public enum InfoStatus {
        MARKET,
        MENU,
        INGREDIENT
    }
}

