package edu.policy.model.constraint;

import edu.policy.model.AttributeType;
import edu.policy.model.OperationType;

import java.util.ArrayList;
import java.util.List;

public class Predicate {

    private int predID; // position in a dependency

    private String attributeName;

    private Boolean truthValue;

    private Boolean hasConstant;

    private Cell cell_1; // sensitive cell (if in senPred) or nonsensitive cell

    private Cell cell_2; // nonsensitive cell

    private int senCellRef = 0;

    private OperationType operation;

    private int constant;

    private double constantDouble;

    private String constantString;

    private AttributeType constantType;

    private boolean isSensitive = Boolean.FALSE;

    private boolean isReverse = Boolean.FALSE;


    public Predicate(int predRef, Cell cell_1, Cell cell_2, OperationType operation,
                     Boolean schemaFlag, String attributeName) {
        // schema-level predicate
        this.predID = predRef;
        this.cell_1 = cell_1;
        this.cell_2 = cell_2;
        this.operation = operation;
        this.hasConstant = Boolean.FALSE;
        this.attributeName = attributeName;

    }

    public Predicate(int predRef, Cell cell_1, int constant, OperationType operation, Boolean schemaFlag, String attributeName) {
        // schema-level predicate
        this.predID = predRef;
        this.cell_1 = cell_1;
        this.constant = constant;
        this.operation = operation;
        this.hasConstant = Boolean.TRUE;
        this.attributeName = attributeName;

        this.constantType = AttributeType.INTEGER;
    }

    public Predicate(int predRef, Cell cell_1, double constant, OperationType operation, Boolean schemaFlag, String attributeName) {
        // schema-level predicate
        this.predID = predRef;
        this.cell_1 = cell_1;
        this.constantDouble = constant;
        this.operation = operation;
        this.hasConstant = Boolean.TRUE;
        this.attributeName = attributeName;

        this.constantType = AttributeType.INTEGER;
    }

    public Predicate(int predRef, Cell cell_1, String constant, OperationType operation, Boolean schemaFlag, String attributeName) {
        // schema-level predicate
        this.predID = predRef;
        this.cell_1 = cell_1;
        this.constantString = constant;
        this.operation = operation;
        this.hasConstant = Boolean.TRUE;
        this.attributeName = attributeName;

        this.constantType = AttributeType.STRING;
    }

    public Predicate(Cell cell_1, Cell cell_2, OperationType operation, Boolean schemaFlag, String attributeName) {
        // schema-level predicate
        this(-1, cell_1, cell_2, operation, schemaFlag, attributeName);
    }

    public Predicate(Cell cell_1, int constant, OperationType operation, Boolean schemaFlag, String attributeName) {
        // schema-level predicate
        this(-1, cell_1, constant, operation, schemaFlag, attributeName);
    }

    public Predicate(Cell cell_1, double constant, OperationType operation, Boolean schemaFlag, String attributeName) {
        // schema-level predicate
        this(-1, cell_1, constant, operation, schemaFlag, attributeName);
    }

    public Predicate(Cell cell_1, String constant, OperationType operation, Boolean schemaFlag, String attributeName) {
        // schema-level predicate
        this(-1, cell_1, constant, operation, schemaFlag, attributeName);
    }

    public Predicate(Predicate schemaPredicate) {
        this.predID = schemaPredicate.predID;
        this.operation = schemaPredicate.operation;
        this.constantString = schemaPredicate.constantString;
        this.constant = schemaPredicate.constant;
        this.constantDouble = schemaPredicate.constantDouble;
        this.attributeName = schemaPredicate.attributeName;
        this.cell_1 = new Cell(schemaPredicate.getComponent_1());
        this.cell_2 = new Cell(schemaPredicate.getComponent_2());
    }

    public boolean setReverse() {
        assert cell_1.getCellType()== cell_2.getCellType();
        switch (cell_1.getCellType()) {
            case INTEGER:
                switch (operation) {
                    case GT:
                        if (cell_1.getIntCellValue() < cell_2.getIntCellValue())
                            isReverse = Boolean.TRUE;
                        break;
                    case GTE:
                        if (cell_1.getIntCellValue() <= cell_2.getIntCellValue())
                            isReverse = Boolean.TRUE;
                        break;
                    case LT:
                        if (cell_1.getIntCellValue() > cell_2.getIntCellValue())
                            isReverse = Boolean.TRUE;
                        break;
                    case LTE:
                        if (cell_1.getIntCellValue() >= cell_2.getIntCellValue())
                            isReverse = Boolean.TRUE;
                        break;
                }
                break;
            case DOUBLE:
                switch (operation) {
                    case GT:
                        if (cell_1.getDoubleCellValue() < cell_2.getDoubleCellValue())
                            isReverse = Boolean.TRUE;
                        break;
                    case GTE:
                        if (cell_1.getDoubleCellValue() <= cell_2.getDoubleCellValue())
                            isReverse = Boolean.TRUE;
                        break;
                    case LT:
                        if (cell_1.getDoubleCellValue() > cell_2.getDoubleCellValue())
                            isReverse = Boolean.TRUE;
                        break;
                    case LTE:
                        if (cell_1.getDoubleCellValue() >= cell_2.getDoubleCellValue())
                            isReverse = Boolean.TRUE;
                        break;
                }
                break;
        }

        return isReverse;
    }

    public Boolean getTruthValue() {
        return truthValue;
    }

    public Cell getComponent_1() {
        return cell_1;
    }

    public Cell getComponent_2() {
        return cell_2;
    }

    public int getConstant() {
        return constant;
    }

    public int getPredID() {
        return predID;
    }

    public OperationType getOperation() {
        return operation;
    }

    public String getConstantString() {
        return constantString;
    }

    public AttributeType getConstantType() {
        return constantType;
    }

    public List<Cell> getComponents() {
        return new ArrayList<Cell>(){{add(cell_1); add(cell_2);}};
    }

    public Boolean hasConstant() {
        return hasConstant;
    }

    public void setComponent_1(Cell cell_1) {
        this.cell_1 = cell_1;
    }

    public void setComponent_2(Cell cell_2) {
        this.cell_2 = cell_2;
    }

    public void setConstant(int constant) {
        this.constant = constant;
    }

    public void setConstantString(String constantString) {
        this.constantString = constantString;
    }

    public void setHasConstant(Boolean hasConstant) {
        this.hasConstant = hasConstant;
    }

    public void setOperation(OperationType operation) {
        this.operation = operation;
    }

    public void setPredID(int predID) {
        this.predID = predID;
    }

    public void setTruthValue(Boolean truthValue) {
        this.truthValue = truthValue;
    }

    public void setSensitive(boolean sensitive) {
        isSensitive = sensitive;
    }

    public Boolean isSensitive() {
        return isSensitive;
    }

    public String getAttributeName() {
        return attributeName;
    }

    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

    public double getConstantDouble() {
        return constantDouble;
    }

    public void setConstantDouble(double constantDouble) {
        this.constantDouble = constantDouble;
    }

    public void setSenCellRef(int senCellRef) {
        this.senCellRef = senCellRef;
    }

    public Cell getSenCell() {
        if (senCellRef == 1) {
            return cell_1;
        }
        else if (senCellRef == 2) {
            return cell_2;
        }
        else
            return null;
    }

    public boolean isReverse() {
        return isReverse;
    }

    public void setReverse(boolean reverse) {
        isReverse = reverse;
    }

    public void instantiateCell_1(Cell cell) {
        switch (cell.getCellType()) {
            case STRING:
                cell_1.setStringCellValue(cell.getStringCellValue());
                break;
            case TIMESTAMP:
                break;
            case DOUBLE:
                cell_1.setDoubleCellValue(cell.getDoubleCellValue());
                break;
            case INTEGER:
                cell_1.setIntCellValue(cell.getIntCellValue());
                break;
            case DATE:
                break;
            case TIME:
                break;
        }

        cell_1.setTupleID(cell.tupleID);
    }

    @Override
    public String toString() {
        return "Predicate{" +
                "attributeName='" + attributeName + '\'' +
                ", hasConstant=" + hasConstant +
                ", cell_1=" + cell_1 +
                ", cell_2=" + cell_2 +
                ", senCellRef=" + senCellRef +
                ", operation=" + operation +
                '}';
    }
}
