package edu.policy.model.constraint;

import edu.policy.model.AttributeType;

import java.util.HashSet;
import java.util.Set;

public class State {

    AttributeType attributeType;

    double low;

    double high;

    double stringDomSize;

    Set<String> minusSetString = new HashSet<>();

    boolean isNoLeakage = Boolean.TRUE;

    boolean isFullLeakage = Boolean.FALSE;


    public State(AttributeType type, Cell targetCell) {

        this.attributeType = type;

        if (type.equals(AttributeType.STRING))
            this.stringDomSize = targetCell.getAttrDomSize();
        else if (type.equals(AttributeType.INTEGER) || type.equals(AttributeType.DOUBLE)) {
            this.low = targetCell.getMinDomain();
            this.high = targetCell.getMaxDomain();
        }

    }

    public State(AttributeType type, double low, double high) {

        this.attributeType = type;

        this.low = low;
        this.high = high;

    }

    public void checkFullLeakage () {
        switch (attributeType) {
            case STRING:
                if (minusSetString.size() == stringDomSize)
                    this.isFullLeakage = Boolean.TRUE;
                break;

            case DOUBLE:
            case INTEGER:
                if (low > high)
                    this.isFullLeakage = Boolean.TRUE;
                if (low == high) // the cell can only take 1 value
                    this.isFullLeakage = Boolean.TRUE;
                break;

            case TIMESTAMP:
            case DATE:
            case TIME:
                throw new IllegalStateException("Not implemented attribute types.");
        }
    }


    public void addToMinusSetString(String value) {
        this.minusSetString.add(value);
    }

    public double getLow() {
        return low;
    }

    public void setLow(double low) {
        this.low = low;
    }

    public double getHigh() {
        return high;
    }

    public void setHigh(double high) {
        this.high = high;
    }

    public Set<String> getMinusSetString() {
        return minusSetString;
    }

    public void setMinusSetString(Set<String> minusSetString) {
        this.minusSetString = minusSetString;
    }

    public boolean isNoLeakage() {
        return isNoLeakage;
    }

    public void setNoLeakage(boolean noLeakage) {
        isNoLeakage = noLeakage;
    }

    public boolean isFullLeakage() {
        return isFullLeakage;
    }

    public void setFullLeakage(boolean fullLeakage) {
        isFullLeakage = fullLeakage;
    }

}
