package edu.policy.manager;

import edu.policy.model.AttributeType;
import edu.policy.model.constraint.Cell;
import edu.policy.model.constraint.ConstraintType;
import edu.policy.model.constraint.Provenance;
import edu.policy.model.cue.CueSet;
import edu.policy.model.data.Session;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PBDCueDetector {

    Session session;

    private static final Logger logger = LogManager.getLogger(PBDCueDetector.class);

    public PBDCueDetector() {

    }

    /**
     * Function Detect.
     * Detect cuesets of a list of sensitive cells based on function constraints.
     *
     * @param dependencies
     * @param senCells
     * @return
     */
    public Set<CueSet> detect (List<Provenance> dependencies, List<Cell> senCells) {

        logger.info(String.format("Generating cuesets for %d provenance-based dependencies and %d sensitive cells.", dependencies.size(), senCells.size()));

        Set<CueSet> cueSetListReturn = new HashSet<>();

        for (Provenance pbd: dependencies) {

            cueSetListReturn.addAll(sensitiveInput(pbd, senCells));
            cueSetListReturn.addAll(sensitiveOutput(pbd, senCells));

        }

        return cueSetListReturn;
    }

    /**
     * Given a function constraint pbd and a list of sensitive cells,
     * return a list of cuesets for those sensitive cells whose attribute is on the input side of the constraint.
     *
     * Example:
     * Tax = fn(Salary, TaxRate)
     * Generate cuesets for sensitive cells whose attribute is salary or taxrate.
     *
     * Rule:
     * If the sensitive cell is on the input of the constraint, the output cell will be in the cueset.
     *
     * @param pbd
     * @param senCells
     * @return
     */
    private List<CueSet> sensitiveInput (Provenance pbd, List<Cell> senCells) {

        List<CueSet> cueSetListReturn = new ArrayList<>();

        List<String> inputAttrs = pbd.getInputAttrs();
        String outputAttr = pbd.getOutputAttr();

        List<Cell> senCellsWithOutputAttr = senCells.stream().filter(cell -> cell.getAttributeName().equals(outputAttr)).collect(Collectors.toList());

        List<Integer> filterTID = new ArrayList<>();

        for (String inputAttr: inputAttrs) {
            List<Cell> senCellsWithInputAttr = senCells.stream().filter(cell -> (cell.getAttributeName().equals(inputAttr))
                    && (! filterTID.contains(cell.getTupleID())) ).collect(Collectors.toList());

            for (Cell cell: senCellsWithInputAttr) {
                if (senCellsWithOutputAttr.stream().map((Function<Cell, Object>) Cell::getTupleID).collect(Collectors.toList())
                        .contains(cell.getTupleID())) {
                    filterTID.add(cell.getTupleID());
                    continue;
                }

                cueSetListReturn.add(cuesetGen(outputAttr, pbd.getOutputAttrType(), cell, pbd));
            }
        }

        return cueSetListReturn;
    }

    /**
     * Given a function constraint pbd and a list of sensitive cells,
     * return a list of cuesets for those sensitive cells whose attribute is on the output side of the constraint.
     *
     * Example:
     * Tax = fn(Salary, TaxRate)
     * Generate cuesets for sensitive cells whose attribute is tax.
     *
     * Rule:
     * If the sensitive cell is on the output of the constraint, we generate |inputAttrs| many cuesets,
     * one cueset for each input attribute.
     *
     * @param pbd
     * @param senCells
     * @return
     */
    private List<CueSet> sensitiveOutput (Provenance pbd, List<Cell> senCells) {

        List<CueSet> cueSetListReturn = new ArrayList<>();

        List<String> inputAttrs = pbd.getInputAttrs();
        String outputAttr = pbd.getOutputAttr();

        List<Cell> senCellsWithOutputAttr = senCells.stream().filter(cell -> cell.getAttributeName().equals(outputAttr)).collect(Collectors.toList());

        List<Integer> filterTID = new ArrayList<>();


        for (Cell cell: senCellsWithOutputAttr) {

            for (String attr: inputAttrs) {
                List<Cell> senCellsWithInputAttr = senCells.stream().filter(sencell -> (sencell.getAttributeName().equals(attr))
                        && (! filterTID.contains(cell.getTupleID())) ).collect(Collectors.toList());

                if (senCellsWithInputAttr.stream().map((Function<Cell, Object>) Cell::getTupleID).collect(Collectors.toList())
                        .contains(cell.getTupleID())) {
                    filterTID.add(cell.getTupleID());
                    continue;
                }

                int attributeIndexOf = inputAttrs.indexOf(attr);
                AttributeType attributeType = pbd.getInputAttrTypes().get(attributeIndexOf);
                cueSetListReturn.add(cuesetGen(attr, attributeType, cell, pbd));
            }

        }


        return cueSetListReturn;
    }

    private CueSet cuesetGen(String attrName, AttributeType attrType, Cell senCell, Provenance pbd) {

        Cell cell = new Cell(senCell.getDatabaseName(), senCell.getRelationName(), attrName, senCell.getTupleID());

        cell.setCellType(attrType);

        CueSet cueSet = new CueSet(Collections.singletonList(cell), senCell, ConstraintType.PBD);

        cueSet.setPbdInstance(pbd);

        return cueSet;

    }
}
