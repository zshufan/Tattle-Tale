package edu.policy.model;

import edu.policy.model.policy.BooleanPredicate;
import edu.policy.model.policy.Policy;

import java.util.ArrayList;
import java.util.List;

public class BooleanCondition {

    private Policy policy;

    private String attribute;

    private AttributeType attributeType;

    private List<BooleanPredicate> booleanPredicates;

    public BooleanCondition() {
        this.booleanPredicates = new ArrayList<>();
    }

    @Override
    public String toString() {
        return "BooleanConditions{" +
                "policy=" + policy +
                ", attribute='" + attribute + '\'' +
                ", attributeType=" + attributeType +
                ", booleanPredicates=" + booleanPredicates +
                '}';
    }
}
