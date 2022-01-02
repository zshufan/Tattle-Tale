package edu.policy.model.constraint;

import java.util.ArrayList;
import java.util.List;

public class DependencyInstance {

    private String relationName;

    ConstraintType type;

    String cnf_form;

    List<Predicate> predicates;

    int senPredID; // The position of the sensitive predicate in List::Predicates

    public DependencyInstance(DataDependency schemaDep) {
        this.relationName = schemaDep.getRelationName();
        this.type = schemaDep.getDependencyType();
        this.cnf_form = schemaDep.getCnf_form();
        this.predicates = clonePredicateList(schemaDep.getSchemaPredicates());
    }

    public String getRelationName() {
        return relationName;
    }

    public ConstraintType getType() {
        return type;
    }

    public List<Predicate> getPredicates() {
        return predicates;
    }

    public void setType(ConstraintType type) {
        this.type = type;
    }

    public void setSenPredID(int senPredID) {
        this.senPredID = senPredID;
    }

    public int getSenPredID() {
        return senPredID;
    }

    public static List<Predicate> clonePredicateList(List<Predicate> predicateList) {
        List<Predicate> clonedList = new ArrayList<Predicate>(predicateList.size());
        for (Predicate predicate : predicateList) {
            clonedList.add(new Predicate(predicate));
        }
        return clonedList;
    }

    public static List<Cell> cloneCellList(List<Cell> cellList) {
        List<Cell> clonedList = new ArrayList<Cell>(cellList.size());
        for (Cell cell : cellList) {
            clonedList.add(new Cell(cell));
        }
        return clonedList;
    }
}
