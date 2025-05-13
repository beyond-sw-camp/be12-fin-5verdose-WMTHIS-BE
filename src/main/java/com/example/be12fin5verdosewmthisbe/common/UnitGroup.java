package com.example.be12fin5verdosewmthisbe.common;

import lombok.Getter;

import java.util.Arrays;
import java.util.Set;

@Getter
public enum UnitGroup {
    WEIGHT(Set.of("kg", "g"), "g"),
    VOLUME(Set.of("L", "ml"), "ml"),
    COUNT(Set.of("개"), "개");

    private final Set<String> units;
    private final String baseUnit;

    UnitGroup(Set<String> units, String baseUnit) {
        this.units = units;
        this.baseUnit = baseUnit;
    }

    public static UnitGroup fromUnit(String unit) {
        return Arrays.stream(values())
                .filter(group -> group.units.contains(unit))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("지원하지 않는 단위입니다: " + unit));
    }

    public boolean contains(String unit) {
        return units.contains(unit);
    }
}