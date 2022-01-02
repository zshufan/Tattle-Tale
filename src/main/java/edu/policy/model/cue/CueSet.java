package edu.policy.model.cue;

import edu.policy.model.constraint.Cell;
import edu.policy.model.constraint.ConstraintType;
import edu.policy.model.constraint.DependencyInstance;
import edu.policy.model.constraint.Provenance;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CueSet {

    private boolean isEmpty = Boolean.FALSE;

    private double leakageToParent;

    private boolean isChosenToBeHidden = Boolean.FALSE;

    private DependencyInstance dependencyInstance;

    private Provenance pbdInstance;

    private ConstraintType constraintType;

    private Cell senCell;

    ArrayList<Cell> cells = new ArrayList<>();

    private String minusString;

    public CueSet(List<Cell> cells, Cell senCell, ConstraintType constraintType) {
        this.cells.addAll(cells);
        this.senCell = senCell;
        this.constraintType = constraintType;
    }

    public CueSet(boolean isEmpty) {
        this.isEmpty = Boolean.TRUE;
    }

    public ArrayList<Cell> getCells() {
        return cells;
    }

    public void addCells(List<Cell> cells) {
        this.cells.addAll(cells);
    }

    public void addCell(Cell cell) {
        this.cells.add(cell);
    }

    public boolean isEmpty() {
        return isEmpty;
    }

    public DependencyInstance getDependencyInstance() {
        return dependencyInstance;
    }

    public void setDependencyInstance(DependencyInstance dependencyInstance) {
        this.dependencyInstance = dependencyInstance;
    }

    public double getLeakageToParent() {
        return leakageToParent;
    }

    public void setLeakageToParent(double leakageToParent) {
        this.leakageToParent = leakageToParent;
    }

    public boolean isChosenToBeHidden() {
        return isChosenToBeHidden;
    }

    public void setChosenToBeHidden(boolean chosenToBeHidden) {
        isChosenToBeHidden = chosenToBeHidden;
    }

    public void deleteCell(Cell cellForRemove) {
        cells.removeIf(c -> (cellForRemove.getRelationName().equals(c.getRelationName())) && (cellForRemove.getTupleID() == c.getTupleID())
                && (cellForRemove.getAttributeName().equals(c.getAttributeName())));
    }

    public void deleteCells(List<Cell> cellsForRemove) {
        cells.removeIf(cellsForRemove::contains);
    }

    public ConstraintType getConstraintType() {
        return constraintType;
    }

    public void setConstraintType(ConstraintType constraintType) {
        this.constraintType = constraintType;
    }

    public Cell getSenCell() {
        return senCell;
    }

    public String getMinusString() {
        return minusString;
    }

    public void setMinusString(String minusString) {
        this.minusString = minusString;
    }

    public Provenance getPbdInstance() {
        return pbdInstance;
    }

    public void setPbdInstance(Provenance pbdInstance) {
        this.pbdInstance = pbdInstance;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CueSet)) return false;
        CueSet cueSet = (CueSet) o;
        return Objects.equals(getSenCell(), cueSet.getSenCell()) && Objects.equals(getCells(), cueSet.getCells());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSenCell(), getCells());
    }
}
