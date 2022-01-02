package edu.policy.model.constraint;

import java.util.HashMap;
import java.util.Map;

public enum ConstraintType {

    FD(1),

    CFD(2),

    DC(3),

    INVC(4),

    MD(5),

    PBD(6);

    private final int id;

    private static final Map<Integer, ConstraintType> lookup = new HashMap<>();

    static {
        for (ConstraintType d : ConstraintType.values()) {
            lookup.put(d.getID(), d);
        }
    }

    private ConstraintType(int id) {
        this.id = id;
    }

    public int getID() {
        return id;
    }
}
