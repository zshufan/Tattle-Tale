package edu.policy.manager;

import edu.policy.model.constraint.Cell;
import edu.policy.model.cue.CueSet;
import edu.policy.model.data.Session;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GreedyAlgorithm {

    List<Cell> hideCells = new ArrayList<>();

    List<CueSet> cuesets = new ArrayList<>();

    CueDetector cueDetector;

    PBDCueDetector pbdCueDetector;

    boolean randomCuesetChoosing = Boolean.TRUE;

    Boolean randomHiddenCellChoosing = Boolean.TRUE;

    long randomSeed = 42; // for repeatability

    Random rand = new Random(randomSeed);

    int totalCuesetSize;

    String usingAlgorithm;

    Session session;

    int cuesetDetectorInvokeCounter;

    Boolean useMVC;

    Boolean testFanOut;

    List<Integer> hiddenCellsFanOut = new ArrayList<>();

    List<Integer> cueSetsFanOut = new ArrayList<>();

    private static final Logger logger = LogManager.getLogger(GreedyAlgorithm.class);

    public GreedyAlgorithm(Session session) {
        this.session = session;
        cueDetector = new CueDetector(session);
        pbdCueDetector = new PBDCueDetector();
        this.setRandomSeed(session.getRandomSeed());
        logger.debug(String.format("Session.randomSeed: %d, this.randomSeed: %d", session.getRandomSeed(), randomSeed));
        this.setRandomCuesetChoosing(session.getRandomCuesetChoosing());
        this.setRandomHiddenCellChoosing(session.getRandomHiddenCellChoosing());
        this.testFanOut = session.getTestFanOut();
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

    public String getUsingAlgorithm() {
        return usingAlgorithm;
    }

    public void setUsingAlgorithm(String usingAlgorithm) {
        this.usingAlgorithm = usingAlgorithm;
    }

    public List<Integer> getHiddenCellsFanOut() {
        return hiddenCellsFanOut;
    }

    public List<Integer> getCueSetsFanOut() {
        return cueSetsFanOut;
    }
}
