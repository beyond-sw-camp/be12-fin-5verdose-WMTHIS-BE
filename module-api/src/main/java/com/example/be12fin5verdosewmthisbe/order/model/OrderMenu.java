package com.example.be12fin5verdosewmthisbe.order.model;


import com.example.be12fin5verdosewmthisbe.menu_management.menu.model.Menu;
import com.fasterxml.jackson.annotation.JsonBackReference;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.mail.event.MailEvent;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;


@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderMenu {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    @JsonBackReference  // 무한 참조 방지
    @Schema(description = "메뉴 목록들이 속한 주문")
    @ToString.Exclude
    private Order order;

    @Builder.Default
    @OneToMany(mappedBy = "orderMenu", cascade = CascadeType.ALL, orphanRemoval = true)
    @Schema(description = "메뉴에서 선택한 옵션 목록")
    private List<OrderOption> orderOptionList = new ArrayList<>();

    // 주문한 메뉴 수량
    private Integer quantity;
    
    // 메뉴의 단가
    private Integer price;

    @ManyToOne
    @JoinColumn(name = "menu_id")
    private Menu menu;
}