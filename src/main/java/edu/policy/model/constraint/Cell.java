package edu.policy.model.constraint;

import edu.policy.common.PCDCConstants;
import edu.policy.model.AttributeType;

import java.util.Objects;

public class Cell {

    String databaseName;

    String relationName;

    String attributeName;

    int tupleID;  // int primary key: eid for Employee table

    private String tupleIdentifier; // string primary key

    private int predRef;

    // value: int/string/double
    private int intCellValue;

    private String stringCellValue;

    private double doubleCellValue;

    private AttributeType cellType;

    private final String defaultOpenHideFlag = PCDCConstants.DEFAULT_OPEN_HIDE_FLAG;

    private String curOpenHideFlag = defaultOpenHideFlag;

    private State curState;

    // As for int/float/double data type, we store the min(Dom) and max(Dom)
    private double minDomain;
    private double maxDomain;

    private double attrDomSize = 0;

    public Cell(String databaseName, String relationName, String attributeName, int predRef, AttributeType attributeType) {
        // schema-level component
        this.databaseName = databaseName;
        this.predRef = predRef;
        this.relationName = relationName;
        this.attributeName = attributeName;
        this.cellType = attributeType;
        this.curState = new State(attributeType, minDomain, maxDomain);
    }

    public Cell(String databaseName, String relationName, String attributeName, int tupleID) {
        this.databaseName = databaseName;
        this.relationName = relationName;
        this.attributeName = attributeName;
        this.tupleID = tupleID;
    }

    public Cell(Cell schemaCell) {
        // instantiation
        this.databaseName = schemaCell.databaseName;
        this.relationName = schemaCell.relationName;
        this.attributeName = schemaCell.attributeName;
        this.tupleID = schemaCell.tupleID;
        this.predRef = schemaCell.predRef;
        this.cellType = schemaCell.cellType;
        this.curState = schemaCell.curState;
        this.attrDomSize = schemaCell.attrDomSize;
    }

    public String getRelationName() {
        return relationName;
    }

    public String getAttributeName() {
        return attributeName;
    }

    public int getTupleID() {
        return tupleID;
    }

    public int getIntCellValue() {
        return intCellValue;
    }

    public String getStringCellValue() {
        return stringCellValue;
    }

    public void setIntCellValue(int intCellValue) {
        this.intCellValue = intCellValue;
    }

    public void setStringCellValue(String stringCellValue) {
        this.stringCellValue = stringCellValue;
    }

    public void setTupleID(int tupleID) {
        this.tupleID = tupleID;
    }

    public void setCellType(AttributeType cellType) {
        this.cellType = cellType;
    }

    public AttributeType getCellType() {
        return cellType;
    }

    public void setDoubleCellValue(double doubleCellValue) {
        this.doubleCellValue = doubleCellValue;
    }

    public double getDoubleCellValue() {
        return doubleCellValue;
    }

    public State getCurState() {
        return curState;
    }

    public void setCurState(State curState) {
        this.curState = curState;
    }

    public double getMinDomain() {
        return minDomain;
    }

    public double getMaxDomain() {
        return maxDomain;
    }

    public double getAttrDomSize() {
        return attrDomSize;
    }

    public void setAttrDomSize(int attrDomSize) {
        this.attrDomSize = attrDomSize;
    }

    public void setMinMaxDom(double minDom, double maxDom) {
        this.minDomain = minDom;
        this.maxDomain = maxDom;
        this.attrDomSize = maxDom - minDom;
    }

    public double getCurrentStateSize () {
        if (cellType.equals(AttributeType.STRING))
            return attrDomSize - curState.getMinusSetString().size();
        else if (cellType.equals(AttributeType.DOUBLE) || cellType.equals(AttributeType.INTEGER))
            return curState.getHigh() - curState.getLow();
        else
            throw new IllegalStateException("Cell (getCurrentStateSize): Unsupported attribute type " + cellType);
    }

    public String getDatabaseName() {
        return databaseName;
    }

    @Override
    // To print the hidden cells
    // For example, tax{78, 'state'}
    public String toString() {
        if (cellType != null) {
            if (cellType.equals(AttributeType.STRING))
                if (stringCellValue != null)
                    return relationName + "{" +
                            tupleID + ", "
                            + '\'' + attributeName + '\''
                            + ", val: \'" + stringCellValue.replaceAll(";", ",") + '\''
                            + "}";
                else
                    return relationName + "{" +
                            tupleID + ", "
                            + '\'' + attributeName + '\''
                            + ", val: \'" + null + '\''
                            + "}";
            else if (cellType.equals(AttributeType.INTEGER))
                return relationName + "{" +
                        tupleID + ", "
                        + '\'' + attributeName + '\''
                        + ", val: \'" + intCellValue + '\''
                        + "}";
            else if (cellType.equals(AttributeType.DOUBLE))
                return relationName + "{" +
                        tupleID + ", "
                        + '\'' + attributeName + '\''
                        + ", val: \'" + doubleCellValue + '\''
                        + "}";
        }

        return relationName + "{" +
                tupleID + ", "
                + '\'' + attributeName + '\''
                + "}";
    }

    // single table for proof of concept
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Cell)) return false;
        Cell cell = (Cell) o;
        return getTupleID() == cell.getTupleID() && getAttributeName().equals(cell.getAttributeName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getAttributeName(), getTupleID());
    }
}
