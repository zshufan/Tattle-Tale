package edu.policy.helper;

import edu.policy.model.constraint.Cell;
import edu.policy.model.cue.CueSet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.stream.Collectors;


public class MinimumSetCover {

    private static final Logger logger = LogManager.getLogger(MinimumSetCover.class);

    /**
     * Implementation of the greedy heuristic for minimum vertex cover problem.
     * Take in a list of cuesets,
     * find a minimum list of cells to hide that cover all the cuesets.
     *
     * @param cueSetList
     * @return
     */
    public static List<Cell> greedyHeuristic(List<CueSet> cueSetList) {

        List<Cell> retCellList = new ArrayList<>();

        List<CueSet> cueSetListCopy = new ArrayList<>(cueSetList);

        while (!cueSetListCopy.isEmpty()) {
            List<Cell> flattenCuesetList = cueSetListCopy.stream().flatMap(cueSet -> cueSet.getCells().stream()).collect(Collectors.toList());

            // get the frequency of the cells in the cueset list
            Map<Cell, Long> cellOccurrence = flattenCuesetList.stream().collect(Collectors.groupingBy(cell-> cell, Collectors.counting()));

            // add the max occurrence to the retCellList
            // from https://stackoverflow.com/questions/43616422/find-the-most-common-attribute-value-from-a-list-of-objects-using-stream
            Cell cellMaxOcc = Collections.max(cellOccurrence.entrySet(), Map.Entry.comparingByValue()).getKey();

            logger.debug(String.format("Max Frequency of the cell: %s, %s", cellOccurrence.get(cellMaxOcc), cellMaxOcc.toString()));

            retCellList.add(cellMaxOcc);

            // delete the cuesets with this cell
            cueSetListCopy.removeIf(cueSet -> cueSet.getCells().contains(cellMaxOcc));
        }

        return retCellList;


    }
}
