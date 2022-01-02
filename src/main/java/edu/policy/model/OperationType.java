package edu.policy.model;

import java.util.Arrays;

public enum  OperationType {

    NEQ("NEQ"), // NEQ before EQ, otherwise the DCParser will first detect EQ from NEQ

    EQ("EQ"),

    GTE("GTE"),

    LTE("LTE"),

    GT("GT"),

    LT("LT"),

    IN("IN"),

    OUT("OUT");

    private final String value;

    OperationType(String value) {
        this.value = value;
    }

    public static String[] getNames(Class<? extends Enum<?>> e) {
        return Arrays.stream(e.getEnumConstants()).map(Enum::name).toArray(String[]::new);
    }

    public String toString() {
        return this.value;
    }
}
