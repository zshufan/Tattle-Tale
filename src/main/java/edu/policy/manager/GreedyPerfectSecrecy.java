package edu.policy.manager;

import edu.policy.helper.MinimumSetCover;
import edu.policy.helper.Utils;
import edu.policy.model.constraint.Cell;
import edu.policy.model.constraint.DataDependency;
import edu.policy.model.constraint.Provenance;
import edu.policy.model.cue.CueSet;
import edu.policy.model.data.Session;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class GreedyPerfectSecrecy extends GreedyAlgorithm {

    private static final Logger logger = LogManager.getLogger(GreedyPerfectSecrecy.class);

    public GreedyPerfectSecrecy(Session session) {
        super(session);
    }

    public List<Cell> greedyHolisticPerfectDen() {

        List<Cell> senCells = session.getPolicies();

        totalCuesetSize = 0;

        hideCells = new ArrayList<>();
        cuesets = new ArrayList<>();

        useMVC = session.getUseMVC();

        for (Cell senCell: senCells) {
            if (!hideCells.contains(senCell))
                hideCells.add(senCell);
        }

        if (session.getTestOblCueset())
            greedyPerfectDenWithOblCueset(senCells);  // baseline method
        else
            greedyPerfectDen(senCells);

        System.out.println(hideCells);
        return hideCells;
    }

    /**
     * Basic version of perfect deniability algorithm.
     * Using **depth-first** search.
     * Take in a single sensitive cell as input,
     * fan out the tree to find the cuesets until no more cells needed to hide.
     *
     * @param senCell sensitive cell
     */
    public void greedyPerfectDen(Cell senCell) {

        if (!hideCells.contains(senCell))
            hideCells.add(senCell);

        List<DataDependency> schemaDependencies = session.getDcs();
        List<Provenance> schemaPBDs = session.getPbds();

        // Cueset detection
        List<CueSet> onDetect = cueDetector.detect(schemaDependencies, senCell);
        if (!onDetect.isEmpty()) {
            cuesets.addAll(onDetect);
            totalCuesetSize += onDetect.size();
            cueSetsFanOut.add(totalCuesetSize);
        }

        // main while loop
        while (!cuesets.isEmpty()) {

            CueSet cueSet;
            if (randomCuesetChoosing)
                cueSet = cuesets.get(rand.nextInt(cuesets.size()));
            else
                cueSet = cuesets.get(0); // get the first cueset
            cueSet.setChosenToBeHidden(Boolean.TRUE);

            if (!hasIntersection(cueSet.getCells(), hideCells)) { // no cell in cueset is hidden

                Cell cell;

                if (randomHiddenCellChoosing)
                    cell = cueSet.getCells().get(rand.nextInt(cueSet.getCells().size()));
                else
                    cell = cueSet.getCells().get(0); // get the first cell in the cueset

                hideCells.add(cell); // hide this cell

                // Get the cuesets of this cell
                onDetect = cueDetector.detect(schemaDependencies, cell);
                if (!onDetect.isEmpty()){
                    cuesets.addAll(onDetect);
                    totalCuesetSize += onDetect.size();
                    cueSetsFanOut.add(totalCuesetSize);
                }
            }

            // remove the current cueset from the cueset list
            cuesets.remove(cueSet);
        }
    }

    /**
     * Another version of the perfect deniability algorithm.
     * Using **breath-first** search.
     * Take in a list of sensitive cells as input,
     * fan out all the trees together to find cuesets of each level of the fan-out trees,
     * hide cells until they cover all detected cuesets.
     *
     * We have a flag to enable minimum vertex cover (MVC) in this version of the algorithm.
     *
     * @param senCells a list of sensitive cells
     */

    public void greedyPerfectDen(List<Cell> senCells) {

        if (!hideCells.containsAll(senCells))
            hideCells = Utils.unionCells(hideCells, senCells);

        List<DataDependency> schemaDependencies = session.getDcs();
        List<Provenance> schemaPBDs = session.getPbds();

        // Cueset detection
        List<CueSet> onDetect = cueDetector.detect(schemaDependencies, senCells);
        Set<CueSet> pbdOnDetect = new HashSet<>();
        if (!schemaPBDs.isEmpty())
            pbdOnDetect = pbdCueDetector.detect(schemaPBDs, senCells);
        cuesetDetectorInvokeCounter += 1;
        logger.info(String.format("The %d-th time invoking cueset detector.", cuesetDetectorInvokeCounter));

        if (!onDetect.isEmpty()) {
            cuesets.addAll(onDetect);

            if (!pbdOnDetect.isEmpty()) {
                cuesets.addAll(pbdOnDetect);
                totalCuesetSize += pbdOnDetect.size();
            }

            totalCuesetSize += onDetect.size();

            cueSetsFanOut.add(totalCuesetSize);
            logger.info(String.format("%d cuesets being detected.", onDetect.size() + pbdOnDetect.size()));
        }


        // main while loop
        while (!cuesets.isEmpty()) {

            if (testFanOut)
                if (cuesetDetectorInvokeCounter >= 6)
                    break;

            List<Cell> toHide = new ArrayList<>();

            if (useMVC) {

                long startTime_MVC = new Date().getTime();
                // test MVC
                cuesets.removeIf(cueSet -> hasIntersection(cueSet.getCells(), hideCells));

                toHide.addAll(MinimumSetCover.greedyHeuristic(cuesets));

                long endTime_MVC = new Date().getTime();
                long timeElapsed = endTime_MVC - startTime_MVC;
                logger.info(String.format("Finished executing MVC; use time: %d ms.", timeElapsed));

            }
            else {
                for (CueSet cueSet: cuesets) {

                    cueSet.setChosenToBeHidden(Boolean.TRUE);

                    if (!hasIntersection(cueSet.getCells(), toHide)
                            && !hasIntersection(cueSet.getCells(), hideCells) ) { // no cell in cueset is hidden

                        Cell cell;

                        if (randomHiddenCellChoosing)
                            cell = cueSet.getCells().get(rand.nextInt(cueSet.getCells().size()));
                        else
                            cell = cueSet.getCells().get(0); // get the first cell in the cueset

                        toHide.add(cell); // for detect the cuesets of this cell

                    }
                }
            }


            if (!toHide.isEmpty()) {

                hideCells.addAll(toHide); // hide this cell
                hiddenCellsFanOut.add(hideCells.size());
                logger.info(String.format("%d cells are hidden at the %d-th level.", hideCells.size(), cuesetDetectorInvokeCounter));

                // Get the cuesets of tohide cell lists
                onDetect = cueDetector.detect(schemaDependencies, toHide);
                cuesetDetectorInvokeCounter += 1;
                if (!schemaPBDs.isEmpty())
                    pbdOnDetect = pbdCueDetector.detect(schemaPBDs, toHide);
                logger.info(String.format("The %d-th time invoking cueset detector.", cuesetDetectorInvokeCounter));

                // remove the current cueset from the cueset list
                cuesets.clear();

                if (!onDetect.isEmpty()){
                    cuesets.addAll(onDetect);
                    if (!pbdOnDetect.isEmpty()) {
                        cuesets.addAll(pbdOnDetect);
                        totalCuesetSize += pbdOnDetect.size();
                    }

                    totalCuesetSize += onDetect.size();

                    cueSetsFanOut.add(totalCuesetSize);
                    logger.info(String.format("%d cuesets being detected.", onDetect.size() + pbdOnDetect.size()));
                }
            }

        }
    }

    /**
     * Baseline method of the perfect deniability algorithm.
     * Using **breath-first** search.
     * Take in a list of sensitive cells as input,
     * fan out all the trees together to find cuesets of each level of the fan-out trees,
     * hide cells until they cover all detected cuesets.
     *
     * Note: The cueset detection in this algorithm is not based on TTC, but partial TTC.
     * We name this type of cuesets as oblivious cueset.
     *
     * We have a flag to enable minimum vertex cover (MVC) in this version of the algorithm.
     *
     * @param senCells a list of sensitive cells
     */

    public void greedyPerfectDenWithOblCueset(List<Cell> senCells) {

        OblCueDetector cueDetector = new OblCueDetector(session);

        if (!hideCells.containsAll(senCells))
            hideCells = Utils.unionCells(hideCells, senCells);

        List<DataDependency> schemaDependencies = session.getDcs();
        List<Provenance> schemaPBDs = session.getPbds();

        // Cueset detection
        List<CueSet> onDetect = cueDetector.detect(schemaDependencies, senCells);
        Set<CueSet> pbdOnDetect = new HashSet<>();
        if (!schemaPBDs.isEmpty())
            pbdOnDetect = pbdCueDetector.detect(schemaPBDs, senCells);
        cuesetDetectorInvokeCounter += 1;
        logger.info(String.format("The %d-th time invoking cueset detector.", cuesetDetectorInvokeCounter));

        if (!onDetect.isEmpty()) {
            cuesets.addAll(onDetect);

            if (!pbdOnDetect.isEmpty()) {
                cuesets.addAll(pbdOnDetect);
                totalCuesetSize += pbdOnDetect.size();
            }

            totalCuesetSize += onDetect.size();

            cueSetsFanOut.add(totalCuesetSize);
            logger.info(String.format("%d cuesets being detected.", onDetect.size() + pbdOnDetect.size()));
        }


        // main while loop
        while (!cuesets.isEmpty()) {

            if (testFanOut)
                if (cuesetDetectorInvokeCounter >= 5)
                    break;

            List<Cell> toHide = new ArrayList<>();

            if (useMVC) {

                long startTime_MVC = new Date().getTime();
                // test MVC
                cuesets.removeIf(cueSet -> hasIntersection(cueSet.getCells(), hideCells));

                toHide.addAll(MinimumSetCover.greedyHeuristic(cuesets));

                long endTime_MVC = new Date().getTime();
                long timeElapsed = endTime_MVC - startTime_MVC;
                logger.info(String.format("Finished executing MVC; use time: %d ms.", timeElapsed));

            }
            else {
                for (CueSet cueSet: cuesets) {

                    cueSet.setChosenToBeHidden(Boolean.TRUE);

                    if (!hasIntersection(cueSet.getCells(), toHide)
                            && !hasIntersection(cueSet.getCells(), hideCells) ) { // no cell in cueset is hidden

                        Cell cell;

                        if (randomHiddenCellChoosing)
                            cell = cueSet.getCells().get(rand.nextInt(cueSet.getCells().size()));
                        else
                            cell = cueSet.getCells().get(0); // get the first cell in the cueset

                        toHide.add(cell); // for detect the cuesets of this cell

                    }
                }
            }


            if (!toHide.isEmpty()) {

                hideCells.addAll(toHide); // hide this cell
                hiddenCellsFanOut.add(hideCells.size());
                logger.info(String.format("%d cells are hidden at the %d-th level.", hideCells.size(), cuesetDetectorInvokeCounter));

                // Get the cuesets of tohide cell lists
                onDetect = cueDetector.detect(schemaDependencies, toHide);
                cuesetDetectorInvokeCounter += 1;
                if (!schemaPBDs.isEmpty())
                    pbdOnDetect = pbdCueDetector.detect(schemaPBDs, toHide);
                logger.info(String.format("The %d-th time invoking cueset detector.", cuesetDetectorInvokeCounter));

                // remove the current cueset from the cueset list
                cuesets.clear();

                if (!onDetect.isEmpty()){
                    cuesets.addAll(onDetect);
                    if (!pbdOnDetect.isEmpty()) {
                        cuesets.addAll(pbdOnDetect);
                        totalCuesetSize += pbdOnDetect.size();
                    }

                    totalCuesetSize += onDetect.size();

                    cueSetsFanOut.add(totalCuesetSize);
                    logger.info(String.format("%d cuesets being detected.", onDetect.size() + pbdOnDetect.size()));
                }
            }

        }
    }

    /**
     * Check if exists intersection between list1 and list2.
     * optimization: https://stackoverflow.com/questions/58320338/which-is-the-fastest-way-for-a-containsany-check
     *
     * @param list1 a list of cells
     * @param list2 a list of cells
     * @return return True if list1 and list2 have intersection; return False otherwise.
     */
    boolean hasIntersection(List<Cell> list1, List<Cell> list2) {
        return !Collections.disjoint(list1, list2);
    }

    List<Cell> intersection(List<Cell> list1, List<Cell> list2) {
        List<Cell> list = new ArrayList<>();

        for (Cell t1 : list1) {
            for (Cell t2: list2) {
                if (t1.equals(t2)) { // since we have customized equals and hashcode implementation
                    list.add(t1);
                }
            }
        }

        return list;
    }

    public int getTotalCuesetSize() {
        return totalCuesetSize;
    }

    public boolean isRandomCuesetChoosing() {
        return randomCuesetChoosing;
    }

    public void setRandomCuesetChoosing(boolean randomCuesetChoosing) {
        this.randomCuesetChoosing = randomCuesetChoosing;
    }

    public Boolean getRandomHiddenCellChoosing() {
        return randomHiddenCellChoosing;
    }

    public void setRandomHiddenCellChoosing(Boolean randomHiddenCellChoosing) {
        this.randomHiddenCellChoosing = randomHiddenCellChoosing;
    }

    public void setRandomSeed(long randomSeed) {
        this.randomSeed = randomSeed;
        this.rand = new Random(randomSeed);
    }

}
