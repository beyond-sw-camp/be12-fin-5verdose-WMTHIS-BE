package com.example.be12fin5verdosewmthisbe.redis.dto;

import com.example.be12fin5verdosewmthisbe.menu_management.option.model.Option;
import com.example.be12fin5verdosewmthisbe.menu_management.option.model.OptionValue;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RedisOptionDto {
    private Long optionId;
    private String name;
    private int price;
    private List<RedisOptionValueDto> optionValues;  // 재고와 수량 정보 포함

    public static RedisOptionDto fromOption(Option option) {
        List<RedisOptionValueDto> valueDtos = option.getOptionValueList().stream()
                .map(RedisOptionValueDto::fromOptionValue)
                .collect(Collectors.toList());

        return new RedisOptionDto(
                option.getId(),
                option.getName(),
                option.getPrice(),
                valueDtos
        );
    }


}

