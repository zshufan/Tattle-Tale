package edu.policy.model.policy;

import edu.policy.model.AttributeType;

import java.util.HashMap;
import java.util.Map;

public class ProjectionAttribute {

    private Policy policy;

    private Map<String, AttributeType> projections;

    public ProjectionAttribute(String attrName, AttributeType attrType){
        projections = new HashMap<>();
        projections.put(attrName, attrType);

    }
}
