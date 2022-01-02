package edu.policy.model;

import java.util.HashMap;
import java.util.Map;

public enum PurposeType {

    ANALYTICS(1),

    ADVERTISEMENT(2),

    EMERGENCY(3);

    private final int id;

    private static final Map<Integer, PurposeType> lookup = new HashMap<>();

    static {
        for (PurposeType d : PurposeType.values()) {
            lookup.put(d.getID(), d);
        }
    }

    PurposeType(int id) {
        this.id = id;
    }

    public int getID() {
        return id;
    }

}
