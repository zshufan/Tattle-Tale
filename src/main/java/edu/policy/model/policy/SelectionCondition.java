package edu.policy.model.policy;

import edu.policy.model.BooleanCondition;

import java.util.List;

public class SelectionCondition extends BooleanCondition {
    String row;

    public SelectionCondition(String row) {
        this.row = row;
    }
}
