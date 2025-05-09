package com.example.be12fin5verdosewmthisbe.common;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class UnitConvertService {

    /**
     * 입력 단위를 기준 단위로 변환 (예: kg → g, L → ml)
     */
    public BigDecimal convertToBase(BigDecimal quantity, String unit) {
        switch (unit) {
            case "kg":
            case "L":
                return quantity.multiply(BigDecimal.valueOf(1000));
            case "g":
            case "ml":
            case "개":
                return quantity;
            default:
                throw new IllegalArgumentException("지원하지 않는 단위입니다: " + unit);
        }
    }

    /**
     * 단위 간 변환 수행 (동일한 단위 그룹 내에서만)
     */
    public BigDecimal convert(BigDecimal quantity, String fromUnit, String toUnit) {
        UnitGroup fromGroup = UnitGroup.fromUnit(fromUnit);
        UnitGroup toGroup = UnitGroup.fromUnit(toUnit);

        if (!fromGroup.equals(toGroup)) {
            throw new IllegalArgumentException("서로 다른 단위 그룹 간의 변환은 허용되지 않습니다. (" + fromUnit + " → " + toUnit + ")");
        }

        // 기준 단위로 변환 후
        BigDecimal base = convertToBase(quantity, fromUnit);

        // 대상 단위로 변환
        switch (toUnit) {
            case "kg":
            case "L":
                return base.divide(BigDecimal.valueOf(1000), 4, RoundingMode.HALF_UP);
            case "g":
            case "ml":
            case "개":
                return base;
            default:
                throw new IllegalArgumentException("지원하지 않는 단위입니다: " + toUnit);
        }
    }
    public boolean canConvert(String fromUnit, String toUnit) {
        try {
            UnitGroup fromGroup = UnitGroup.fromUnit(fromUnit);
            UnitGroup toGroup = UnitGroup.fromUnit(toUnit);
            return fromGroup.equals(toGroup);
        } catch (IllegalArgumentException e) {
            return false; // 지원하지 않는 단위일 경우 변환 불가
        }
    }
}