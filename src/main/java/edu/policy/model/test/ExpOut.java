package edu.policy.model.test;

import edu.policy.model.constraint.Cell;
import edu.policy.model.data.Session;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ExpOut {
    int expID;

    int numSenCells;

    List<Cell> senCells;

    int numDeps;

    int numCuesets;

    double k_value;

    int numHiddenCells;

    Set<Cell> hiddenCells;

    String executionTime;

    Boolean randomCuesetChoosing;

    Boolean randomHiddenCellChoosing;

    Boolean useMVC;

    Boolean testOblCueset;

    Boolean isPagination;

    int DBSize;

    int attrNum;

    String usingAlgorithm;

    String policySenLevel;

    List<Integer> hiddenCellsFanOut;

    List<Integer> cueSetsFanOut;

    String fileName;

    DecimalFormat df = new DecimalFormat("#.##");

    private static final Logger logger = LogManager.getLogger(ExpOut.class);

    public ExpOut(Session session, int numCuesets, int numHiddenCells, Set<Cell> hiddenCells, String executionTime,
                  List<Integer> hiddenCellsFanOut, List<Integer> cueSetsFanOut, String fileName, Boolean testOblCueset,
                  Boolean isPagination) {

        this.expID = session.getExpID();
        this.numSenCells = session.getPolicies().size();
        this.senCells = session.getPolicies();
        this.numDeps = session.getDcs().size() + session.getPbds().size();
        this.k_value = session.getK_value();
        this.randomCuesetChoosing = session.getRandomCuesetChoosing();
        this.randomHiddenCellChoosing = session.getRandomHiddenCellChoosing();
        this.useMVC = session.getUseMVC();
        this.DBSize = session.getLimit();
        this.usingAlgorithm = session.getAlgo();
        this.policySenLevel = session.getPolicySenLevel();
        this.attrNum = session.getSchema().size();

        this.numCuesets = numCuesets;
        this.numHiddenCells = numHiddenCells;
        this.hiddenCells = hiddenCells;
        this.executionTime = executionTime;

        this.hiddenCellsFanOut = hiddenCellsFanOut;
        this.cueSetsFanOut = cueSetsFanOut;

        this.testOblCueset = testOblCueset;

        this.isPagination = isPagination;

        this.fileName = fileName;
    }

    public void toFile(String dir) throws IOException {

        BufferedWriter out = null;

        Path directory = Paths.get(dir);

        logger.info(String.format("Experimental reports store in: %s", directory));
        if (!Files.exists(directory)){
            Files.createDirectories(directory);
        }

        File expOut =new File(Paths.get(dir, fileName).toUri());
        try {
            if (!expOut.exists()) {
                FileWriter fstream = new FileWriter(expOut, true); // append mode

                out = new BufferedWriter(fstream);
                out.write("expID; numSenCells; senCells; numDeps; numCuesets; k_value; numHiddenCells; " +
                        "hiddenCells; executionTime (HH:mm:ss.SSS); randomCuesetChoosing; randomHiddenCellChoosing;" +
                        "DBSize; attrNum; policySenLevel; useMVC; usingAlgorithm; hiddenCellsFanOut; cueSetsFanOut; " +
                        "useOblCueset; isPagination; no. of levels  \n");
                printVarToExpOut(out);
            }
            else {
                FileWriter fstream = new FileWriter(expOut, true); // append mode

                out = new BufferedWriter(fstream);
                printVarToExpOut(out);
            }

        }

        catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }

        finally {
            if(out != null) {
                out.close();
            }
        }

    }

    private void printVarToExpOut(BufferedWriter out) throws IOException {
        out.write(expID + "; ");
        out.write(numSenCells + "; ");
        out.write(senCells.toString() + "; ");
        out.write(numDeps + "; ");
        out.write(numCuesets + "; ");
        out.write(df.format(k_value) + "; ");
        out.write(numHiddenCells + "; ");
        out.write(hiddenCells.toString() + "; ");
        out.write(executionTime + "; ");
        out.write(randomCuesetChoosing.toString() + "; ");
        out.write(randomHiddenCellChoosing.toString() + "; ");
        out.write(DBSize + "; ");
        out.write(attrNum + "; ");
        out.write(policySenLevel + "; ");
        out.write(useMVC + "; ");
        out.write(usingAlgorithm + "; ");
        out.write(hiddenCellsFanOut.toString() + "; ");
        out.write(cueSetsFanOut.toString() + "; ");
        out.write(testOblCueset + "; ");
        out.write(isPagination + "; ");
        out.write(Integer.toString(hiddenCellsFanOut.size()));
        out.write("\n");
    }

    public void toFile() throws IOException {
        // By default, the file will be stored under the project working dir
        String dir = System.getProperty("user.dir");
        this.toFile(dir);
    }

    @Override
    public String toString() {
        return "ExpOut{" +
                "expID=" + expID +
                ", numSenCells=" + numSenCells +
                ", senCells=" + senCells +
                ", numDeps=" + numDeps +
                ", numCuesets=" + numCuesets +
                ", k_value=" + k_value +
                ", numHiddenCells=" + numHiddenCells +
                ", hiddenCells=" + hiddenCells +
                ", executionTime='" + executionTime + '\'' +
                '}';
    }
}
