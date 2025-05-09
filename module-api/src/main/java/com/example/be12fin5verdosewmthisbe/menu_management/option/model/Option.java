package com.example.be12fin5verdosewmthisbe.menu_management.option.model;

import com.example.be12fin5verdosewmthisbe.menu_management.category.model.CategoryOption;
import com.example.be12fin5verdosewmthisbe.order.model.OrderOption;
import com.example.be12fin5verdosewmthisbe.store.model.Store;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Schema(description = "메뉴 옵션 정보")
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Option {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Schema(description = "옵션 ID (자동 생성)", example = "1")
    private Long id;

    @Schema(description = "옵션 이름", example = "사이즈업")
    private String name;

    @Schema(description = "옵션 가격", example = "500")
    private int price;

    @ManyToOne
    @JoinColumn(name = "store_id")
    private Store store;

    @OneToMany(mappedBy = "option", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CategoryOption> categoryOptions = new ArrayList<>();

    @OneToMany(mappedBy = "option", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderOption> orderOptionList = new ArrayList<>();


    @OneToMany(mappedBy = "option", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OptionValue> optionValueList = new ArrayList<>();
}