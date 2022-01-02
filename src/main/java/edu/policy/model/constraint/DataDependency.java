package edu.policy.model.constraint;


/**
 * Schema level data dependency
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DataDependency {

    ConstraintType dependencyType;

    String relationName;

    String cnf_form;

    List<Predicate> schemaPredicates = new ArrayList<>();

    List<Cell> schemaCells = new ArrayList<>();

    List<String> attributeNames = new ArrayList<>();


    public DataDependency() {

    }

    public void setCnf_form(String cnf_form) {
        this.cnf_form = cnf_form;
    }

    public void setAttributeNames(String... attributeNames) {
        Collections.addAll(this.attributeNames, attributeNames);
    }

    public void setDependencyType(ConstraintType dependencyType) {
        this.dependencyType = dependencyType;
    }

    public void setRelationName(String relationName) {
        this.relationName = relationName;
    }

    public void setSchemaComponents(Cell... schemaCells) {
        Collections.addAll(this.schemaCells, schemaCells);
    }

    public void addSchemaPredicate(Predicate schemaPredicate) {
        this.schemaPredicates.add(schemaPredicate);
    }

    public String getRelationName() {
        return relationName;
    }

    public String getCnf_form() {
        return cnf_form;
    }

    public ConstraintType getDependencyType() {
        return dependencyType;
    }

    public List<Cell> getSchemaComponents() {
        return schemaCells;
    }

    public List<Predicate> getSchemaPredicates() {
        return schemaPredicates;
    }

    public List<String> getAttributeNames() {
        return attributeNames;
    }


    @Override
    public String toString() {
        return "DataDependency{" +
                "dependencyType=" + dependencyType +
                ", relationName='" + relationName + '\'' +
                ", attributeNames=" + attributeNames +
                '}';
    }
}
