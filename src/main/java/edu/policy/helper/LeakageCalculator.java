package edu.policy.helper;


import edu.policy.model.AttributeType;
import edu.policy.model.OperationType;
import edu.policy.model.constraint.*;
import edu.policy.model.cue.CueSet;
import edu.policy.model.data.Session;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

/**
 * Leakage Calculator Class (Helper/Util):
 *  Input:
 *  	c* - target cell for calculating leakage
 *  	C -  a set of instantiated cells
 *  	D - a set of instantiated dependencies
 *  Output:
 *      Updates the state of c*
 */
public class LeakageCalculator {

    private static final Logger logger = LogManager.getLogger(LeakageCalculator.class);

    /**
     * Compute the state of the input targetCell if the input list of cuesets are all open.
     *
     * This function plays two roles:
     *      1. calculate the state of the target cell and return;
     *      2. calculate the leakage of each cueset in the list to the target cell, and associate the leakage
     *         to the cueset (for greedy heuristic in k-den).
     *
     * The leakage calculation is due to the sensitive predicate.
     *
     * According to Tattle-tale condition, the cueset will leak information
     * iff the sensitive predicate is evaluated as FALSE.
     * If leakage happen, we calculate the leakage and update the state w.r.t. the non-sensitive cell
     * in the sensitive predicate.
     *
     * @param targetCell the cell whose state is calculated by this function
     * @param cueSets the list of cuesets that are open
     * @param session the current session
     * @return the state of the target cell due to the list of cuesets
     */
    public static State joint_state(Cell targetCell, List<CueSet> cueSets, Session session) {

        // create a new state of the target cell
        State stateOfTargetCell = new State(targetCell.getCellType(), targetCell);

        for (CueSet cueSet: cueSets) {
            State tempState = new State(targetCell.getCellType(), targetCell);

            if (cueSet.getConstraintType().equals(ConstraintType.DC)) {
                DependencyInstance instance = cueSet.getDependencyInstance();
                Predicate senPred = instance.getPredicates().get(instance.getSenPredID());
                Cell nonSenCell = senPred.getComponent_2();

                OperationType operation = senPred.getOperation();

                // Important! Dealing with symmetric DCs.
                if (senPred.isReverse()) {
                    switch (operation) {
                        case LT:
                            operation = OperationType.GT;
                            break;
                        case GT:
                            operation = OperationType.LT;
                            break;
                        case LTE:
                            operation = OperationType.GTE;
                            break;
                        case GTE:
                            operation = OperationType.LTE;
                            break;
                    }
                }

                switch (targetCell.getCellType()) {
                    case STRING:

                        String stringCellValue = nonSenCell.getStringCellValue();
                        if (operation == OperationType.NEQ) {

                            stateOfTargetCell.setNoLeakage(FALSE);
                            stateOfTargetCell.setFullLeakage(TRUE);
                            tempState.setNoLeakage(FALSE);
                            tempState.setFullLeakage(TRUE);

                        }
                        else if (operation == OperationType.EQ) {

                            stateOfTargetCell.setNoLeakage(FALSE);
                            stateOfTargetCell.addToMinusSetString(stringCellValue);
                            tempState.setNoLeakage(FALSE);
                            tempState.addToMinusSetString(stringCellValue);
                            cueSet.setMinusString(stringCellValue);

                        }
                        else
                            throw new java.lang.Error("LeakageCalculator (joint_state): " +
                                    "STRING attribute type cannot have range operators!");

                        break;

                    case DOUBLE:
                        double doubleCellValue = nonSenCell.getDoubleCellValue();
                        switch (operation) {
                            case NEQ:

                                stateOfTargetCell.setLow(doubleCellValue);
                                stateOfTargetCell.setHigh(doubleCellValue);

                                stateOfTargetCell.setNoLeakage(FALSE);
                                stateOfTargetCell.setFullLeakage(TRUE);
                                tempState.setNoLeakage(FALSE);
                                tempState.setFullLeakage(TRUE);

                                break;

                            case GTE:
                            case GT:
                                if (doubleCellValue < stateOfTargetCell.getHigh()) {
                                    stateOfTargetCell.setHigh(Math.max(doubleCellValue, stateOfTargetCell.getLow()));
                                    tempState.setHigh(doubleCellValue);
                                }

                                stateOfTargetCell.setNoLeakage(FALSE);
                                tempState.setNoLeakage(FALSE);
                                break;

                            case LTE:
                            case LT:
                                if (doubleCellValue > stateOfTargetCell.getLow()) {
                                    stateOfTargetCell.setLow(Math.min(doubleCellValue, stateOfTargetCell.getHigh()));
                                    tempState.setLow(doubleCellValue);
                                }

                                stateOfTargetCell.setNoLeakage(FALSE);
                                tempState.setNoLeakage(FALSE);
                                break;

                            case IN:
                            case OUT:
                                throw new IllegalStateException("Not implemented operation type.");
                        }

                        break;
                    case INTEGER:
                        int intCellValue = nonSenCell.getIntCellValue();
                        switch (operation) {
                            case NEQ:

                                stateOfTargetCell.setLow(intCellValue);
                                stateOfTargetCell.setHigh(intCellValue);

                                stateOfTargetCell.setNoLeakage(FALSE);
                                stateOfTargetCell.setFullLeakage(TRUE);
                                tempState.setNoLeakage(FALSE);
                                tempState.setFullLeakage(TRUE);

                                break;
                            case GTE:
                            case GT:
                                if (intCellValue < stateOfTargetCell.getHigh()) {
                                    stateOfTargetCell.setHigh(Math.max(intCellValue, stateOfTargetCell.getLow()));
                                    tempState.setHigh(intCellValue);
                                }

                                stateOfTargetCell.setNoLeakage(FALSE);
                                tempState.setNoLeakage(FALSE);
                                break;

                            case LTE:
                            case LT:
                                if (intCellValue > stateOfTargetCell.getLow()) {
                                    stateOfTargetCell.setLow(Math.min(intCellValue, stateOfTargetCell.getHigh()));
                                    tempState.setLow(intCellValue);
                                }

                                stateOfTargetCell.setNoLeakage(FALSE);
                                tempState.setNoLeakage(FALSE);
                                break;

                            case IN:
                            case OUT:
                                throw new IllegalStateException("Not implemented operation type.");

                        }
                        break;

                    case TIMESTAMP:
                    case DATE:
                    case TIME:
                        throw new java.lang.Error("LeakageCalculator (joint_state): Unsupported attribute type.");
                }

                stateOfTargetCell.checkFullLeakage();
                tempState.checkFullLeakage();
                cueSet.setLeakageToParent(computeLeakageToParent(targetCell, tempState));
            }
            else if (cueSet.getConstraintType().equals(ConstraintType.PBD)) {
                Provenance pbdInstance = cueSet.getPbdInstance();

                String inputAttrNotInCueset = pbdInstance.getInputAttrs().stream()
                        .filter(s -> !s.equals(cueSet.getCells().get(0).getAttributeName()) && !s.equals(cueSet.getSenCell().getAttributeName()))
                        .findFirst().orElse(null);

                Cell cuesetCell = cueSet.getCells().get(0);
                double cuesetCellVal;

                switch (session.getSchemaTypeDict().get(cuesetCell.getAttributeName())) {
                    case INTEGER:
                        cuesetCellVal = cuesetCell.getIntCellValue();
                        break;
                    case DOUBLE:
                        cuesetCellVal = cuesetCell.getDoubleCellValue();
                        break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + session.getSchemaTypeDict().get(cuesetCell.getAttributeName()));
                }

                double outputAttrDomMin = session.getNumericalAttrDomMin().get(pbdInstance.getOutputAttr());
                double outputAttrDomMax = session.getNumericalAttrDomMax().get(pbdInstance.getOutputAttr());

                double inputAttrNotInCuesetDomMax = session.getNumericalAttrDomMax().get(inputAttrNotInCueset);
                double inputAttrNotInCuesetDomMin = session.getNumericalAttrDomMin().get(inputAttrNotInCueset);

                if (cueSet.getSenCell().getAttributeName().equals(pbdInstance.getOutputAttr())) {
                    /**
                     * if the sensitive cell is on the output of the pbd
                     * e.g.
                     *      A * B = C(')
                     *      C is sensitive, A is in this cueset
                     *
                     *   we first get the domain min and max of B,
                     *   use the value of A, we can calculate the possible range of C
                     *   this range is the leakage to C
                     */

                    double leakageLowerBound = cuesetCellVal * inputAttrNotInCuesetDomMin;
                    double leakageUpperBound = cuesetCellVal * inputAttrNotInCuesetDomMax;

                    tempState.setLow(leakageLowerBound);
                    tempState.setHigh(leakageUpperBound);

                    if (stateOfTargetCell.getHigh() > leakageUpperBound)
                        stateOfTargetCell.setHigh(Math.max(leakageUpperBound, stateOfTargetCell.getLow()));
                    if (stateOfTargetCell.getLow() < leakageLowerBound)
                        stateOfTargetCell.setLow(Math.min(leakageLowerBound, stateOfTargetCell.getHigh()));

                }
                else {
                    /**
                     * if the sensitive cell is on the input side of the pbd
                     *
                     * e.g.
                     *      A(') * B = C
                     *      A is sensitive, then C is in the cueset
                     *
                     *   we first get the domain min and max of B,
                     *   use the value of C, we can calculate the possible range of A
                     *   this range is the leakage to A
                     *
                     */

                    double senCellDomMax = session.getNumericalAttrDomMax().get(cueSet.getSenCell().getAttributeName());
                    double senCellDomMin = session.getNumericalAttrDomMin().get(cueSet.getSenCell().getAttributeName());

                    if (inputAttrNotInCuesetDomMax == 0) {
                        inputAttrNotInCuesetDomMax = 0.001;  // avoid divided-by-zero issue
                    }

                    if (inputAttrNotInCuesetDomMin == 0) {
                        inputAttrNotInCuesetDomMin = 0.001;  // avoid divided-by-zero issue
                    }

                    double leakageLowerBound = cuesetCellVal / inputAttrNotInCuesetDomMax;
                    double leakageUpperBound = cuesetCellVal / inputAttrNotInCuesetDomMin;

                    if (leakageLowerBound < senCellDomMin)
                        leakageLowerBound = senCellDomMin;
                    if (leakageUpperBound > senCellDomMax)
                        leakageUpperBound = senCellDomMax;

                    tempState.setLow(leakageLowerBound);
                    tempState.setHigh(leakageUpperBound);

                    if (stateOfTargetCell.getHigh() > leakageUpperBound)
                        stateOfTargetCell.setHigh(Math.max(leakageUpperBound, stateOfTargetCell.getLow()));
                    if (stateOfTargetCell.getLow() < leakageLowerBound)
                        stateOfTargetCell.setLow(Math.min(leakageLowerBound, stateOfTargetCell.getHigh()));

                }

                stateOfTargetCell.checkFullLeakage();
                tempState.checkFullLeakage();
                cueSet.setLeakageToParent(computeLeakageToParent(targetCell, tempState));
            }

        }

        targetCell.setCurState(stateOfTargetCell);

        return stateOfTargetCell;
    }

    /**
     * Compute the percentage leakage to the parent cell.
     * Used for sorting the cuesets to hide.
     *
     * If full leakage, return 1;
     * if no leakage, return 0;
     * otherwise, return leaked domain size / total domain size.
     *
     * @param parentCell the target cell for calculating the leakage
     * @param state the state corresponding to a single cueset
     * @return the percentage leakage towards the parent cell
     */
    private static double computeLeakageToParent(Cell parentCell, State state) {
        if (state.isFullLeakage())
            return 1;

        // for discrete domain attributes
        if (parentCell.getCellType().equals(AttributeType.STRING))
            return state.getMinusSetString().size() / parentCell.getAttrDomSize();

        // for continuous domain attributes
        else if (parentCell.getCellType().equals(AttributeType.INTEGER) ||
                parentCell.getCellType().equals(AttributeType.DOUBLE))
            return ( (state.getLow() - parentCell.getMinDomain()) + (parentCell.getMaxDomain() - state.getHigh()))
               / (parentCell.getMaxDomain() - parentCell.getMinDomain());

        else
            throw new java.lang.Error("LeakageCalculator (computeLeakageToParent): Unsupported attribute type.");

    }


}
