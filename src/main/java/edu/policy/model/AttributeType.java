package edu.policy.model;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;


public enum AttributeType {

    STRING(1),

    TIMESTAMP(2),

    DOUBLE(3),

    INTEGER(4),

    DATE(5),

    TIME(6);

    private final int id;

    private static final Map<Integer, AttributeType> lookup = new HashMap<>();

    private static final Logger logger = LogManager.getLogger(AttributeType.class);

    static {
        for (AttributeType d : AttributeType.values()) {
            lookup.put(d.getID(), d);
        }
    }

    AttributeType(int id) {
        this.id = id;
    }

    public int getID() {
        return id;
    }

    public static AttributeType contains(String stringType) {
        if (stringType.contains("INT"))
            return AttributeType.INTEGER;
        else if (stringType.contains("CHAR"))
            return AttributeType.STRING;
        else if (stringType.contains("DOUBLE") || stringType.contains("FLOAT"))
            return DOUBLE;
        else {
            logger.error(String.format("Attribute type not recognized: %s", stringType));
            return null;
        }

    }
}
