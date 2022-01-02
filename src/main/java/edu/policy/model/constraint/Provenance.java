package edu.policy.model.constraint;

import edu.policy.model.AttributeType;

import java.util.ArrayList;
import java.util.List;

/**
 * The Provenance class:
 * Storing the provenance information for provenance-based dependencies (= function-based dependencies).
 *
 * This class includes two attributes:
 * OutputAttr: string, representing the name of the derived attribute
 * InputAttrs: list of strings, representing the names of the provenance attributes
 *
 * Example:
 * A provenance based dependency could be in the following form
 * OutputAttr = fn(InputAttr_1, ...)
 * Tax = fn(Salary, TaxRate)
 */

public class Provenance {

    String OutputAttr;

    List<String> inputAttrs = new ArrayList<>();

    AttributeType outputAttrType;

    List<AttributeType> inputAttrTypes = new ArrayList<>();

    ConstraintType dependencyType;

    String relationName;

    List<String> relationNames = new ArrayList<>(); // for cross relation dependencies, e.g PBD

    public Provenance() {

    }

    public Provenance(String outputAttr, List<String> inputAttrs) {
        OutputAttr = outputAttr;
        inputAttrs = inputAttrs;
    }

    public String getOutputAttr() {
        return OutputAttr;
    }

    public void setOutputAttr(String outputAttr) {
        OutputAttr = outputAttr;
    }

    public void addInputAttr(String inputAttr) {
        inputAttrs.add(inputAttr);
    }

    public List<String> getInputAttrs() {
        return inputAttrs;
    }

    public void setInputAttrs(List<String> inputAttrs) {
        inputAttrs = inputAttrs;
    }

    public void setDependencyType(ConstraintType dependencyType) {
        this.dependencyType = dependencyType;
    }

    public void setRelationName(String relationName) {
        this.relationName = relationName;
    }

    public void setRelationNames(List<String> relationNames) {
        this.relationNames = relationNames;
    }

    public AttributeType getOutputAttrType() {
        return outputAttrType;
    }

    public void setOutputAttrType(AttributeType outputAttrType) {
        this.outputAttrType = outputAttrType;
    }

    public List<AttributeType> getInputAttrTypes() {
        return inputAttrTypes;
    }

    public void setInputAttrTypes(List<AttributeType> inputAttrTypes) {
        this.inputAttrTypes = inputAttrTypes;
    }

    public void addInputAttrType(AttributeType inputAttrType) {
        this.inputAttrTypes.add(inputAttrType);
    }

    @Override
    public String toString() {
        return "Provenance{" +
                "OutputAttr='" + OutputAttr + '\'' +
                ", InputAttrs=" + inputAttrs +
                '}';
    }
}
